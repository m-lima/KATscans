package no.uib.inf252.katscan.view.component;

import com.bulenkov.darcula.DarculaUIUtil;
import com.bulenkov.darcula.ui.DarculaTextFieldUI;
import com.bulenkov.iconloader.util.DoubleColor;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.Gray;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Marcelo Lima
 */
public class ValidatableBorder implements Border {
    
    public static final Color INVALID_COLOR = new Color(130, 50, 50);
    private static final DoubleColor INVALID_DOUBLE_COLOR = new DoubleColor(new Color(212, 121, 35), new Color(255, 175, 96));
//    private static final Color INVALID_COLOR = new Color(212, 121, 35);

    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int vOffset = c instanceof JPasswordField ? 3 : 4;
        if (DarculaTextFieldUI.isSearchField(c)) {
            vOffset += 2;
        }
        if (DarculaTextFieldUI.isSearchFieldWithHistoryPopup(c)) {
            return new InsetsUIResource(vOffset, 7 + 16 + 3, vOffset, 7 + 16);
        } else if (DarculaTextFieldUI.isSearchField(c)) {
            return new InsetsUIResource(vOffset, 4 + 16 + 3, vOffset, 7 + 16);
        } else {
            return new InsetsUIResource(vOffset, 7, vOffset, 7);
        }
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g2, int x, int y, int width, int height) {
        if (DarculaTextFieldUI.isSearchField(c)) {
            return;
        }

        Graphics2D g = ((Graphics2D) g2);
        final GraphicsConfig config = new GraphicsConfig(g);
        g.translate(x, y);

        if (c.hasFocus()) {
            if (valid) {
                DarculaUIUtil.paintFocusRing(g, 2, 2, width - 4, height - 4);
            } else {
                DarculaUIUtil.paintFocusRing(g, INVALID_DOUBLE_COLOR, new Rectangle(2, 2, width - 4, height - 4));
            }
        } else {
            boolean editable = !(c instanceof JTextComponent) || (((JTextComponent) c).isEditable());
            g.setColor(c.isEnabled() && editable ? Gray._100 : new Color(0x535353));
            g.drawRect(1, 1, width - 2, height - 2);
        }

        g.translate(-x, -y);
        config.restore();
    }
}
