package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Player;
import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.event.GameOverEvent;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.capability.TunnelCapability.Tunnel;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.AppMenuBar.MenuItemDef;
import com.jcloisterzone.ui.controls.chat.GameChatPanel;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.panel.GameOverPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.view.ChannelView;
import com.jcloisterzone.ui.view.GameChatView;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.ui.view.StartView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.GameMessage.GameStatus;
import com.jcloisterzone.wsio.message.LeaveGameMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.message.WsReplayableMessage;

import io.vavr.collection.Array;
import io.vavr.collection.Stream;

public class GameController extends EventProxyUiController<Game> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Game game;
    private GameStatus gameStatus;
    private String channel;
    private boolean passwordProtected;

    private ReportingTool reportingTool;

    private GameView gameView;
    private Connection connProxy;

    public GameController(Game game) {
        super(game);
        this.game = game;
        connProxy = new ConnectionProxy();
    }

    public Game getGame() {
        return game;
    }

    public String getGameId() {
        return game.getGameId();
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void onGameStarted(Game game) {
        Stream<PlayerSlot> slots = Stream.ofAll(Arrays.asList(game.getPlayerSlots()));
        Array<PlayerSlot> occupiedSlots = slots.filter(slot -> slot != null && slot.isOccupied()).toArray();
        // for free color we can't search slot - because for loaded game, slots are already filtered
        // to existing ones
        Array<PlayerColors> freeColors = Stream.range(0, PlayerSlot.COUNT)
            .filter(i -> occupiedSlots.find(s -> s.getNumber() == i).isEmpty())
            .map(i -> getConfig().getPlayerColor(i))
            .toArray();

        int occupiedSize = occupiedSlots.size();
        int freeSize = freeColors.size();
        int i = 0;
        for (PlayerSlot slot : occupiedSlots) {
            Map<Tunnel, Color> tunnelColors = new HashMap<>();
            tunnelColors.put(Tunnel.TUNNEL_A, slot.getColors().getMeepleColor());
            if (freeSize >= occupiedSize) {
                tunnelColors.put(Tunnel.TUNNEL_B, freeColors.get(i).getMeepleColor());
                i++;
            }
            if (freeSize >= 2 * occupiedSize) {
                tunnelColors.put(Tunnel.TUNNEL_C, freeColors.get(i).getMeepleColor());
                i++;
            }
            slot.getColors().setTunnelColors(tunnelColors);
        }

        if (gameView == null) {
            gameView = new GameView(this);
            if (getUiView() instanceof GameChatView) {
                GameChatView prevView = (GameChatView) getUiView();
                gameView.setChatPanel(prevView.getChatPanel());
            } else {
                gameView.setChatPanel(new GameChatPanel(game));
            }
        } else {
            gameView.getGridPanel().hideErrorMessage(GameView.RECONNECTING_ERR_MSG);
        }
        if (getUiView() != gameView) {
            Platform.runLater(() -> {
                mountView(gameView);
            });
        }
    }


    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        //TODO probably can be removed
        if (gameView == null) {
            logger.warn("gameView is null");
            return;
        }
        GameState state = ev.getCurrentState();

        if (ev.hasDiscardedTilesChanged()) {
            DiscardedTilesDialog discardedTilesDialog = FxClient.getInstance().getDiscardedTilesDialog();
            if (discardedTilesDialog == null) {
                discardedTilesDialog = new DiscardedTilesDialog();
                FxClient.getInstance().setDiscardedTilesDialog(discardedTilesDialog);
                getMenuBar().setItemEnabled(MenuItemDef.DISCARDED_TILES, true);
            }
            discardedTilesDialog.setDiscardedTiles(state.getDiscardedTiles());
            discardedTilesDialog.setVisible(true);
        }

        if (ev.hasPlayerActionsChanged()) {
            Player pl = state.getActivePlayer();
            boolean canUndo = pl != null && pl.isLocalHuman() && game.isUndoAllowed();
            getMenuBar().setItemEnabled(MenuItemDef.UNDO, canUndo);
        }

        if (ev.hasTurnPlayerChanged()) {
            Player pl = state.getTurnPlayer();

            if (pl.isLocalHuman()) {
                getAudioManager().beep();
            }

            // TODO better image quality ?
            Color c = pl.getColors().getMeepleColor();
            Image image = getResourceManager().getLayeredImage(new LayeredImageDescriptor(SmallFollower.class, c));
            getPrimaryStage().getIcons().clear();
            getPrimaryStage().getIcons().add(SwingFXUtils.toFXImage((BufferedImage) image, null));
        }
    }

    @Subscribe
    public void handleGameStateChange(GameOverEvent ev) {
        boolean showPlayAgain = FxClient.getInstance().getLocalServer() != null;
        gameView.setGameRunning(false);
        //TODO allow chat after game also for standalone server
        if (getChannel() == null && gameView.getChatPanel() != null) {
            gameView.getGridPanel().remove(gameView.getChatPanel());
        }
        FxClient.getInstance().closeGame(true);
        GameOverPanel panel = new GameOverPanel(this, showPlayAgain);
        gameView.getGridPanel().add(panel, "pos 0 35");
        gameView.getGridPanel().revalidate();
    }

    public void refreshWindowTitle() {
        StringBuilder title = new StringBuilder(Client.BASE_TITLE);
        GameState state = game.getState();

        Player activePlayer = state.getActivePlayer();
        if (activePlayer != null) {
            title.append(" ⋅ ").append(activePlayer.getNick());
        }
        int packSize = state.getTilePack().totalSize();
        title.append(" ⋅ ").append(String.format(_tr("%d tiles left"), packSize));

        getPrimaryStage().setTitle(title.toString());
    }

    // User interface

    //@Override
    public void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }

//    @Subscribe
//    public void handleSelectMageAndWitchRemoval(MageWitchSelectRemoval ev) {
//        SelectMageWitchRemovalPanel panel = new SelectMageWitchRemovalPanel(this);
//        GridPanel gridPanel = gameView.getGridPanel();
//        //gridPanel.setMageWitchPanel(panel);
//        gridPanel.add(panel, "pos (100%-525) 0 (100%-275) 100%"); //TODO more robust layouting
//        gridPanel.revalidate();
//
//    }

    public void leaveGame() {
        if (getChannel() == null) {
            mountView(new StartView());
        } else {
            if (getConnection().isClosed()) {
                //TODO stop reconnecting
                mountView(new StartView());
            } else {
                ClientMessageListener cml = FxClient.getInstance().getClientMessageListener();
                getConnection().send(new LeaveGameMessage());
                ChannelController ctrl = cml.getChannelControllers().get(channel);
                mountView(new ChannelView(ctrl));

                List<GameController> gcs = cml.getGameControllers(channel);
                ctrl.getEventProxy().post(
                    new GameListChangedEvent(gcs.toArray(new GameController[gcs.size()]))
                );
            }
        }
    }

    public GameView getGameView() {
        return gameView;
    }

    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }

    @Override
    public Connection getConnection() {
        return connProxy;
    }

    /**
     * Connection proxy sets gameId field for every sent WsInGameMessage
     */
    class ConnectionProxy implements Connection {

        private Connection getConnection() {
            return FxClient.getInstance().getConnection();
        }

        @Override
        public void send(WsMessage msg) {
            if (msg instanceof WsInGameMessage) {
                ((WsInGameMessage) msg).setGameId(game.getGameId());
            }
            if (msg instanceof WsReplayableMessage) {
                ((WsReplayableMessage) msg).setMessageId(game.getMessageId());
            }
            getConnection().send(msg);
        }

        @Override
        public boolean isClosed() {
            if (getConnection() != null) {
                return getConnection().isClosed();
            } else {
                return false;
            }
        }

        @Override
        public void close() {
            if (getConnection() != null) {
                getConnection().close();
            }
        }

        @Override
        public void reconnect(String gameId) {
            getConnection().reconnect(gameId);
        }

        @Override
        public void stopReconnecting() {
            if (getConnection() != null) {
                getConnection().stopReconnecting();
            }
        }

        @Override
        public String getSessionId() {
            return getConnection().getSessionId();
        }

        @Override
        public String getNickname() {
            return FxClient.getInstance().getConnection().getNickname();
        }
    }
}