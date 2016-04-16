package no.uib.inf252.katscan.persistence;

import java.io.Serializable;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Marcelo Lima
 */
public class Project extends DefaultMutableTreeNode implements Serializable {
    
    private String name;

    public Project() {
        name = "New project";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
