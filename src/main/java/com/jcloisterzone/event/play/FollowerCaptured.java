package com.jcloisterzone.event.play;

import com.jcloisterzone.game.state.DeployedMeeple;

public class FollowerCaptured extends PlayEvent {

    private static final long serialVersionUID = 1L;

    private DeployedMeeple capturedFollower;

    public FollowerCaptured(PlayEventMeta metadata, DeployedMeeple capturedFollower) {
        super(metadata);
        this.capturedFollower = capturedFollower;
    }

    public DeployedMeeple getCapturedFollower() {
        return capturedFollower;
    }
}
