/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Display a text field with a label and optional monopsaced font
 */
public class DisplayText extends JPanel {
    private static final long serialVersionUID = 1L;

    static final Font monoFont = Font.decode("monospaced");

    private JLabel text;
    
    public DisplayText(String name, String value) {
        this(name, value, false);
    }

    public DisplayText(String name, String value, boolean monospaced) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentY(CENTER_ALIGNMENT);
        if (name != null) {
            JLabel title = new JLabel(name + ((monospaced) ? "=" : ": "));
            title.setAlignmentY(BOTTOM_ALIGNMENT);
            add(title);
        }
        text = new JLabel(value);
        if (monospaced)
            text.setFont(monoFont);
        text.setAlignmentY(BOTTOM_ALIGNMENT);
        add(text);
    }

    public void setValue(String t) {
        text.setText(t);
    }
}
