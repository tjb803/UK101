/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This is a panel with a FlowLayout that can return its 
 * preferred size for its elements laid out either in a 
 * single horizontal row or vertical column.
 * 
 * @author Baldwin
 */
public class FlowPanel extends JPanel implements SwingConstants {
    private static final long serialVersionUID = 1L;
    
    int direction;

    public FlowPanel(int direction) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        this.direction = direction;
    }

    public Dimension getPreferredSize() {
        Dimension size = null;
        if (direction == HORIZONTAL) {
            // Horizontal layout is the default for FlowLayout
            size = super.getPreferredSize();
        } else {
            // Vertical layout needs a bit more work
            FlowLayout f = (FlowLayout)getLayout();
            size = new Dimension(f.getHgap(), f.getVgap());
            for (Component c : getComponents()) {
                Dimension d = c.getPreferredSize();
                size.width = Math.max(size.width, d.width + f.getHgap()*2);
                size.height += d.height + f.getVgap();
            }
        }
        return size;
    }
}
