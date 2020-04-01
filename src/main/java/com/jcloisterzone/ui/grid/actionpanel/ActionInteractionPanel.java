package com.jcloisterzone.ui.grid.actionpanel;

import java.awt.Font;

import javax.swing.JPanel;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.UiMixin;

public class ActionInteractionPanel<T extends PlayerAction<?>> extends JPanel implements UiMixin {

	public static Font FONT_HEADER = new Font(null, Font.BOLD, 18);

    protected final GameController gc;
    private GameState state;

    public ActionInteractionPanel(GameController gc) {
        super();
        this.gc = gc;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }

    public GameState getGameState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    @SuppressWarnings("unchecked")
    public T getAction() {
        return (T) state.getPlayerActions().getActions().get();
    }


}
