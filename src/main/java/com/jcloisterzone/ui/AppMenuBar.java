package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.util.EnumMap;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.ui.view.ConnectP2PView;
import com.jcloisterzone.ui.view.ConnectPlayOnlineView;
@SuppressWarnings("serial")
public class AppMenuBar extends MenuBar {

    public static enum MenuItemDef {
        //Session
        NEW_GAME(_tr("New game"), new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)),
        CONNECT_P2P(_tr("Connect"), new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)),
        PLAY_ONLINE(_tr("Play Online"), new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN)),
        DISCONNECT(_tr("Disconnect")),
        LEAVE_GAME(_tr("Leave Game")),
        SAVE(_tr("Save Game"), new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)),
        LOAD(_tr("Load Game"), new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN)),
        QUIT(_tr("Quit"), new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)),
        //Game
        UNDO(_tr("Undo"), new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN)),
        ZOOM_IN(_tr("Zoom In"), new KeyCodeCombination(KeyCode.PLUS)),
        ZOOM_OUT(_tr("Zoom Out"), new KeyCodeCombination(KeyCode.MINUS)),
        ROTATE_BOARD(_tr("Rotate Board"), new KeyCodeCombination(KeyCode.DIVIDE)),
        GAME_EVENTS(_tr("Show Game Events"), new KeyCodeCombination(KeyCode.E)),
        LAST_PLACEMENTS(_tr("Show Last Placements"), new KeyCodeCombination(KeyCode.X)),
        FARM_HINTS(_tr("Show Farm Hints"), new KeyCodeCombination(KeyCode.F)),
        PROJECTED_POINTS(_tr("Show Projected Points"), new KeyCodeCombination(KeyCode.P)),
        DISCARDED_TILES(_tr("Show Discarded Tiles"), new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN)),
        GAME_SETUP(_tr("Show Game Setup")),
        TAKE_SCREENSHOT(_tr("Take Screenshot"), new KeyCodeCombination(KeyCode.F12)),
        //Settings
        BEEP_ALERT(_tr("Beep Alert at Player Turn")),
        CONFIRM_ANY_DEPLOYMENT(_tr("Confirm Every Meeple Deployment")),
        CONFIRM_FARM_DEPLOYMENT(_tr("Confirm Meeple Deployment on a Farm")),
        CONFIRM_TOWER_DEPLOYMENT(_tr("Confirm Meeple Deployment on a Tower")),
        CONFIRM_RANSOM(_tr("Confirm Ransom Payment")),
        PREFERENCES(_tr("Preferences"), new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN)),
        //Help
        ABOUT(_tr("About")),
        TILE_DISTRIBUTION(_tr("Tile Distribution"), new KeyCodeCombination(KeyCode.F11)),
        CONTROLS(_tr("Controls")),
        REPORT_BUG(_tr("Report Bug"));

        String title;
        KeyCombination accelerator;

        MenuItemDef(String title) {
            this(title, null);
        };

        MenuItemDef(String title, KeyCombination accelerator) {
            this.title = title;
            this.accelerator = accelerator;
        }
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private EnumMap<MenuItemDef, MenuItem> items = new EnumMap<>(MenuItemDef.class);

    public AppMenuBar(FxClient client) {

        boolean isMac = JCloisterZone.isMac();
        setUseSystemMenuBar(true);

        Menu menu;

        menu = new Menu(_tr("Session"));
        menu.getItems().addAll(
                createMenuItem(MenuItemDef.NEW_GAME, e -> { client.createGame(); }),
                createMenuItem(MenuItemDef.CONNECT_P2P, e -> { client.mountView(new ConnectP2PView()); }),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.PLAY_ONLINE, e -> { client.mountView(new ConnectPlayOnlineView()); }),
                createMenuItem(MenuItemDef.DISCONNECT, false),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.LEAVE_GAME, false),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.SAVE, false),
                createMenuItem(MenuItemDef.LOAD, e -> { client.handleLoad(); })
        );
        if (!isMac) {
            menu.getItems().addAll(
                    new SeparatorMenuItem(),
                    createMenuItem(MenuItemDef.QUIT, e -> { client.handleQuit(); })
            );
        }
        getMenus().add(menu);

        menu = new Menu(_tr("Game"));
        menu.getItems().addAll(
                createMenuItem(MenuItemDef.UNDO, false),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.ZOOM_IN, false),
                createMenuItem(MenuItemDef.ZOOM_OUT, false),
                createMenuItem(MenuItemDef.ROTATE_BOARD, false),
                new SeparatorMenuItem(),
                createCheckBoxMenuItem(MenuItemDef.GAME_EVENTS, false, true),
                createCheckBoxMenuItem(MenuItemDef.LAST_PLACEMENTS, false, false),
                createCheckBoxMenuItem(MenuItemDef.FARM_HINTS, false, false),
                createCheckBoxMenuItem(MenuItemDef.PROJECTED_POINTS, false, false),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.DISCARDED_TILES, false),
                createMenuItem(MenuItemDef.GAME_SETUP, false),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.TAKE_SCREENSHOT, false));
        getMenus().add(menu);

        menu = new Menu(_tr("Settings"));
        menu.getItems().addAll(
                createCheckBoxMenuItem(MenuItemDef.BEEP_ALERT, e -> {
                    CheckMenuItem ch = (CheckMenuItem) e.getSource();
                    client.getConfig().setBeep_alert(ch.isSelected());
                    client.saveConfig();
                }, true, client.getConfig().getBeep_alert()),
                new SeparatorMenuItem(),
                createCheckBoxMenuItem(MenuItemDef.CONFIRM_ANY_DEPLOYMENT, e -> {
                    CheckMenuItem ch = (CheckMenuItem) e.getSource();
                    client.getConfig().getConfirm().setAny_deployment(ch.isSelected());
                    client.saveConfig();
                }, true, client.getConfig().getConfirm().getAny_deployment()),
                createCheckBoxMenuItem(MenuItemDef.CONFIRM_FARM_DEPLOYMENT, e -> {
                    CheckMenuItem ch = (CheckMenuItem) e.getSource();
                    client.getConfig().getConfirm().setFarm_deployment(ch.isSelected());
                    client.saveConfig();
                }, true, client.getConfig().getConfirm().getFarm_deployment()),
                createCheckBoxMenuItem(MenuItemDef.CONFIRM_TOWER_DEPLOYMENT, e -> {
                    CheckMenuItem ch = (CheckMenuItem) e.getSource();
                    client.getConfig().getConfirm().setOn_tower_deployment(ch.isSelected());
                    client.saveConfig();
                }, true, client.getConfig().getConfirm().getOn_tower_deployment()),
                new SeparatorMenuItem(),
                createCheckBoxMenuItem(MenuItemDef.CONFIRM_RANSOM, e -> {
                    CheckMenuItem ch = (CheckMenuItem) e.getSource();
                    client.getConfig().getConfirm().setRansom_payment(ch.isSelected());
                    client.saveConfig();
                }, true, client.getConfig().getConfirm().getRansom_payment()),
                new SeparatorMenuItem(),
                createMenuItem(MenuItemDef.PREFERENCES, e -> { client.showPreferncesDialog(); }));
        getMenus().add(menu);

        menu = new Menu(_tr("Help"));
        menu.getItems().addAll(
                createMenuItem(MenuItemDef.ABOUT, e -> { client.showAboutDialog(); }),
                createMenuItem(MenuItemDef.TILE_DISTRIBUTION, e -> { client.showTileDistribution(); }),
                createMenuItem(MenuItemDef.CONTROLS, e -> { client.showHelpDialog(); }),
                createMenuItem(MenuItemDef.REPORT_BUG, false));
        getMenus().add(menu);
    }

    private MenuItem createMenuItem(MenuItemDef def, boolean enabled) {
        return createMenuItem(def, null, enabled);
    }

    private MenuItem createMenuItem(MenuItemDef def, EventHandler<ActionEvent> handler) {
        return createMenuItem(def, handler, true);
    }

    private MenuItem createMenuItem(MenuItemDef def, EventHandler<ActionEvent> handler, boolean enabled) {
        MenuItem instance = new MenuItem(def.title);
        initMenuItem(instance, def, handler);
        instance.setDisable(!enabled);
        return instance;
    }

    private CheckMenuItem createCheckBoxMenuItem(MenuItemDef def, boolean enabled, boolean selected) {
        return createCheckBoxMenuItem(def, null, enabled, selected);
    }

    private CheckMenuItem createCheckBoxMenuItem(MenuItemDef def, EventHandler<ActionEvent> handler) {
        return createCheckBoxMenuItem(def, handler, true, false);
    }

    private CheckMenuItem createCheckBoxMenuItem(MenuItemDef def, EventHandler<ActionEvent> handler, boolean enabled, boolean selected) {
        CheckMenuItem instance = new CheckMenuItem(def.title);
        initMenuItem(instance, def, handler);
        instance.setDisable(!enabled);
        instance.setSelected(selected);
        return instance;
    }

    private void initMenuItem(MenuItem instance, MenuItemDef def, EventHandler<ActionEvent> handler) {
        if (def.accelerator != null) {
            instance.setAccelerator(def.accelerator);
        }
        if (handler != null) {
            instance.setOnAction(handler);
        }
        items.put(def, instance);
    }

    public void setItemEnabled(MenuItemDef item, boolean state) {
        items.get(item).setDisable(!state);
    }

    public void setItemActionListener(MenuItemDef item, EventHandler<ActionEvent> handler) {
        MenuItem instance = items.get(item);
        instance.setOnAction(handler);
    }

    public boolean isSelected(MenuItemDef item) {
        CheckMenuItem instance = (CheckMenuItem) items.get(item);
        return instance.isSelected();
    }
}
