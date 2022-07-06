/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2012
 */
package uk101.view.component;

import java.awt.Color;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * A button on the cassette player.
 */
public class CassetteButton extends JPanel {
    private static final long serialVersionUID = 1L;

    public JToggleButton button;

    public CassetteButton(String text, String label, Color color, ItemListener action) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(BOTTOM_ALIGNMENT);

        JLabel title = new JLabel(label);
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(3));

        button = new JToggleButton(text);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setForeground(color);
        add(button);

        button.addItemListener(action);
    }
}
