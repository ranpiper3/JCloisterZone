package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.AppMenuBar;
import com.jcloisterzone.ui.AppMenuBar.MenuItemDef;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.panel.ChannelPanel;

public class ChannelView extends AbstractSwingUiView {

    private final ChannelController cc;

    private ChannelPanel channelPanel;
    private ChatPanel chatPanel;

    public ChannelView(ChannelController cc) {
        this.cc = cc;
    }

    @Override
    public void show(Container pane) {
        channelPanel = new ChannelPanel(cc);
        pane.add(channelPanel);

        chatPanel = channelPanel.getChatPanel();
        cc.setChannelPanel(channelPanel);

        registerChildComponents(channelPanel, cc);

        AppMenuBar menu = getMenuBar();
        menu.setItemActionListener(MenuItemDef.DISCONNECT, e -> {
            cc.getConnection().close();
            mountView(new StartView());
        });
        menu.setItemEnabled(MenuItemDef.LOAD, false);
        menu.setItemEnabled(MenuItemDef.NEW_GAME, false);
        menu.setItemEnabled(MenuItemDef.CONNECT_P2P, false);
        menu.setItemEnabled(MenuItemDef.PLAY_ONLINE, false);
        menu.setItemEnabled(MenuItemDef.DISCONNECT, true);
    }

    @Override
    public void hide(UiView nextView) {
        unregisterChildComponents(channelPanel, cc);

        AppMenuBar menu = getMenuBar();
        if (nextView instanceof StartView) {
            menu.setItemEnabled(MenuItemDef.DISCONNECT, false);
            menu.setItemEnabled(MenuItemDef.LOAD, true);
            menu.setItemEnabled(MenuItemDef.NEW_GAME, true);
            menu.setItemEnabled(MenuItemDef.CONNECT_P2P, true);
            menu.setItemEnabled(MenuItemDef.PLAY_ONLINE, true);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (chatPanel.getInput().hasFocus()) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyChar() == '`' || e.getKeyChar() == ';') {
                e.consume();
                chatPanel.activateChat();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        mountView(new StartView());
    }
}
