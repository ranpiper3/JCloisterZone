package com.jcloisterzone.wsio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.wsio.message.HelloMessage;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.message.PingMessage;
import com.jcloisterzone.wsio.message.WelcomeMessage;
import com.jcloisterzone.wsio.message.WsMessage;

public class WebSocketConnection implements Connection {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private ReportingTool reportingTool;

    private MessageParser parser = new MessageParser();
    //private WebSocketClientImpl ws;
    private WebSocket ws;
    private URI uri;
    private final MessageListener listener;

    private long msgSequence;
    private String sessionId;
    private String clientId;
    private String secret; //TODO will be used for message signing
    private String nickname;

    private boolean closedByUser;
    private int pingInterval = 0;
    private String maintenance;

    // TODO do not use 1 executor per connection
    // TODO executor shutdown?
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> pingFuture;
    private ScheduledFuture<?> reconnectFuture;

    public class WebSocketClientImpl implements WebSocket.Listener {
        private String username;
        private String reconnectGameId;

        public WebSocketClientImpl(String username, String reconnectGameId) {
            this.username = username;
            this.reconnectGameId = reconnectGameId;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
            ws = webSocket;

            msgSequence = 1;
            WebSocketConnection.this.send(new HelloMessage(username, clientId, secret));
            if (reconnectGameId != null) {
                JoinGameMessage msg = new JoinGameMessage();
                msg.setGameId(reconnectGameId);
                WebSocketConnection.this.send(msg);
            }
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            cancelPing();
            logger.info("Closed with status " + statusCode + ", reason: " + reason);

            // sheduler.shutdown();
            ws = null;

            listener.onWebsocketClose(statusCode, reason, !closedByUser);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            //if (reconnectFuture != null) return; //don't handle connection refuse while trying to reconnect
//            if (ex instanceof WebsocketNotConnectedException) {
//                cancelPing();
//                listener.onWebsocketClose(0, ex.getMessage(), true);
//            } else {
//                listener.onWebsocketError(ex);
//            }
            logger.error(error.getMessage(), error);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            WebSocket.Listener.super.onText(webSocket, data, last);

            String payload = data.toString();

            WsMessage msg = parser.fromJson(data.toString());
            if (logger.isInfoEnabled()) {
                logger.info(payload);
            }

            if (msgSequence != msg.getSequenceNumber()) {
                String errMessage = String.format("Message lost. Received #%s but expected #%s.", msg.getSequenceNumber(), msgSequence);
                listener.onWebsocketError(new MessageLostException(errMessage));
                //close(Connection.CLOSE_MESSAGE_LOST, errMessage);
                ws.sendClose(Connection.CLOSE_MESSAGE_LOST, errMessage);
                return null;
            }
            msgSequence = msg.getSequenceNumber() + 1;

            if (msg instanceof WelcomeMessage) {
                WelcomeMessage welcome = (WelcomeMessage) msg;
                sessionId = welcome.getSessionId();
                nickname = welcome.getNickname();
                pingInterval = welcome.getPingInterval();
                maintenance = welcome.getMaintenance();
            }
            schedulePing();
            listener.onWebsocketMessage(msg);

            return null;
        }
    }

    /*
    class WebSocketClientImpl extends WebSocketClient {
        private String username;
        private String reconnectGameId;

        public WebSocketClientImpl(URI serverURI, String username, String reconnectGameId) {
            super(serverURI);
//            if (System.getProperty("hearthbeat") != null) {
//                setConnectionLostTimeout(Integer.parseInt(System.getProperty("hearthbeat")));
//            } else {
//                setConnectionLostTimeout(DEFAULT_HEARTHBEAT_INTERVAL);
//            }
            this.username = username;
            this.reconnectGameId = reconnectGameId;
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            cancelPing();

            // workaround for https://github.com/TooTallNate/Java-WebSocket/issues/587
            //ws.setConnectionLostTimeout(0);

            listener.onWebsocketClose(code, reason, remote && !closedByUser);
        }

        @Override
        public void onError(Exception ex) {
            if (reconnectFuture != null) return; //don't handle connection refuse while trying to reconnect
            if (ex instanceof WebsocketNotConnectedException) {
                cancelPing();
                listener.onWebsocketClose(0, ex.getMessage(), true);
            } else {
                listener.onWebsocketError(ex);
            }
        }

        @Override
        synchronized public void onMessage(String payload) {
            WsMessage msg = parser.fromJson(payload);
            if (logger.isInfoEnabled()) {
                logger.info(payload);
            }

            if (msgSequence != msg.getSequenceNumber()) {
                String errMessage = String.format("Message lost. Received #%s but expected #%s.", msg.getSequenceNumber(), msgSequence);
                listener.onWebsocketError(new MessageLostException(errMessage));
                close(Connection.CLOSE_MESSAGE_LOST, errMessage);
                return;
            }
            msgSequence = msg.getSequenceNumber() + 1;

            if (msg instanceof WelcomeMessage) {
                WelcomeMessage welcome = (WelcomeMessage) msg;
                sessionId = welcome.getSessionId();
                nickname = welcome.getNickname();
                pingInterval = welcome.getPingInterval();
                maintenance = welcome.getMaintenance();
            }
            schedulePing();
            listener.onWebsocketMessage(msg);
        }

        @Override
        public void onOpen(ServerHandshake arg0) {
            msgSequence = 1;
            WebSocketConnection.this.send(new HelloMessage(username, clientId, secret));
            if (reconnectGameId != null) {
                JoinGameMessage msg = new JoinGameMessage();
                msg.setGameId(reconnectGameId);
                WebSocketConnection.this.send(msg);
            }
        }
    }*/

    public WebSocketConnection(final String username, Config config, URI uri, MessageListener listener) {
        clientId = config.getClient_id();
        secret = config.getSecret();
        this.listener = listener;
        this.uri = uri;

        var httpClient = HttpClient.newBuilder().executor(scheduler).build();
        var webSocketBuilder = httpClient.newWebSocketBuilder();
        webSocketBuilder.buildAsync(uri, new WebSocketClientImpl(username, null)).join();

        //ws = new WebSocketClientImpl(uri, username, null);
        //ws.connect();
    }

    @Override
    public void reconnect(final String gameId) {
        reconnectFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                var httpClient = HttpClient.newBuilder().executor(scheduler).build();
                var webSocketBuilder = httpClient.newWebSocketBuilder();

                //TODO timeout and not connected handling
                ws = webSocketBuilder.buildAsync(uri, new WebSocketClientImpl(nickname, gameId)).join();
                stopReconnecting();

//                ws = new WebSocketClientImpl(uri, nickname, gameId);
//                try {
//                    if (ws.connectBlocking()) {
//                        stopReconnecting();
//                    }
//                } catch (InterruptedException e) {
//                }
            }
        }, 1, 4, TimeUnit.SECONDS);
    }

    @Override
    public void stopReconnecting() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }

    private void cancelPing() {
        if (pingFuture != null) {
            pingFuture.cancel(false);
            pingFuture = null;
        }
    }

    private void schedulePing() {
        if (pingInterval == 0) return;
        if (pingFuture != null) pingFuture.cancel(false);
        pingFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                WebSocketConnection.this.send(new PingMessage());
            }
        }, pingInterval, pingInterval, TimeUnit.SECONDS);
    }

    @Override
    public void send(WsMessage arg) {
        if (isClosed()) {
            return;
        }
        schedulePing();
        ws.sendText(parser.toJson(arg), true);
        // TODO handle err
//        try {
//
//        } catch (WebsocketNotConnectedException ex) {
//            listener.onWebsocketClose(0, ex.getMessage(), true);
//        }
    }

    @Override
    public void close() {
        closedByUser = true;
        ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
    }

    @Override
    public boolean isClosed() {
        return ws == null || ws.isInputClosed();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public MessageParser getParser() {
        return parser;
    }

    public String getMaintenance() {
        return maintenance;
    }

    public static class MessageLostException extends Exception {

        public MessageLostException(String message) {
            super(message);
        }
    }
}
