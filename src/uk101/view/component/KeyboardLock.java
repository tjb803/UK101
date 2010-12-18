/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.BorderLayout;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JToggleButton;

/**
 * This is a special keyboard key that toggles up and down when pressed
 * with the mouse.
 *
 * @author Baldwin
 */
public class KeyboardLock extends JToggleButton {
    private static final long serialVersionUID = 1L;

    int code;

    /*
     * Ideally this would be a subclass of KeyboardKey, but we need this
     * one to subclass JToggleButton rather than JButton so that is not so easy
     * to do.  For the moment it just copies code from KeyboardKey, but is
     * less general as we know it is only used for SHIFT-LOCK.
     */
    public KeyboardLock(String text, int size, int keycode, ItemListener handler) {
        setLayout(new BorderLayout());
        setBorder(KeyboardKey.KEY_BORDER);
        setFocusable(false);
        setAlignmentY(BOTTOM_ALIGNMENT);
        addItemListener(handler);

        // Lock-keys are always the default size.
        setMaximumSize(KeyboardKey.KEY_SIZE);
        setPreferredSize(KeyboardKey.KEY_SIZE);

        // Always have two lines of text
        int i = text.indexOf(' ');
        JLabel l1 = new JLabel(text.substring(0,i), CENTER);
        l1.setFont(KeyboardKey.KEY_FONT);
        JLabel l2 = new JLabel(text.substring(i+1), CENTER);
        l2.setFont(KeyboardKey.KEY_FONT);
        add(l1, BorderLayout.NORTH);
        add(l2, BorderLayout.SOUTH);
        code = keycode;
    }

    public int getCode() {
        return code;
    }
}
