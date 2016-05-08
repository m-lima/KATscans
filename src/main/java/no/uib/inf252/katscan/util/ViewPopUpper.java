package no.uib.inf252.katscan.util;

import com.jogamp.opengl.math.FloatUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import no.uib.inf252.katscan.view.katview.opengl.VolumeRenderer;

/**
 *
 * @author Marcelo Lima
 */
public class ViewPopUpper {

    transient private JPopupMenu popupMenu;
    transient private JMenuItem menuOrtho;

    private void buildPopup(final VolumeRenderer owner) {
        popupMenu = new JPopupMenu();
        final JMenuItem top = new JMenuItem("Top", new ImageIcon(getClass().getResource("/icons/top.png")));
        final JMenuItem bottom = new JMenuItem("Bottom", new ImageIcon(getClass().getResource("/icons/bottom.png")));
        final JMenuItem front = new JMenuItem("Front", new ImageIcon(getClass().getResource("/icons/front.png")));
        final JMenuItem back = new JMenuItem("Back", new ImageIcon(getClass().getResource("/icons/back.png")));
        final JMenuItem right = new JMenuItem("Right", new ImageIcon(getClass().getResource("/icons/right.png")));
        final JMenuItem left = new JMenuItem("Left", new ImageIcon(getClass().getResource("/icons/left.png")));
        final JMenuItem reset = new JMenuItem("Reset", new ImageIcon(getClass().getResource("/icons/reset.png")));
        if (orthographic) {
            menuOrtho = new JMenuItem("Perspective", new ImageIcon(getClass().getResource("/icons/perspective.png")));
        } else {
            menuOrtho = new JMenuItem("Orthographic", new ImageIcon(getClass().getResource("/icons/ortho.png")));
        }
        final JMenuItem structure = new JMenuItem("Create structure", new ImageIcon(getClass().getResource("/icons/tree/structure.png")));

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == top) {
                    currentRotation.rotateByAngleX(+FloatUtil.HALF_PI);
                } else if (e.getSource() == bottom) {
                    currentRotation.rotateByAngleX(-FloatUtil.HALF_PI);
                } else if (e.getSource() == back) {
                    currentRotation.rotateByAngleX(FloatUtil.PI);
                    currentRotation.rotateByAngleZ(FloatUtil.PI);
                } else if (e.getSource() == right) {
                    currentRotation.rotateByAngleY(-FloatUtil.HALF_PI);
                } else if (e.getSource() == left) {
                    currentRotation.rotateByAngleY(+FloatUtil.HALF_PI);
                } else if (e.getSource() == reset) {
                    owner.getDisplayable().get
                } else if (e.getSource() == menuOrtho) {
                    toggleOrthographic(owner);
                } else if (e.getSource() == structure) {
                    owner.createStructure(popupMenu.getX(), popupMenu.getY(), 1f);
                }
            }
        };

        top.addActionListener(listener);
        bottom.addActionListener(listener);
        front.addActionListener(listener);
        back.addActionListener(listener);
        right.addActionListener(listener);
        left.addActionListener(listener);
        reset.addActionListener(listener);
        menuOrtho.addActionListener(listener);

        popupMenu.add(top);
        popupMenu.add(bottom);
        popupMenu.add(front);
        popupMenu.add(back);
        popupMenu.add(right);
        popupMenu.add(left);
        popupMenu.addSeparator();
        popupMenu.add(reset);
        popupMenu.addSeparator();
        popupMenu.add(menuOrtho);
        popupMenu.addSeparator();
        popupMenu.add(structure);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (popupMenu == null) {
                buildPopup((VolumeRenderer) e.getComponent());
            }
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

}
