package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.DeploymentCheckResult;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.DeployedMeeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.LinkedHashMap;

public class DeployMeeple implements Reducer {

    private final Meeple meeple;
    private final FeaturePointer fp;
    private final String role;

    public DeployMeeple(Meeple meeple, FeaturePointer fp) {
        this(meeple, fp, null);
    }

    public DeployMeeple(Meeple meeple, FeaturePointer fp, String role) {
        this.meeple = meeple;
        this.fp = fp;
        this.role = role;
    }

    @Override
    public GameState apply(GameState state) {
        Structure feature = state.getStructure(fp);
        if (feature == null) {
            throw new IllegalArgumentException("There is no feature on " + fp);
        }

        DeploymentCheckResult check = meeple.isDeploymentAllowed(state, fp, feature);
        if (!check.result) {
            throw new IllegalArgumentException(check.error);
        }

        LinkedHashMap<Meeple, DeployedMeeple> deployedMeeples = state.getDeployedMeeples();
        DeployedMeeple dm = new DeployedMeeple(meeple, fp, role);
        state = state.setDeployedMeeples(deployedMeeples.put(meeple, dm));
        state = state.appendEvent(
            new MeepleDeployed(PlayEventMeta.createWithActivePlayer(state), dm)
        );
        return state;
    }

}
