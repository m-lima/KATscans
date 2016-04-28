package no.uib.inf252.katscan.view.katview;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.inf252.katscan.project.displayable.Displayable;
import no.uib.inf252.katscan.project.displayable.TransferFunctionNode;
import no.uib.inf252.katscan.view.katview.opengl.AlphaRenderer;
import no.uib.inf252.katscan.view.katview.opengl.CompositeRenderer;
import no.uib.inf252.katscan.view.katview.opengl.MaximumRenderer;
import no.uib.inf252.katscan.view.katview.opengl.SliceNavigator;
import no.uib.inf252.katscan.view.katview.opengl.SurfaceRenderer;

/**
 *
 * @author Marcelo Lima
 */
public interface KatView {

    public enum Type {
        COMPOSITE("Composite Renderer", 'C', true, CompositeRenderer.class),
        ALPHA("Alpha Renderer", 'A', true, AlphaRenderer.class),
        SURF("Surface Renderer", 'U', false, SurfaceRenderer.class),
        MAXIMUM("Maximum Renderer", 'M', false, MaximumRenderer.class),
        SLICE("Slice Navigator", 'S', true, SliceNavigator.class),
        EDITOR("Editor", 'E', true, TransferFunctionEditor.class),
        HISTOGRAM("Histogram", 'H', false, Histogram.class);

        private final String name;
        private final char mnemonic;
        private final boolean transferFunctionNeeded;
        private final Constructor<? extends Component> constructor;

        private Type(String name, char mnemonic, boolean transferFunctionNeeded, Class<? extends Component> clazz) {
            this.name = name;
            this.mnemonic = mnemonic;
            this.transferFunctionNeeded = transferFunctionNeeded;
            
            Constructor<? extends Component> newCOnstructor = null;
            try {
                if (transferFunctionNeeded) {
                    newCOnstructor = clazz.getConstructor(TransferFunctionNode.class);
                } else {
                    newCOnstructor = clazz.getConstructor(Displayable.class);
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(Type.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            constructor = newCOnstructor;
        }

        public String getName() {
            return name;
        }

        public char getMnemonic() {
            return mnemonic;
        }

        public boolean isTransferFunctionNeeded() {
            return transferFunctionNeeded;
        }

        public Constructor<? extends Component> getConstructor() {
            return constructor;
        }

    }

}
