package com.jcloisterzone.ui.panel;

import javafx.scene.control.Label;
import org.tbee.javafx.scene.layout.MigPane;

import static com.jcloisterzone.ui.I18nUtils._tr;

@Deprecated
public class HelpPanel extends MigPane {

    public HelpPanel() {
        super("", "[grow]", "[]10[]10[]10[]");
        Label paragraph;

        paragraph = new Label(_tr("<html>Use <b>A</b>, <b>S</b>, <b>W</b>, <b>D</b> or <b>cursor</b> keys to scroll the board. " +
                "Click with <b>middle</b> mouse button to center the board. " +
                "Zoom with <b>+</b> or <b>-</b> keys or use mouse <b>wheel</b>.</html>"));
        add(paragraph, "cell 0 0,grow");

        paragraph = new Label(_tr("<html>Rotate a tile by <b>Tab</b> key or <b>right</b> mouse click. Place it by <b>left</b> click.</html>"));
        add(paragraph, "cell 0 1,grow");

        paragraph = new Label(_tr("<html>When tile is placed use <b>Tab</b> or <b>right</b> click again to "+
                        "select appropriate action.</html>"));
        add(paragraph, "cell 0 2,grow");

        paragraph = new Label(_tr("<html>Alternativelly you can pass with <b>Enter</b> or <b>Space</b> to play no action.</html>"));
        add(paragraph, "cell 0 3,grow");

        paragraph = new Label(_tr("<html>Press <b>F</b> to toggle farm hints or <b>X</b> to see last placed tiles.</html>"));
        add(paragraph, "cell 0 4,grow");
    }

}
