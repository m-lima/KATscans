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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Uma arvore que suporta acoes de <i>Drag & Drop</i>.
 *
 * @see DraggableList
 * @see DraggableItem
 *
 * @author Marcelo
 */
public class DraggableTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener, TreeSelectionListener {

    //Informa o tipo de dados que essa arvore eh e com qual tipos trabalha
    public static final DataFlavor LOCAL_OBJECT_FLAVOR;
    public static final DataFlavor[] SUPPORTED_FLAVORS;

    //Inicializa as variaveis de tipo de dados arrastaveis por essa arvore
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
    private List<TreeNode> incomingNodes = null;
    private List<TreeNode> outgoingNodes = null;
    private TreeNode targetNode = null;

    //Variaveis referentes a imagem fantasma do drag&drop
    private Point ghostOffset;
    private BufferedImage ghostImage;
    private Rectangle ghostRectangle;
    private Rectangle oldGhostRectangle;

    //Variaveis referentes a linha de alvo do drag&drop
    private Rectangle dropLine;
    private Rectangle oldDropLine;
    private Rectangle targetBounds;

    //Flag que informa se essa arvore esta com o drag&drop habilitado
    private boolean draggable;

    //A raiz da arvore
    private TreeNode root;

    //O handler do drag&drop
    private DraggableHandler<TreeNode> handler;

    //A lista artificial de selecao
    private TreePath[] newSelection;
    private TreePath[] currentSelection;

    public DraggableTree() {
        super();

        //Inicializa a arvore com valores padroes
        this.draggable = false;
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Prepara o DnD desta arvore
        dragSource = new DragSource();
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        dropTarget = new DropTarget(this, this);

        //Controi o handler padrao
        handler = new DraggableHandler<TreeNode> ();

        addTreeSelectionListener(this);
    }

    public DraggableHandler<TreeNode> getDragHandler() {
        return handler;
    }

    public void setDragHandler(DraggableHandler<? extends TreeNode> handler) {
        this.handler = (DraggableHandler<TreeNode>) handler;
    }

    /**
     * Finaliza o <i>Drag & Drop</i> anulando todas as variaveis referentes a
     * esta operacao e repintando a arvore.
     */
    public void finishDnD() {
        incomingNodes = null;
        outgoingNodes = null;
        targetNode = null;

        ghostOffset = null;
        ghostImage = null;
        ghostRectangle = null;
        oldGhostRectangle = null;

        dropLine = null;
        oldDropLine = null;
        targetBounds = null;

        repaint();
    }

    /**
     * Obtem os {@code Bounds} da selecao, mesmo esta sendo multi-itens.
     * @param paths Os {@code paths} dos itens selecionados.
     * @return O {@code Bound} da selecao.
     */
    private Rectangle getSelectionBounds(TreePath[] paths) {
        int x;
        int y;
        int width = 0;
        int height = 0;

        Rectangle tempRect = getPathBounds(paths[paths.length - 1]);

        x = getPathBounds(paths[0]).x;
        y = tempRect.y + tempRect.height;

        for (TreePath treePath : paths) {
            width = Math.max(width, getPathBounds(treePath).width);
            height += getPathBounds(treePath).height;
        }

        return new Rectangle(x, y, width, height);
    }

    protected TreeNode[] getPathToRoot(TreeNode node, int depth) {
	TreeNode[] path;

	if(node == null) {
	    if(depth == 0) {
		return null;
            } else {
		path = new TreeNode[depth];
            }
	}
	else {
	    depth++;
	    path = getPathToRoot(node.getParent(), depth);
	    path[path.length - depth] = node;
	}
	return path;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {

        //Se nao for draggable cai fora
        if (!draggable) {
            return;
        }

        TreePath[] selectedPaths;

        if (currentSelection == null) {
            selectedPaths = getSelectionPaths();
        } else {
            setSelectionPaths(currentSelection);
            selectedPaths = currentSelection;
        }

        //Obtem os itens selecionados
        ArrayList<TreeNode> selectedNodes = new ArrayList<>(selectedPaths.length);
        outgoingNodes = new ArrayList<>();

        //Nao existe selecao? CAI FORA!!
        if (selectedPaths.length < 1) {
            return;
        }

        Point clickPoint = dge.getDragOrigin();

        //Obter posicoes e tamanhos do objeto arrastado
        Rectangle selectionBounds = getSelectionBounds(selectedPaths);
        Point clickOffset = new Point(clickPoint.x - selectionBounds.x, clickPoint.y - selectionBounds.y);

        //Preparar uma imagem fantasma do objeto
        BufferedImage newGhostImage = new BufferedImage(selectionBounds.width, selectionBounds.height, BufferedImage.TYPE_INT_ARGB_PRE);

        //Gerar a imagem fantasma
        Graphics2D g2d = newGhostImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
        for (int i = 0; i < selectedPaths.length; i++) {
            TreePath path = selectedPaths[i];
            TreeNode node = (TreeNode) path.getLastPathComponent();
            Component render = getCellRenderer().getTreeCellRendererComponent(this, node, true, isExpanded(path), getModel().isLeaf(node), getRowForPath(path), false);
            render.setSize(getPathBounds(path).width, (getPathBounds(path).height));
            render.setBackground(Color.WHITE);
            render.paint(g2d);
            g2d.translate(0, render.getBounds().height);

            //Aproveita a iteracao pra montar a lista de saida
            outgoingNodes.add(node);
            selectedNodes.add(i, node);
        }
        g2d.dispose();

        //Monta o objeto de transferencia
        DraggableItem item = handler.prepareOutgoingItems(dge, selectedNodes, newGhostImage, clickOffset);

        //Se nao houver itens, cancelar o Drag&Drop
        if (item == null || item.isDragListEmpty()) {
            finishDnD();
            return;
        }

        //Comecar o Drag&Drop
        dragSource.startDrag(dge, DragSource.DefaultMoveDrop, item, this);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

        //Checa se a arvore pode receber o drag
        if (draggable && dtde.isDataFlavorSupported(LOCAL_OBJECT_FLAVOR)){

            //Obtem o objeto
            try {
                Transferable transferable = dtde.getTransferable();
                ArrayList transferData = (ArrayList) transferable.getTransferData(LOCAL_OBJECT_FLAVOR);

                try{
                    //Monta o fantasma
                    ghostImage = (BufferedImage) transferData.get(0);
                    ghostOffset = (Point) transferData.get(1);

                    incomingNodes = handler.prepareIncomingItems(dtde, transferData.subList(2, transferData.size()));

                //Se der excessao aqui, o transferable nao era um DraggableItem
                } catch (ClassCastException ex) {
                    incomingNodes = handler.prepareIncomingItems(dtde, transferData);
                }

                if (incomingNodes.isEmpty()) {
                    dtde.rejectDrag();
                    finishDnD();
                } else {
                    dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                }

            //Se der qualquer excessao, cai fora sem montar a transferencia
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

        // Descobrir o alvo
        Point dragPoint = dtde.getLocation();
        TreePath path = getClosestPathForLocation(dragPoint.x, dragPoint.y);

        //Rolar o scroll
        scrollRowToVisible(getRowForPath(path) + 1);
        scrollRowToVisible(getRowForPath(path) - 1);

        //Calcular o target
        if (path == null || !draggable || incomingNodes == null) {
            targetNode = null;
        } else {
            expandPath(path);
            targetNode = handler.calculateDropTarget(dtde, (TreeNode) path.getLastPathComponent(), incomingNodes);
        }

        //Se nao houver destino, cancela
        if (targetNode == null) {
            dropLine = null;

            if (oldDropLine != null) {
                paintImmediately(oldDropLine);
            }
            oldDropLine = null;
            
        } else {
            path = new TreePath(getPathToRoot(targetNode, 0));

            //Capturando os bounds do target
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
            if (draggable && targetNode != null && incomingNodes != null) {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                dtde.dropComplete(handler.performIncomingDrop(dtde, incomingNodes, targetNode));
            }
        } finally {
            finishDnD();
        }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        try {
            if (draggable && outgoingNodes != null) {
                handler.performOutgoingDrop(dsde, outgoingNodes, dsde.getLocation());
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

    /**
     * Setando a raiz da arvore, todos os valores dela serao alterados.
     *
     * @param root A nova raiz da arvore.
     */
    public void setRoot(TreeNode root) {
        getModel().setRoot(root);
        this.root = root;
    }

    /**
     * Obtem a raiz da arvore.
     *
     * @return A raiz da arvore.
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Avisa se a arvore esta com o <i>Drag & Drop</i> habilitado.
     *
     * @return Se a arvore esta aceitando operacoes de <i>Drag & Drop</i>.
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Altera o modo da arvore para aceitar ou nao operacoes de <i>Drag & Drop</i>.
     *
     * @param draggable Se a arvore devera aceitar operacoes de <i>Drag & Drop</i>.
     */
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    @Override
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) super.getModel();
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        dropLine = null;
        ghostRectangle = null;
        repaint();
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde){};

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {}

    @Override
    public void dragExit(DragSourceEvent dse) {}

    @Override
    public void dragOver(DragSourceDragEvent dsde) {}

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {}

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        newSelection = getSelectionPaths();
    }
//
//    /**
//     * Empacota os dados que serao arrastados para fora da arvore.
//     * Eh chamada internamente assim que o gesto de inicio de <i>Drag & Drop</i>
//     * eh detectado.
//     *
//     * @param selectedPaths Os paths dos objetos que serao arrastados.
//     * @param newGhostImage A imagem que sera usada como fantasma para o <i>Drag & Drop</i>.
//     * @param clickPoint O offset do clique do mouse em relacao aos {@code Bounds} da selecao.
//     * @return Um objeto arrastavel que encapsula os itens selecionados.
//     * @see DraggableItem
//     */
//    protected abstract DraggableItem prepareOutgoingItems(TreePath[] selectedPaths, BufferedImage newGhostImage, Point clickPoint);
//
//    /**
//     * Interpreta os dados que estao chegando na arvore atraves de um
//     * {@see DraggableItem}. Eh chamada logo apos a interpretacao incial do
//     * {@see DraggableItem} e a retirada do {@see List} de objetos transportados.
//     *
//     * <p>Deve montar os novos nos da arvore referentes aos itens sendo carregados
//     * e prepara-los para a acao de <i>drop</i> na arvore.</p>
//     *
//     * <p>Vale lembrar que essa funcao apenas prepara os itens. <b>Nao os
//     * insere.</b></p>
//     *
//     * @param transferData A lista de objetos sendo arrastados.
//     * @return Uma lista de nos da arvore que serao usados na acao de <i>drop</i>.
//     * @throws Exception
//     * @see DraggableItem
//     */
//    protected abstract ArrayList<TreeNode> prepareIncomingItems(List transferData) throws Exception;
//
//    /**
//     * Detecta o item da arvore que servira de alvo do <i>Drag & Drop</i>.
//     *
//     * @param dtde O evento de <i>Drag & Drop</i>.
//     * @param path O <i>path</i> mais proximo da posicao atual do mouse.
//     * @param incomingNodes A lista de itens sendo arrastados.
//     * @return Um no que sera o alvo para a acao de <i>drop</i>.
//     */
//    protected abstract TreeNode calculateDropTarget(DropTargetDragEvent dtde, TreePath path, ArrayList<TreeNode> incomingNodes);
//
//    /**
//     * Executa de fato o <i>drop</i>. Eh chamado logo que o evento de <i>drop</i>
//     * eh detectado pelo {@code DropTargetListener}.
//     * O implementador deste metodo tem total liberdade para realizar qualquer
//     * operacao como sendo a acao de <i>drop</i> visto que retorne um {@code boolean}
//     * indicando o sucesso da operacao.
//     *
//     * @param incomingNodes Os nos sendo arrastados.
//     * @param targetNode O alvo do <i>Drap & Drop</i>.
//     * @return Se a operacao de <i>drop</i> foi bem sucedida.
//     */
//    protected abstract boolean performIncomingDrop(ArrayList<TreeNode> incomingNodes, TreeNode targetNode);
//
//    /**
//     * Eh chamado quando um <i>drop</i> eh realizado com elementos partindo
//     * deste componente.
//     *
//     * @param outgoingNodes Os nos sendo arrastados.
//     */
//    protected abstract void performOutgoingDrop(ArrayList<TreeNode> outgoingNodes);
}
