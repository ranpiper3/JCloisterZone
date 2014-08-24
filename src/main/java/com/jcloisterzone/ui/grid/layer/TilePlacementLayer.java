package com.jcloisterzone.ui.grid.layer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Set;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;

public class TilePlacementLayer extends AbstractTilePlacementLayer implements ActionLayer<TilePlacementAction> {

    private TilePlacementAction action;

    private Rotation realRotation;
    private Rotation previewRotation;
    private boolean allowedRotation;

    public TilePlacementLayer(GridPanel gridPanel) {
        super(gridPanel);
    }

    @Override
    public void setAction(TilePlacementAction action) {
        this.action = action;
        if (action == null) {
            setAvailablePositions(null);
            realRotation = null;
        } else {
            setAvailablePositions(action.groupByPosition().keySet());
        };
    }

    @Override
    public TilePlacementAction getAction() {
        return null;
    }

    @Override
    protected Image createPreviewIcon() {
        return getClient().getResourceManager().getTileImage(action.getTile());
    }

    @Override
    protected void drawPreviewIcon(Graphics2D g2, Image previewIcon, Position previewPosition) {
        if (realRotation != action.getTileRotation()) {
            preparePreviewRotation(previewPosition);
        }
        Composite compositeBackup = g2.getComposite();
        g2.setComposite(allowedRotation ? ALLOWED_PREVIEW : DISALLOWED_PREVIEW);
        g2.drawImage(previewIcon, getAffineTransform(previewIcon.getWidth(null), previewPosition, previewRotation), null);
        g2.setComposite(compositeBackup);
    }

    private void preparePreviewRotation(Position p) {
        realRotation = action.getTileRotation();
        previewRotation = realRotation;

        Set<Rotation> allowedRotations = action.getRotations(p);
        if (allowedRotations.contains(previewRotation)) {
            allowedRotation = true;
        } else {
            if (allowedRotations.size() == 1) {
                previewRotation = allowedRotations.iterator().next();
                allowedRotation = true;
            } else if (action.getTile().getSymmetry() == TileSymmetry.S2) {
                previewRotation = realRotation.next();
                allowedRotation = true;
            } else {
                allowedRotation = false;
            }
        }
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        realRotation = null;
        super.squareEntered(e, p);
    }

    @Override
    public void squareExited(MouseEvent e, Position p) {
        realRotation = null;
        super.squareExited(e, p);
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (getPreviewPosition() != null && getClient().isClientActive() && allowedRotation) {
                e.consume();
                action.perform(getClient().getServer(), new TilePlacement(p, previewRotation));
            }
        }
    }

}
