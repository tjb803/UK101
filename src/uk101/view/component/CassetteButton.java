/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * A button on the cassette player.
 *
 * @author Baldwin
 */
public class CassetteButton extends JPanel {
    private static final long serialVersionUID = 1L;

    public JToggleButton button;

    public CassetteButton(String text, String label, ItemListener action) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(BOTTOM_ALIGNMENT);

        add(new JLabel(label));
        add(Box.createVerticalStrut(5));

        button = new JToggleButton(text);
        button.addItemListener(action);
        button.setAlignmentX(LEFT_ALIGNMENT);
        add(button);
    }
}
