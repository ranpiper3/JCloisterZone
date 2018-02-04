package com.jcloisterzone.event.play;

import com.jcloisterzone.game.state.DeployedMeeple;

public class MeepleReturned extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private DeployedMeeple returnedMeeple;
    /** true if meeple is returned different way than scoring feature */
    private final boolean forced;

    public MeepleReturned(PlayEventMeta metadata, DeployedMeeple returnedMeeple, boolean forced) {
        super(metadata);
        this.returnedMeeple = returnedMeeple;
        this.forced = forced;
    }

    public DeployedMeeple getReturnedMeeple() {
        return returnedMeeple;
    }

    public boolean isForced() {
        return forced;
    }

}
