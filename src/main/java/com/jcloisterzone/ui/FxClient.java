package com.jcloisterzone.ui;

import com.jcloisterzone.bugreport.ReportingTool;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.ConfigLoader;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.save.SavedGame;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.ui.dialog.DiscardedTilesDialog;
import com.jcloisterzone.ui.gtk.MenuFix;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.theme.Theme;
import com.jcloisterzone.ui.view.*;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WebSocketConnection;
import com.jcloisterzone.wsio.server.SimpleServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.jcloisterzone.ui.I18nUtils._tr;

public class FxClient extends Application {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String BASE_TITLE = "JCloisterZone";
    public static final Image DEFAULT_ICON = new Image(FxClient.class.getClassLoader().getResourceAsStream("sysimages/ico.png"));

    // HACK use same properties as old Swint Client
    public static Path dataDirectory;
    public static Config config;
    public static ConfigLoader configLoader;
    public static ConvenientResourceManager resourceManager;
    public static List<Plugin> plugins;

    private static FxClient instance;
    private AudioManager audioManger;

    private Stage primaryStage;
    private BorderPane rootPane;
    private AppMenuBar menuBar;
    private SwingNode swingNode;

    private UiView view;
    private Theme theme;

    private DiscardedTilesDialog discardedTilesDialog;

    private final AtomicReference<SimpleServer> localServer = new AtomicReference<>();
    private ClientMessageListener clientMessageListener;



    // little bit HACK, just easy way how to be able load integration test games in UI
    // works only for local games
    private HashMap<String, Object> savedGameAnnotations;

    public static FxClient getInstance() {
        return instance;
    }

    public AudioManager getAudioManger() {
        return audioManger;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public UiView getUiView() {
        return view;
    }

    public Theme getTheme() {
        return theme;
    }

    public AppMenuBar getMenuBar() {
        return menuBar;
    }

    public Config getConfig() {
        return config;
    }

    public ClientMessageListener getClientMessageListener() {
        return clientMessageListener;
    }

    public HashMap<String, Object> getSavedGameAnnotations() {
        return savedGameAnnotations;
    }

    public void saveConfig() {
        configLoader.save(config);
        resourceManager.reload();
    }

    public void connect(String hostname, int port) {
        connect(null, hostname, port, false);
    }

    public void connectPlayOnline(String username) {
        String configValue =  getConfig().getPlay_online_host();
        String[] hp = ((configValue == null || configValue.trim().length() == 0) ? ConfigLoader.DEFAULT_PLAY_ONLINE_HOST : configValue).split(":");
        int port = 80;
        if (hp.length > 1) {
            port = Integer.parseInt(hp[1]);
        }
        connect(username, hp[0], port, true);
    }

    private String getUserName() {
        if (System.getProperty("nick") != null) {
            return System.getProperty("nick");
        }
        String name = config.getClient_name();
        name = name == null ? "" : name.trim();
        if (name.equals("")) name = System.getProperty("user.name");
        if (name.equals("")) name = UUID.randomUUID().toString().substring(2, 6);
        return name;
    }

    private void connect(String username, String hostname, int port, boolean playOnline) {
        clientMessageListener = new ClientMessageListener(playOnline);
        try {
            URI uri = new URI("ws", null, "".equals(hostname) ? "localhost" : hostname, port, playOnline ? "/ws" : "/", null, null);
            logger.info("Connection to {}", uri);
            WebSocketConnection conn = clientMessageListener.connect(username == null ? getUserName() : username, uri);
            conn.setReportingTool(new ReportingTool());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public SimpleServer getLocalServer() {
        return localServer.get();
    }

    public void createGame() {
        createGame(null, null);
    }

    public void createGame(Game game) {
        createGame(null, game);
    }

    public void createGame(SavedGame savedGame) {
        createGame(savedGame, null);
    }

    private void createGame(SavedGame savedGame, Game game) {
        if (closeGame()) {
            if (savedGame != null) {
                savedGameAnnotations = savedGame.getAnnotations();
            } else {
                savedGameAnnotations = null;
            }

            int port = config.getPort() == null ? ConfigLoader.DEFAULT_PORT : config.getPort();
            SimpleServer server = new SimpleServer(new InetSocketAddress(port), new SimpleServer.SimpleServerErrorHandler() {
                @Override
                public void onError(WebSocket ws, Exception ex) {
                    if (ex instanceof ClosedByInterruptException) {
                        logger.info(ex.toString()); //exception message is null
                    } else if (ex instanceof BindException) {
                        onServerStartError(ex);
                    } else {
                        logger.error(ex.getMessage(), ex);
                    }

                }
            });
            localServer.set(server);
            server.createGame(savedGame, game, config.getClient_id());
            server.start();
            try {
                //HACK - there is not success handler in WebSocket server
                //we must wait for start to now connect to
                Thread.sleep(50);
            } catch (InterruptedException e) {
                //empty
            }
            if (localServer.get() != null) { //can be set to null by server error
                connect(null, "localhost", port, false);
            }
        }
    }

    public boolean closeGame() {
        return closeGame(false);
    }

    public boolean closeGame(boolean force) {
        boolean isGameRunning = (view instanceof GameView) && ((GameView)view).isGameRunning();
        if (isGameRunning && !"false".equals(System.getProperty("closeGameConfirm"))) {
            ButtonType buttonLeave = new ButtonType(_tr("Leave game"));
            ButtonType buttonCancel = new ButtonType(_tr("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            if (localServer.get() != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.getButtonTypes().setAll(buttonLeave, buttonCancel);
                alert.setTitle(_tr("Leave game"));
                alert.setHeaderText(_tr("Leave game"));
                alert.setContentText(_tr("The game is not finished. Do you really want to stop game and disconnect all other players?"));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != buttonLeave) {
                    return false;
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.getButtonTypes().setAll(buttonLeave, buttonCancel);
                alert.setTitle(_tr("Leave game"));
                alert.setHeaderText(_tr("Leave game"));
                alert.setContentText(_tr("The game is not finished. Do you really want to leave it?"));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != buttonLeave) {
                    return false;
                }
            }
        }

        primaryStage.setTitle(BASE_TITLE);
        resetWindowIcon();
        if (clientMessageListener != null && !clientMessageListener.isPlayOnline()) {
            clientMessageListener.getConnection().close();
            clientMessageListener = null;
        }
        SimpleServer server = localServer.get();
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            localServer.set(null);
        }

        //TODO decouple
        if (view instanceof GameView) {
            ((GameView)view).closeGame();
        }

        if (discardedTilesDialog != null) {
            discardedTilesDialog.dispose();
            discardedTilesDialog = null;
        }
        return true;
    }

    public void onServerStartError(final Exception ex) {
        localServer.set(null);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(_tr("Error"));
            alert.setContentText(ex.getLocalizedMessage());
            alert.showAndWait();
        });
    }

    public File getSavesDirectory() {
        String savesFolderValue = getConfig().getSaved_games().getFolder();
        File savesFolder;
        if (savesFolderValue == null || savesFolderValue.isEmpty()) {
            savesFolder = dataDirectory.resolve("saves").toFile();
        } else {
            savesFolder = new File(savesFolderValue);
        }
        if (!savesFolder.exists()) {
            savesFolder.mkdir();
        }
        return savesFolder;
    }

    public File getScreenshotDirectory() {
        String screenFolderValue = getConfig().getScreenshots().getFolder();
        File folder;
        if (screenFolderValue == null || screenFolderValue.isEmpty()) {
            folder = dataDirectory.resolve("screenshots").toFile();
        } else {
            folder = new File(screenFolderValue);
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        // TODO FX setLocale

        audioManger = new AudioManager(config);

        if ("dark".equalsIgnoreCase(config.getTheme())) {
            theme = Theme.DARK;
        } else {
            theme = Theme.LIGHT;
        }

        // Swing legacy stuff
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            MenuFix.installGtkPopupBugWorkaround();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        theme.setUiMangerDefaults();


        resetWindowIcon();
        initWindowSize();
        primaryStage.setTitle(BASE_TITLE);

        rootPane = new BorderPane();
        rootPane.setTop(menuBar = new AppMenuBar(this));

        swingNode = new SwingNode();

        mountView(new StartView());

        primaryStage.setScene(new Scene(rootPane));
        primaryStage.show();
    }

    void resetWindowIcon() {
        primaryStage.getIcons().clear();
        primaryStage.getIcons().add(DEFAULT_ICON);
    }

    private void initWindowSize() {
        String windowSize = config.getDebug() == null ? null : config.getDebug().getWindow_size();
        if (System.getProperty("windowSize") != null) {
            windowSize = System.getProperty("windowSize");
        }
        if (windowSize == null || "fullscreen".equals(windowSize)) {
            primaryStage.setMaximized(true);
        } else if ("L".equals(windowSize) || "R".equals(windowSize)) {
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            int dw = (int) primScreenBounds.getWidth();
            int dh = (int) primScreenBounds.getHeight();
            primaryStage.setWidth(dw / 2);
            primaryStage.setHeight(dh - 40);
            primaryStage.setX("L".equals(windowSize) ? 0 : dw/2);
            primaryStage.setY(0);
        } else {
            String[] sizes = windowSize.split("x");
            if (sizes.length == 2) {
                UiUtils.centerStage(primaryStage, Integer.parseInt(sizes[0]), Integer.parseInt(sizes[1]));
            } else {
                logger.warn("Invalid configuration value for windows_size");
                primaryStage.setMaximized(true);
            }
        }
    }

    public void handleLoad() {
        // TODO FX
    }

    public void handleQuit() {
        if (getUiView().requestHide(null)) {
            System.exit(0);
        }
    }

    public void showPreferncesDialog() {
        // TODO FX
    }

    public void showAboutDialog() {
        // TODO FX
    }

    public void showTileDistribution() {
        // TODO FX
    }

    public void showHelpDialog() {
        // TODO FX
    }

    //TODO should be referenced from Controller
    public Connection getConnection() {
        return clientMessageListener == null ? null : clientMessageListener.getConnection();
    }

    public DiscardedTilesDialog getDiscardedTilesDialog() {
        return discardedTilesDialog;
    }

    public void setDiscardedTilesDialog(DiscardedTilesDialog discardedTilesDialog) {
        this.discardedTilesDialog = discardedTilesDialog;
    }



    public boolean mountView(UiView view) {
        if (this.view != null) {
            if (this.view.requestHide(view)) {
                this.view.hide(view);
            } else {
                return false;
            }
        }

        if (view instanceof SwingUiView) {
            rootPane.setCenter(swingNode);

            SwingUtilities.invokeLater(() -> {
                JPanel panel = new JPanel();
                swingNode.setContent(panel);
                ((SwingUiView) view).show(panel);
                this.view = view;
                logger.info("{} mounted", view.getClass().getSimpleName());
            });
        } else {
            try {
                rootPane.setCenter(((FxUiView) view).show());
                this.view = view;
                logger.info("{} mounted", view.getClass().getSimpleName());
            } catch (IOException e) {
                logger.error("Unable to mount view", e);
            }
        }
        return true;
    }

    public void onWebsocketError(Exception ex) {
        view.onWebsocketError(ex);
    }

    public void onWebsocketClose(int code, String reason, boolean remote) {
        view.onWebsocketClose(code, reason, remote);
    }

    public void onUnhandledWebsocketError(Exception ex) {
        String message;
        if (ex instanceof WebsocketNotConnectedException) {
            message = _tr("Connection lost");
        } else {
            message = ex.getMessage();
            if (message == null || message.length() == 0) {
                message = ex.getClass().getSimpleName();
            }
            logger.error(message, ex);
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(_tr("Error"));
        alert.setContentText(message);
        alert.showAndWait();
    }
}
