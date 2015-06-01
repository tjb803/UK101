/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * A very small push button.
 */
public class SmallButton extends JButton {
    private static final long serialVersionUID = 1L;
    
    static final Insets MARGIN = new Insets(0, 2, 0, 1);
    static final Font FONT = Font.decode("Dialog-10");
    
    public SmallButton(String text, ActionListener listener) {
        super(text);
        addActionListener(listener);
        // Margin and font work in most look and feels ...
        setMargin(MARGIN);
        setFont(FONT);
        // ... size property works for Nimbus
        putClientProperty("JComponent.sizeVariant", "mini");
        updateUI();
    }
}
