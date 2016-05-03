package no.uib.inf252.katscan.view.component.draggable;

import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcelo
 * @param <T>
 */
public class DraggableHandler<T> {

    /**
     * Empacota os dados que serao arrastados para fora da lista.
     * Eh chamada internamente assim que o gesto de inicio de <i>Drag & Drop</i>
     * eh detectado.
     *
     * @param evt O evento de <i>Drag & Drop</i>.
     * @param selectedValues Os objetos que serao arrastados.
     * @param ghostImage A imagem que sera usada como fantasma para o <i>Drag & Drop</i>.
     * @param offset O offset do clique do mouse em relacao aos {@code Bounds} da selecao.
     * @return Um objeto arrastavel que encapsula os itens selecionados.
     * @see DraggableItem
     */
    public DraggableItem prepareOutgoingItems(DragGestureEvent evt, List<T> selectedValues, BufferedImage ghostImage, Point offset) {
        return new DraggableItem(selectedValues, ghostImage, offset);
    }

    /**
     * Interpreta os dados que estao chegando na lista atraves de um
     * {@see DraggableItem}. Eh chamada logo apos a interpretacao incial do
     * {@see DraggableItem} e a retirada do {@see List} de objetos transportados.
     *
     * <p>Deve montar os novos itens da lista referentes aos itens sendo carregados
     * e prepara-los para a acao de <i>drop</i> na lista.</p>
     *
     * <p>Vale lembrar que essa funcao apenas prepara os itens. <b>Nao os
     * insere.</b></p>
     *
     * @param evt O evento de <i>Drag & Drop</i>.
     * @param transferData A lista de objetos sendo arrastados.
     * @return Uma lista de itens da lista que serao usados na acao de <i>drop</i>.
     * @throws Exception
     * @see DraggableItem
     */
    public List<T> prepareIncomingItems(DropTargetDragEvent evt, List<T> transferData) throws Exception {
        return new ArrayList(transferData);
    }

    /**
     * Detecta o item da lista que servira de alvo do <i>Drag & Drop</i>.
     *
     * @param evt O evento de <i>Drag & Drop</i>.
     * @param currentDropTarget O item mais proximo da posicao atual do mouse.
     * @param draggedObject
     * @return Um item que sera o alvo para a acao de <i>drop</i>.
     */
    public T calculateDropTarget(DropTargetDragEvent evt, T currentDropTarget, List<T> draggedObject) {
        return currentDropTarget;
    }

    /**
     * Executa de fato o <i>drop</i>. Eh chamado logo que o evento de <i>drop</i>
     * eh detectado pelo {@code DropTargetListener}.
     *
     * @param evt O evento de <i>Drag & Drop</i>.
     * @param incomingObjects Os itens sendo arrastados.
     * @param targetObject O alvo do <i>Drap & Drop</i>.
     * @return 
     */
    public boolean performIncomingDrop(DropTargetDropEvent evt, List<T> incomingObjects, T targetObject) {
        return true;
    }

    /**
     * Eh chamado quando um <i>drop</i> eh realizado com elementos partindo
     * deste componente.
     *
     * @param evt O evento de <i>Drag & Drop</i>.
     * @param outgoingObjects Os nos sendo arrastados.
     * @param dropLocation O local onde o drop esta sendo realizado.
     * @return 
     */
    public boolean performOutgoingDrop(DragSourceDropEvent evt, List<T> outgoingObjects, Point dropLocation) {
        return true;
    }

}
