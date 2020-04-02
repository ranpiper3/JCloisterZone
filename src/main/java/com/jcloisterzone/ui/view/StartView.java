package com.jcloisterzone.ui.view;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jcloisterzone.AppUpdate;
import com.jcloisterzone.ui.FxClient;
import com.jcloisterzone.ui.AppMenuBar;
import com.jcloisterzone.ui.AppMenuBar.MenuItemDef;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.HelpPanel;
import com.jcloisterzone.ui.panel.StartPanel;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class StartView implements FxUiView {

    private StartPanel startPanel;

    @Override
    public Node show() {
        AppMenuBar menu = FxClient.getInstance().getMenuBar();
        menu.setItemEnabled(MenuItemDef.DISCONNECT, false);
        menu.setItemEnabled(MenuItemDef.SAVE, false);
        menu.setItemEnabled(MenuItemDef.LOAD, true);
        menu.setItemEnabled(MenuItemDef.NEW_GAME, true);
        menu.setItemEnabled(MenuItemDef.PLAY_ONLINE, true);
        menu.setItemEnabled(MenuItemDef.CONNECT_P2P, true);

        startPanel = new StartPanel();
        return startPanel;
    }

//    public void show(Container pane) {
//        pane.setLayout(new BorderLayout()); //TODO should be this line in client init?
//        JPanel envelope = new BackgroundPanel(new GridBagLayout());
//        envelope.setBackground(getTheme().getMainBg());
//        pane.add(envelope, BorderLayout.CENTER);
//
//        AppMenuBar menu = FxClient.getInstance().getMenuBar();
//        menu.setItemEnabled(MenuItemDef.DISCONNECT, false);
//        menu.setItemEnabled(MenuItemDef.SAVE, false);
//        menu.setItemEnabled(MenuItemDef.LOAD, true);
//        menu.setItemEnabled(MenuItemDef.NEW_GAME, true);
//        menu.setItemEnabled(MenuItemDef.PLAY_ONLINE, true);
//        menu.setItemEnabled(MenuItemDef.CONNECT_P2P, true);
//
//        startPanel = new StartPanel();
//        envelope.add(startPanel);
//    }


    public void showUpdateIsAvailable(final AppUpdate appUpdate) {
        // TODO FX show update info
//        Color bg = new Color(0.2f, 1.0f, 0.0f, 0.1f);
//        HelpPanel hp = startPanel.getHelpPanel();
//        hp.removeAll();
//        hp.setOpaque(true);
//        hp.setBackground(bg);
//        Font font = new Font(null, Font.BOLD, 14);
//        JLabel label;
//        label = new JLabel(_tr("JCloisterZone " + appUpdate.getVersion() + " is available for download."));
//        label.setFont(font);
//        hp.add(label, "wrap");
//        label = new JLabel(appUpdate.getDescription());
//        hp.add(label, "wrap");
//
//        final JTextField link = new JTextField(appUpdate.getDownloadUrl());
//        link.setEditable(false);
//        link.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
//        link.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                link.setSelectionStart(0);
//                link.setSelectionEnd(link.getText().length());
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                link.setSelectionStart(0);
//                link.setSelectionEnd(0);
//            }
//        });
//
//        hp.add(link, "wrap, growx");
//        hp.repaint();
    }

}
