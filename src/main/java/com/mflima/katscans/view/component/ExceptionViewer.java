package com.mflima.katscans.view.component;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 *
 * @author Marcelo Lima
 */
public class ExceptionViewer extends JPanel {

    public ExceptionViewer() {
        setOpaque(false);
        setBackground(new Color(200, 150, 150, 150));
        setBorder(new LineBorder(Color.RED, 2));
    }

}
