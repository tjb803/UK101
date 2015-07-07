/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010, 2015
 */
package uk101.view.component;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JToggleButton;

/**
 * This is a special keyboard key that toggles up and down when pressed
 * with the mouse.
 */
public class KeyboardLock extends JToggleButton implements ItemListener {
    private static final long serialVersionUID = 1L;

    private int code;

    /*
     * Ideally this would be a subclass of KeyboardKey, but we need this
     * one to subclass JToggleButton rather than JButton so that is not so easy
     * to do.  For the moment it just copies code from KeyboardKey, but is
     * less general as we know it is only used for SHIFT-LOCK.
     */
    public KeyboardLock(String text, int keycode, boolean state, ItemListener handler) {
        setLayout(new BorderLayout());
        setBorder(state ? KeyboardKey.KEY_BORDER_DOWN : KeyboardKey.KEY_BORDER_UP);
        setAlignmentY(BOTTOM_ALIGNMENT);
        setSelected(state);
        addItemListener(handler);
        addItemListener(this);

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

    // Item listener to change key border
    public void itemStateChanged(ItemEvent e) {
        setBorder(isSelected() ? KeyboardKey.KEY_BORDER_DOWN : KeyboardKey.KEY_BORDER_UP);
    }
}
