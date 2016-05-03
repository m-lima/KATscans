package no.uib.inf252.katscan.view.component.draggable;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * O item que encapsula os itens carregados por da familia de componentes
 * <i>Draggable</i>. Permite o transporte de diversos itens dentro de um so objeto
 * de transporte {@code DraggableItem}.
 *
 * <p>Possui o diferencial de armazenar uma imagem para ser o fantasma da operacao
 * de <i>Drag & Drop</i>.</p>
 *
 * @see DraggableTree
 * @see DraggableList
 *
 * @author Marcelo
 */
public class DraggableItem implements Transferable {

    private List objects;
    private Point offset;
    private BufferedImage ghostImage;

    /**
     * Cria um novo {@code DraggableItem} sem imagem fantasma.
     *
     * @param objects Os objetos a serem encapsulados.
     */
    public DraggableItem(List objects) {
        this.objects = objects;

        //Inserindo dois nulos no inicio da lista: um para a imagem fanatasma e outro pro offset do mouse
        objects.add(0, null);
        objects.add(0, null);
    }

    /**
     * Cria um novo {@code DraggableItem} com imagem fantasma.
     *
     * @param objects Os objetos a serem encapsulados.
     * @param ghostImage A imagem que sera usada para representar o <i>Drag & Drop</i>.
     * @param offset O offset do mouse em relacao a origem da imagem fantasma.
     */
    public DraggableItem(List objects, BufferedImage ghostImage, Point offset) {
        this.objects = objects;
        this.offset = offset;
        this.ghostImage = ghostImage;

        objects.add(0, offset);
        objects.add(0, ghostImage);
    }

    /**
     * Obtem o offset do mouse em relacao a origem da imagem fantasma.
     *
     * @return O offset do mouse em relacao a origem da imagem fantasma.
     */
    public Point getOffset() {
        return offset;
    }

    public boolean isDragListEmpty() {
        if (objects != null) {
            
            //Se so contiver o fantasma e o offset, esta vazia
            return objects.size() < 3;

        }
        return false;
    }

    /**
     * Obtem a imagem que sera usada para representar o <i>Drag & Drop</i>.
     *
     * @return A imagem que sera usada para representar o <i>Drag & Drop</i>.
     */
    public BufferedImage getGhostImage() {
        return ghostImage;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {DraggableTree.LOCAL_OBJECT_FLAVOR, DraggableTree.LOCAL_OBJECT_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < getTransferDataFlavors().length; i++) {
            if (!flavor.equals(getTransferDataFlavors()[i])){
                return false;
            }
        }

        return true;
    }

    @Override
    public List getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported (flavor)){
            return objects;
        }else{
            throw new UnsupportedFlavorException(flavor);
        }
    }

}
