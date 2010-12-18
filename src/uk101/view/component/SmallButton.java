/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * A very small push button.
 * 
 * @author Baldwin
 */
public class SmallButton extends JButton {
    private static final long serialVersionUID = 1L;
    
    static final Insets MARGIN = new Insets(0, 2, 0, 2);
    
    public SmallButton(String text, ActionListener listener) {
        super(text);
        setMargin(MARGIN);
        addActionListener(listener);
    }
}
