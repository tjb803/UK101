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
        setMargin(SmallButton.MARGIN);
        addActionListener(listener);
    }
}
