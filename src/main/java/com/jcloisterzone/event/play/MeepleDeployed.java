package com.jcloisterzone.event.play;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.DeployedMeeple;

public class MeepleDeployed extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private DeployedMeeple deployedMeeple;

    public MeepleDeployed(PlayEventMeta metadata, DeployedMeeple deployedMeeple) {
        super(metadata);
        this.deployedMeeple = deployedMeeple;
    }

    public DeployedMeeple getDeployedMeeple() {
        return deployedMeeple;
    }

    public Meeple getMeeple() {
        return deployedMeeple.getMeeple();
    }

    public Location getLocation() {
        return deployedMeeple.getFeaturePointer().getLocation();
    }


}
