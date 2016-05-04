package no.uib.inf252.katscan.view.project;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import no.uib.inf252.katscan.project.KatNode;

/**
 *
 * @author Marcelo Lima
 */
public class ProjectBrowserRenderer extends JLabel implements TreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        setText(value.toString());
        setIcon(((KatNode)value).getIcon());
        return this;
    }

}
