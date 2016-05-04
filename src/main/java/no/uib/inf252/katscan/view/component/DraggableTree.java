package no.uib.inf252.katscan.view.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import no.uib.inf252.katscan.project.DataFileNode;
import no.uib.inf252.katscan.project.KatNode;
import no.uib.inf252.katscan.project.ProjectHandler;
import no.uib.inf252.katscan.project.ProjectNode;
import no.uib.inf252.katscan.project.displayable.Displayable;

/**
 *
 * @see DraggableList
 * @see DraggableItem
 *
 * @author Marcelo
 */
public class DraggableTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener {

    public static final DataFlavor LOCAL_OBJECT_FLAVOR;
    public static final DataFlavor[] SUPPORTED_FLAVORS;

    static {
        DataFlavor initialFlavor = null;
        try {
            initialFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException cnfe) {
            Logger.getLogger(DraggableTree.class.getName()).log(Level.SEVERE, null, cnfe);
        }
        LOCAL_OBJECT_FLAVOR = initialFlavor;
        SUPPORTED_FLAVORS = new DataFlavor[]{LOCAL_OBJECT_FLAVOR, DraggableTree.LOCAL_OBJECT_FLAVOR};
    }
    
    private static final Color INVALID_COLOR = new Color(150, 0, 0);

    private final DragSource dragSource;

    private TreePath path;
    private KatNode incomingNode = null;
    private KatNode targetNode = null;

    private Rectangle dropLine;
    private Rectangle oldDropLine;
    private Rectangle targetBounds;

    public DraggableTree() {
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        new DropTarget(this, this);
    }

    public void finishDnD() {
        incomingNode = null;
        targetNode = null;

        dropLine = null;
        oldDropLine = null;
        targetBounds = null;

        repaint();
    }
    
    private boolean checkDropValid() {
        if (targetNode == null) {
            return false;
        }
        
        if (incomingNode == null) {
            return false;
        }
        
        if (targetNode == incomingNode) {
            return false;
        }
        
        if (!(targetNode instanceof Displayable)) {
            return false;
        }
        
        if (incomingNode.getParent().equals(targetNode)) {
            return false;
        }
        
        KatNode parent = targetNode;
        while((parent = parent.getParent()) != null) {
            if (parent.equals(incomingNode)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath selectedPath = getSelectionPath();
        if (selectedPath == null) {
            finishDnD();
            return;
        }

        KatNode node = (KatNode) selectedPath.getLastPathComponent();
        if (node == null || node instanceof ProjectNode || node instanceof DataFileNode) {
            finishDnD();
            return;
        }

        dragSource.startDrag(dge, DragSource.DefaultMoveDrop, node, this);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(LOCAL_OBJECT_FLAVOR)) {

            try {
                Transferable transferable = dtde.getTransferable();
                incomingNode = (KatNode) transferable.getTransferData(LOCAL_OBJECT_FLAVOR);

                if (incomingNode == null) {
                    dtde.rejectDrag();
                    finishDnD();
                } else {
                    dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                }
            } catch (Exception ex) {
                Logger.getLogger(DraggableTree.class.getName()).log(Level.SEVERE, null, ex);
                dtde.rejectDrag();
                finishDnD();
            }
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        Point dragPoint = dtde.getLocation();
        TreePath newPath = getClosestPathForLocation(dragPoint.x, dragPoint.y);
        
        if (newPath == null && path == null) {
            return;
        }
        
        if (newPath.equals(path)) {
            return;
        }
        
        path = newPath;

        scrollRowToVisible(getRowForPath(path) + 1);
        scrollRowToVisible(getRowForPath(path) - 1);

        if (path == null || incomingNode == null) {
            targetNode = null;
        } else {
            expandPath(path);
            targetNode = (KatNode) path.getLastPathComponent();
        }

        if (targetNode == null) {
            dropLine = null;

            if (oldDropLine != null) {
                repaint(oldDropLine);
            }
            oldDropLine = null;

        } else {
            targetBounds = getPathBounds(path);

            if (targetBounds == null) {
                targetBounds = new Rectangle();
            }

            if (dropLine == null) {
                dropLine = new Rectangle();
            }

            if (oldDropLine == null) {
                oldDropLine = new Rectangle();
            }

            oldDropLine.setRect(dropLine);
            dropLine.setRect(0, targetBounds.y + (int) targetBounds.getHeight(), getWidth(), 2);

            repaint(oldDropLine);
            repaint(dropLine);
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (checkDropValid()) {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            
            ProjectHandler project = ProjectHandler.getInstance();
            project.removeNodeFromParent(incomingNode);
            project.insertNodeInto(incomingNode, targetNode, 0);

            expandPath(path);
        } else {
            dtde.rejectDrop();
        }
        
        finishDnD();
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        finishDnD();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        if (dropLine != null) {
            g2d.setColor(checkDropValid() ? Color.GRAY : INVALID_COLOR);
            g2d.fill(dropLine);
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        dropLine = null;
        repaint();
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {}

    @Override
    public void dragExit(DragSourceEvent dse) {}

    @Override
    public void dragOver(DragSourceDragEvent dsde) {}

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {}

}
