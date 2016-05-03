package no.uib.inf252.katscan.view.component.draggable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Uma lista que suporta acoes de <i>Drag & Drop</i>.
 *
 * @see DraggableTree
 * @see DraggableItem
 *
 * @author Marcelo
 */
public class DraggableList extends JList implements DragSourceListener, DropTargetListener, DragGestureListener, ListSelectionListener, MouseListener {

    //Informa o tipo de dados que essa lista eh e com qual tipos trabalha
    public static final DataFlavor LOCAL_OBJECT_FLAVOR;
    public static final DataFlavor[] SUPPORTED_FLAVORS;

    //Inicializa as variaveis de tipo de dados arrastaveis por essa lista
    static {
        DataFlavor initialFlavor = null;
        try {
            initialFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException cnfe) {
            Logger.getLogger(DraggableList.class.getName()).log(Level.SEVERE, null, cnfe);
        }
        LOCAL_OBJECT_FLAVOR = initialFlavor;
        SUPPORTED_FLAVORS = new DataFlavor[] {LOCAL_OBJECT_FLAVOR, DraggableTree.LOCAL_OBJECT_FLAVOR};
    }

    //Os objetos que emitirao e receberao o drag&drop
    private final DragSource dragSource;
    private final DropTarget dropTarget;

    //Os itens arrastados e o alvo do drag&drop
    private List incomingObjects = null;
    private List outgoingObjects = null;
    private Object targetObject = null;

    //Variaveis referentes a imagem fantasma do drag&drop
    private Point ghostOffset;
    private BufferedImage ghostImage;
    private Rectangle ghostRectangle;
    private Rectangle oldGhostRectangle;

    //Variaveis referentes a linha de alvo do drag&drop
    private Rectangle dropLine;
    private Rectangle oldDropLine;
    private Rectangle targetBounds;

    //O handler do drag&drop
    private DraggableHandler handler;

    //A lista artificial de selecao
    private int[] newSelection;
    private int[] currentSelection;

    public DraggableList() {
        super();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Prepara o DnD desta arvore
        dragSource = new DragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        dropTarget = new DropTarget(this, this);

        //Controi o handler padrao
        handler = new DraggableHandler();

        addListSelectionListener(this);
        addMouseListener(this);
    }

    public DraggableHandler getDragHandler() {
        return handler;
    }
    
    public void setDragHandler(DraggableHandler handler) {
        this.handler = handler;
    }

    /**
     * Finaliza o <i>Drag & Drop</i> anulando todas as variaveis referentes a
     * esta operacao e repintando a arvore.
     */
    public void finishDnD() {
        incomingObjects = null;
        outgoingObjects = null;
        targetObject = null;

        ghostOffset = null;
        ghostImage = null;
        ghostRectangle = null;
        oldGhostRectangle = null;

        dropLine = null;
        oldDropLine = null;
        targetBounds = null;

        repaint();
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {

        int[] indices;

        if (currentSelection == null) {
            indices = getSelectedIndices();
        } else {
            setSelectedIndices(currentSelection);
            indices = currentSelection;
        }

        //Obtem os itens selecionados
        List selectedValues = getSelectedValuesList();

        //Nao existe selecao? CAI FORA!!
        if (selectedValues == null || selectedValues.size() < 1) {
            return;
        }

        Point clickPoint = dge.getDragOrigin();
        
        //Obter posicoes e tamanhos do objeto arrastado
        Rectangle selectionBounds = getCellBounds(indices[0], indices[indices.length - 1]);
        Point clickOffset = new Point(clickPoint.x - selectionBounds.x, clickPoint.y - selectionBounds.y);

        //Preparar uma imagem fantasma do objeto
        BufferedImage newGhostImage = new BufferedImage(selectionBounds.width, selectionBounds.height, BufferedImage.TYPE_INT_ARGB_PRE);

        //Gerar a imagem fantasma
        Graphics2D g2d = newGhostImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
        for (int i = 0; i < selectedValues.size(); i++) {
            Object item = selectedValues.get(i);
            int index = indices[i];

            Component render = getCellRenderer().getListCellRendererComponent(this, item, index, false, false);
            render.setBounds(getCellBounds(index, index));
            render.paint(g2d);
            g2d.translate(0, render.getBounds().height);
        }
        g2d.dispose();

        //Monta o objeto de transferencia
        DraggableItem item = handler.prepareOutgoingItems(dge, selectedValues, newGhostImage, clickOffset);
        try {
            List transferData = item.getTransferData(LOCAL_OBJECT_FLAVOR);
            outgoingObjects = transferData.subList(2, transferData.size());
        } catch (Exception ex) {
            outgoingObjects = null;
        }
        
        //Se nao houver itens, cancelar o Drag&Drop
        if (item.isDragListEmpty()) {
            finishDnD();
            return;
        }

        //Comecar o Drag&Drop
        dragSource.startDrag(dge, DragSource.DefaultCopyDrop, item, this);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

        //Checa se o tipo de objeto eh suportado
        if (dtde.isDataFlavorSupported(LOCAL_OBJECT_FLAVOR)){
            
            //Obtem o objeto
            try {
                Transferable transferable = dtde.getTransferable();
                ArrayList transferData = (ArrayList) transferable.getTransferData(LOCAL_OBJECT_FLAVOR);

                try {
                    //Monta o fantasma
                    ghostImage = (BufferedImage) transferData.get(0);
                    ghostOffset = (Point) transferData.get(1);

                    incomingObjects = handler.prepareIncomingItems(dtde,transferData.subList(2, transferData.size()));

                //Se der excessao aqui, o transferable nao era um DraggableItem
                } catch (ClassCastException ex) {
                    incomingObjects = handler.prepareIncomingItems(dtde, transferData);
                }

                if (incomingObjects.isEmpty()) {
                    dtde.rejectDrag();
                    finishDnD();
                } else {
                    dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                }

            //Se der qualquer excessao, cai fora sem montar a transferencia
            } catch (Exception ex) {
                Logger.getLogger(DraggableList.class.getName()).log(Level.SEVERE, null, ex);
                dtde.rejectDrag();
                finishDnD();
            }
        } else {
            dtde.rejectDrag();
        }

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

        // Descobrir o alvo
        Point dragPoint = dtde.getLocation();
        int targetIndex = locationToIndex(dragPoint);

        if (targetIndex < 0) {
            targetObject = null;
            targetBounds = null;
        } else {
            targetObject = getModel().getElementAt(targetIndex);
            targetBounds = getCellBounds(targetIndex, targetIndex);
        }


        if (targetBounds == null) {
            targetBounds = new Rectangle(dragPoint);
        }

        //Rolar o scroll
        scrollRectToVisible(targetBounds);

        //Calcular o target
        targetObject = handler.calculateDropTarget(dtde, targetObject, incomingObjects);

        //Se nao houver destino, cancela
        if (targetObject == null) {
            dropLine = null;

            if (oldDropLine != null) {
                paintImmediately(oldDropLine);
            }
            oldDropLine = null;

        } else {
            
            //Capturando os bounds do target
            targetIndex = ((DefaultListModel)getModel()).indexOf(targetObject);
            targetBounds = getCellBounds(targetIndex, targetIndex);

            if (targetBounds == null) {
                targetBounds = new Rectangle();
            }

            if (dropLine == null) {
                dropLine = new Rectangle();
            }

            if (oldDropLine == null) {
                oldDropLine = new Rectangle();
            }
            
            //Depois ressalta o alvo
            oldDropLine.setRect(dropLine);
            dropLine.setRect(0, targetBounds.y + (int) targetBounds.getHeight(), getWidth(), 2);

            //Atualizar a linha de drop
            paintImmediately(oldDropLine);
            paintImmediately(dropLine);
        }

        //Desenhar o fantasma
        if (!DragSource.isDragImageSupported()) {

            //Checa se existe uma imagem carregada
            if (ghostImage != null && ghostOffset != null) {

                //Instanciar posicoes
                if (ghostRectangle == null) {
                    ghostRectangle = new Rectangle();
                }

                if (oldGhostRectangle == null) {
                    oldGhostRectangle = new Rectangle();
                }

                //Guardar a nova posicao
                oldGhostRectangle.setRect(ghostRectangle);
                ghostRectangle.setRect(dragPoint.x - ghostOffset.x, dragPoint.y - ghostOffset.y, ghostImage.getWidth(), ghostImage.getHeight());

                //Atualizar o fantasma
                paintImmediately(oldGhostRectangle);
                paintImmediately(ghostRectangle);
            }
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (incomingObjects != null) {
                dtde.dropComplete(handler.performIncomingDrop(dtde, incomingObjects, targetObject));
            }
        } finally {
            finishDnD();
        }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        try {
            if (outgoingObjects != null) {
                handler.performOutgoingDrop(dsde, outgoingObjects, dsde.getLocation());
            }
        } finally {
            finishDnD();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        if (dropLine != null) {
            g2d.setColor(Color.GRAY);
            g2d.fill(dropLine);
        }

        if (!DragSource.isDragImageSupported()) {
            if (ghostImage != null && ghostRectangle != null) {
                g2d.drawImage(ghostImage, AffineTransform.getTranslateInstance(ghostRectangle.getX(), ghostRectangle.getY()), null);
            }
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        dropLine = null;
        ghostRectangle = null;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            currentSelection = newSelection;
        }

//        if (!e.isControlDown() && !e.isShiftDown()) {
//            selectionList.clear();
//        }
//
//        // Descobrir o alvo
//        Point dragPoint = e.getPoint();
//        int targetIndex = locationToIndex(dragPoint);
//
//        if (targetIndex < 0) {
//            return;
//        }
//
//        //Rolar o scroll
//        scrollRectToVisible(getCellBounds(targetIndex, targetIndex));
//
//        //Adicionar a lista de selecao
//        if (e.isShiftDown()) {
//            int minIndex = Math.min(getSelectedIndex(), targetIndex);
//            int maxIndex = Math.max(getSelectedIndex(), targetIndex);
//
//            for (int i = minIndex; i <= maxIndex; i++) {
//                selectionList.add(i);
//            }
//        } else {
//            selectionList.add(targetIndex);
//        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        newSelection = getSelectedIndices();
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde){}

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {}

    @Override
    public void dragExit(DragSourceEvent dse) {}

    @Override
    public void dragOver(DragSourceDragEvent dsde) {}

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            currentSelection = newSelection;
        }        
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

}
