/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.event.ActionListener;

import javax.swing.JToggleButton;

/**
 * A very small toggle button.
 * 
 * @author Baldwin
 */
public class SmallToggle extends JToggleButton {
    private static final long serialVersionUID = 1L;
    
    public SmallToggle(String text, ActionListener listener) {
        super(text);
        // Margin and font work in most look and feels ...
        setMargin(SmallButton.MARGIN);
        setFont(SmallButton.FONT);
        // ... size property works for Nimbus
        putClientProperty("JComponent.sizeVariant", "mini");
        updateUI();
        addActionListener(listener);
    }
}
