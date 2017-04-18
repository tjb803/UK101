/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2017
 */
package uk101.view.component;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * JPanel that tries to set its components to a small size.  Only really
 * works on some look and feels.
 */
public class MiniPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public JComponent add(JComponent comp) {
        super.add(comp);
        comp.putClientProperty("JComponent.sizeVariant", "mini");
        comp.updateUI();
        return comp;
    }
}
