package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.MeepleReturned;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Structure;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.game.state.DeployedMeeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Stream;

public class UndeployMeeple implements Reducer {

    private final Meeple meeple;
    /** true if meeple is returned different way than scoring feature */
    private final boolean forced;

    public UndeployMeeple(Meeple meeple, boolean forced) {
        this.meeple = meeple;
        this.forced = forced;
    }

    @Override
    public GameState apply(GameState state) {
        DeployedMeeple deployedMeeple = state.getDeployedMeeples().get(meeple).get();
        PlayEventMeta metaWithPlayer = PlayEventMeta.createWithActivePlayer(state);
        state = primaryUndeploy(state, metaWithPlayer, deployedMeeple);
        Player owner = meeple.getPlayer();

        // Undeploy lonely Builders and Pigs
        PlayEventMeta metaNoPlayer = PlayEventMeta.createWithoutPlayer();
        Structure feature = state.getStructure(deployedMeeple.getFeaturePointer());
        Stream<DeployedMeeple> threatened = feature.getDeployedMeeples(state)
            .filter(dm -> (dm.getMeeple() instanceof Pig) || (dm.getMeeple() instanceof Builder))
            .filter(dm -> dm.getMeeple().getPlayer().equals(owner));

        for (DeployedMeeple dm : threatened) {
            if (feature.getFollowers(state).find(f -> f.getPlayer().equals(owner)).isEmpty()) {
                state = undeploy(state, metaNoPlayer, dm);
            }
        }

        return state;
    }

    protected GameState primaryUndeploy(GameState state, PlayEventMeta meta, DeployedMeeple deployedMeeple) {
        return undeploy(state, meta, deployedMeeple);
    }

    private GameState undeploy(GameState state, PlayEventMeta meta, DeployedMeeple deployedMeeple) {
        LinkedHashMap<Meeple, DeployedMeeple> deployedMeeples = state.getDeployedMeeples();
        state = state.setDeployedMeeples(deployedMeeples.remove(deployedMeeple.getMeeple()));
        state = state.appendEvent(
            new MeepleReturned(meta, deployedMeeple, forced)
        );
        return state;
    }

    public boolean isForced() {
        return forced;
    }

}
