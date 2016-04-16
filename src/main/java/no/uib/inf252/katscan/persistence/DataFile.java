package no.uib.inf252.katscan.persistence;

import java.io.Serializable;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Marcelo Lima
 */
public class DataFile extends DefaultMutableTreeNode implements Serializable {
    
    private String name;
    private String file;

    public DataFile(String name, String file) {
        if (name == null || file == null || name.isEmpty() || file.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        this.name = name;
        this.file = file;
        setUserObject(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof String) {
            return Objects.equals(obj, name);
        } else {
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DataFile other = (DataFile) obj;
            
            return !Objects.equals(this.name, other.name);
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
