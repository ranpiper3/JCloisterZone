package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.ScoreFeatureReducer;
import com.jcloisterzone.game.state.DeployedMeeple;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class ScoreFarmBarn implements ScoreFeatureReducer {

    private final Farm farm;
    private final boolean isFinal;

    // "out" variable - computed owners are store to instance
    // to be available to reducer caller
    private Map<Player, Integer> playerPoints = HashMap.empty();

    public ScoreFarmBarn(Farm farm, boolean isFinal) {
        this.farm = farm;
        this.isFinal = isFinal;
    }

    @Override
    public Scoreable getFeature() {
        return farm;
    }

    @Override
    public GameState apply(GameState state) {
        Stream<DeployedMeeple> barns = farm.getDeployedSpecialMeeples(state)
            .filter(dm -> dm.getMeeple() instanceof Barn);

        int points = farm.getBarnPoints(state);
        PointCategory pointCategory = farm.getPointCategory();

        for (DeployedMeeple dm : barns) {
            Barn barn = (Barn) dm.getMeeple();
            state = (new AddPoints(barn.getPlayer(), points, pointCategory)).apply(state);
            playerPoints = playerPoints.put(barn.getPlayer(), points);

            ScoreEvent scoreEvent = new ScoreEvent(points, pointCategory, isFinal, dm.getFeaturePointer(), barn);
            state = state.appendEvent(scoreEvent);
        }

        return state;
    }

    @Override
    public Set<Player> getOwners() {
        return playerPoints.keySet();
    }

    @Override
    public int getFeaturePoints() {
        throw new UnsupportedOperationException("Call getFeaturePoints() with player argument");
    }

    @Override
    public int getFeaturePoints(Player player) {
        return playerPoints.getOrElse(player, 0);
    }
}
