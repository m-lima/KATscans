package no.uib.inf252.katscan.view.project;

import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.List;
import no.uib.inf252.katscan.project.DataFileNode;
import no.uib.inf252.katscan.project.KatNode;
import no.uib.inf252.katscan.project.KatViewNode;
import no.uib.inf252.katscan.project.ProjectHandler;
import no.uib.inf252.katscan.project.ProjectNode;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.view.component.draggable.DraggableHandler;
import no.uib.inf252.katscan.view.component.draggable.DraggableItem;

/**
 *
 * @author Marcelo Lima
 */
public class ProjectDraggableHandler extends DraggableHandler<KatNode> {

    @Override
    public DraggableItem prepareOutgoingItems(DragGestureEvent evt, List<KatNode> selectedValues, BufferedImage ghostImage, Point offset) {
        KatNode node = selectedValues.get(0);
        if (node instanceof ProjectNode || node instanceof DataFileNode) {
            return null;
        }
        return super.prepareOutgoingItems(evt, selectedValues, ghostImage, offset); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean performIncomingDrop(DropTargetDropEvent evt, List<KatNode> incomingObjects, KatNode targetObject) {
        if (!(targetObject instanceof Displayable)) {
            return false;
        } 
        
        KatNode node = incomingObjects.get(0);

        if (node.getParent().equals(targetObject)) {
            return false;
        }

        ProjectHandler project = ProjectHandler.getInstance();
        if (node instanceof KatViewNode) {
            KatViewNode katViewNode = (KatViewNode) node;
            katViewNode.getView().close();
            node = KatViewNode.buildKatView(katViewNode.getType(), (Displayable) targetObject);
            ((KatViewNode)node).getKatView().setTrackBall(katViewNode.getKatView().getTrackBall());
        } else {
            project.removeNodeFromParent(node);
        }
        project.insertNodeInto(node, targetObject, targetObject.getChildCount());
        return true;
    }

}
