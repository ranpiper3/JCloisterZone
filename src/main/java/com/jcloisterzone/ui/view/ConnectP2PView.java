package com.jcloisterzone.ui.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.ConnectGamePanel;

public class ConnectP2PView extends AbstractSwingUiView {

    private ConnectGamePanel panel;

    @Override
    public void show(Container pane) {
        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new GridBagLayout()); //to have centered inner panel

        panel = new ConnectGamePanel();
        envelope.add(panel);

        pane.add(envelope, BorderLayout.CENTER);
    }

    @Override
    public void onWebsocketError(Exception ex) {
        panel.onWebsocketError(ex);
    }
}
