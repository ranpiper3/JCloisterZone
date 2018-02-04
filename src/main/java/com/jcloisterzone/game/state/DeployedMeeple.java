package com.jcloisterzone.game.state;

import java.io.Serializable;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

/**
 * Represents a meeple that is placed on the board. This is made of a {@link Meeple},
 * a {@link FeaturePointer} and a role. */
@Immutable
public class DeployedMeeple implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Meeple meeple;
    private final FeaturePointer featurePointer;
    private final String role;

    public DeployedMeeple(Meeple meeple, FeaturePointer featurePointer) {
        this(meeple, featurePointer, null);
    }

    public DeployedMeeple(Meeple meeple, FeaturePointer featurePointer, String role) {
        this.meeple = meeple;
        this.featurePointer = featurePointer;
        this.role = role;
    }

    public Meeple getMeeple() {
        return meeple;
    }

    public FeaturePointer getFeaturePointer() {
        return featurePointer;
    }

    public String getRole() {
        return role;
    }
}
