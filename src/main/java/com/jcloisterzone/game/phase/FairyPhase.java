package com.jcloisterzone.game.phase;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.RandomGenerator;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.state.DeployedMeeple;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;


@RequiredCapability(FairyCapability.class)
public class FairyPhase extends Phase {

    public FairyPhase(RandomGenerator random) {
        super(random);
    }

    @Override
    public StepResult enter(GameState state) {
        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();
        if (ptr == null) {
            return next(state);
        }

        boolean onTileRule = ptr instanceof Position;
        FeaturePointer fairyFp = ptr.asFeaturePointer();

        for (DeployedMeeple dm : state.getDeployedMeeplesX()) {
            Meeple m = dm.getMeeple();
            if (!m.getPlayer().equals(state.getTurnPlayer())) continue;
            if (!dm.getFeaturePointer().equals(fairyFp)) continue;

            if (!onTileRule) {
                if (!((MeeplePointer) ptr).getMeepleId().equals(m.getId())) continue;
            }

            state = new AddPoints(
                m.getPlayer(),
                FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN,
                PointCategory.FAIRY
            ).apply(state);

            state = state.appendEvent(new ScoreEvent(
                FairyCapability.FAIRY_POINTS_BEGINNING_OF_TURN, PointCategory.FAIRY,
                false, fairyFp, m
            ));
        }

        return next(state);
    }
}
