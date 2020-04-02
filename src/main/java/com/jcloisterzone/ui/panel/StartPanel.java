package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.FxClient;
import com.jcloisterzone.ui.UiMixin;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.ui.view.ConnectP2PView;
import com.jcloisterzone.ui.view.ConnectPlayOnlineView;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.miginfocom.swing.MigLayout;
import org.tbee.javafx.scene.layout.MigPane;

public class StartPanel extends MigPane implements UiMixin {

    static Font FONT_LARGE_BUTTON = new Font(null, Font.PLAIN, 25);

    private HelpPanel helpPanel;

    /**
     * Create the panel.
     */
    public StartPanel() {
        super("", "[center,grow]20[center,grow]", "[]20[]10[]");
//        if (!getTheme().isDark()) { //HACK
//            setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
//        }


        Label lblNewLabel = new Label();
        InputStream imageResource;
        if (getTheme().isDark()) {
            imageResource = getClass().getResourceAsStream("/sysimages/jcloisterzone-dark.png");
        } else {
            imageResource = getClass().getResourceAsStream("/sysimages/jcloisterzone.png");
        }
        lblNewLabel.setGraphic(new ImageView(new Image(imageResource)));

        add(lblNewLabel, "span 2, wrap, center");
        helpPanel = new HelpPanel();
        add(helpPanel, "span 2, wrap, grow, gap 30 30");

        MigPane playHostedPanel = new MigPane("", "[grow,center]", "20[40px]20[grow]");
//        if (!getTheme().isDark()) { //HACK
//            playHostedPanel.setBorder(new TitledBorder(
//                UIManager.getBorder("TitledBorder.border"),  "", TitledBorder.LEADING,
//                TitledBorder.TOP, null, new Color(0, 0, 0)));
//        }

        add(playHostedPanel, "grow 2, width :500:");

        playHostedPanel.add(new Label(
          _tr("Create a new game or continue a previously saved one. A game will be hosted on your computer and other players may connect during game set up. " +
            "You can also play only against any number of computer players.")),
            "wrap, grow");


        MigPane btnPanel = new MigPane("", "[]30[]30[]", "[]");
        playHostedPanel.add(btnPanel, "wrap");

        Button btn = new Button(_tr("New game"));
        btnPanel.add(btn, "aligny top");
        btn.setOnAction(e -> { FxClient.getInstance().createGame(); });
        // btn.setFont(FONT_LARGE_BUTTON);

        btnPanel.add(new Label(_tr("or")));

        btn = new Button(_tr("Load game"));
        btnPanel.add(btn, "aligny top");
        btn.setOnAction(e -> { FxClient.getInstance().handleLoad(); });
        // btn.setFont(FONT_LARGE_BUTTON);

        playHostedPanel.add(new Label(
                _tr("You can also connect to remote JCloisterZone application hosting a game. Connect when game is created but not yet started.")),
            "wrap, grow, gaptop 15");

        btn = new Button(_tr("Connect"));
        playHostedPanel.add(btn, "wrap, alignx center,aligny top");
        btn.setOnAction(e -> { mountView(new ConnectP2PView()); });
        // btn.setFont(FONT_LARGE_BUTTON);

        MigPane playOnlinePanel = new MigPane("", "[grow,center]", "20[40px]20[grow]");
//        if (!getTheme().isDark()) { //HACK
//            playOnlinePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
//        }
        add(playOnlinePanel, "grow, width :250:, wrap");

        playOnlinePanel.add(new Label(
          _tr("Connect to other players and play with them using internet connection and public game server play.jcloisterzone.com.")), "wrap, grow");

        btn = new Button(_tr("Play online"));
        playOnlinePanel.add(btn, "wrap, alignx center, aligny top");
        btn.setOnAction(e -> { mountView(new ConnectPlayOnlineView()); });
        //btn.setFont(FONT_LARGE_BUTTON);

    }

    public HelpPanel getHelpPanel() {
        return helpPanel;
    }

}
