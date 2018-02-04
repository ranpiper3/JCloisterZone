package com.jcloisterzone.reducers;

import java.util.ArrayList;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.MeepleReturned;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.state.DeployedMeeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


/**
 * Undeploys all meeples except Barn! Barns are never returned.
 */
public class UndeployMeeples implements Reducer {

    private final Feature feature;
    /** true if meeple is returned different way than scoring feature */
    private final boolean forced;

    public UndeployMeeples(Feature feature, boolean forced) {
        this.feature = feature;
        this.forced = forced;
    }

    @Override
    public GameState apply(GameState state) {
        Set<FeaturePointer> fps = HashSet.ofAll(feature.getPlaces());
        ArrayList<Meeple> meeples = new ArrayList<>();
        ArrayList<PlayEvent> events = new ArrayList<>();
        PlayEventMeta eventMeta = PlayEventMeta.createWithoutPlayer();

        for (DeployedMeeple dm : state
                .getDeployedMeeplesX()
                .filter(dm -> fps.contains(dm.getFeaturePointer()))
                .filter(dm -> !(dm.getMeeple() instanceof Barn))
            ) {
            meeples.add(dm.getMeeple());
            events.add(
                new MeepleReturned(eventMeta, dm, forced)
            );
        }
        state = state.setDeployedMeeples(
            state.getDeployedMeeples().removeAll(meeples)
        );
        state = state.setEvents(
            state.getEvents().appendAll(events)
        );

        return state;
    }

    public boolean isForced() {
        return forced;
    }

}
