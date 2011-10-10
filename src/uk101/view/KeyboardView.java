/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2011
 */
package uk101.view;

import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import uk101.hardware.Keyboard;
import uk101.machine.Computer;
import uk101.view.component.KeyboardKey;
import uk101.view.component.KeyboardLock;

/**
 * A visual representation of the keyboard.
 *
 * The keyboard can operate in two modes 'raw' or normal.  In 'raw' mode basic
 * key presses and releases are used and the keyboard layout closely matches
 * the UK101 (so for example SHIFT-3 gives a '#').  In normal mode an attempt
 * is made to use the PC characters to press the appropriate key (this ought to
 * make general typing easier but may not work correctly for games).
 */
public class KeyboardView extends JInternalFrame implements ItemListener, MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;

    static final String[] KB_ROW1 =
        { "! 1", "\" 2", "# 3", "$ 4", "% 5", "& 6", "' 7", "( 8", ") 9", "0", "* :", "= -" };
    
    static final String[] UK_ROW2 =
        { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "@ P", "\u2191 ^" };
    static final String[] UK_ROW3 =
        { "A", "S", "D", "F", "G", "H", "LF J", "[ K", "\\ L", "+ ;" };
    static final String[] UK_ROW4 =
        { "Z", "X", "ETX C", "V", "B", "N", "] M", "< ,", "> .", "? /" };
    
    static final String[] US_ROW2 =
        { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P" };
    static final String[] US_ROW3 =
        { "A", "S", "D", "F", "G", "H", "J", "K", "L", "+ ;" };
    static final String[] US_ROW4 =
        { "Z", "X", "C", "V", "B", "N", "M", "< ,", "> .", "? /" };

    Computer computer;
    Keyboard keyboard;
    boolean rawMode, ctrl;

    public KeyboardView(Computer computer, Keyboard keyboard) {
        super("Keyboard", false, false, false, false);
        this.computer = computer;
        this.keyboard = keyboard;

        rawMode = false;

        // Layout the basic keys
        JPanel row1, row2, row3, row4, row5;
        row1 = makeRow(KB_ROW1);
        row5 = makeRow(new String[0]);
        if (keyboard.isUK()) {
            row2 = makeRow(UK_ROW2);
            row3 = makeRow(UK_ROW3);
            row4 = makeRow(UK_ROW4);
        } else {
            row2 = makeRow(US_ROW2);
            row3 = makeRow(US_ROW3);
            row4 = makeRow(US_ROW4);
        }
        
        // Add various additional keys to the rows.  Note the SHIFT-LOCK key starts in
        // the pressed state.
        row1.add(new KeyboardKey("RUB OUT", Keyboard.KEY_RUBOUT, this));
        if (!keyboard.isUK()) {
            row2.add(new KeyboardKey("ESC", Keyboard.KEY_ESC, this), 0);
            row2.add(new KeyboardKey("LINE FEED", Keyboard.KEY_LINEFEED, this));
        }
        row2.add(new KeyboardKey("RETURN", KeyboardKey.KEY_BIG, Keyboard.KEY_RETURN, this));
        row3.add(new KeyboardKey("CTRL", Keyboard.KEY_CTRL, this), 0);
        row3.add(new KeyboardLock("SHIFT LOCK", Keyboard.KEY_SHIFTLOCK, true, this));
        if (!keyboard.isUK()) {
            row3.add(new KeyboardKey("REPT", Keyboard.KEY_REPEAT, this));
        }
        row4.add(new KeyboardKey("SHIFT", KeyboardKey.KEY_BIG, Keyboard.KEY_LSHIFT, this), 0);
        row4.add(new KeyboardKey("SHIFT", KeyboardKey.KEY_BIG, Keyboard.KEY_RSHIFT, this));
        row5.add(new KeyboardKey("", KeyboardKey.KEY_STD*8, Keyboard.KEY_SPACE, this));

        // Add filler space to align the rows
        if (keyboard.isUK()) {
            row2.add(KeyboardKey.getOffset(KeyboardKey.KEY_HALF), 0);
            row5.add(KeyboardKey.getOffset(KeyboardKey.KEY_BIG+KeyboardKey.KEY_STD), 0);
        } else {
            row1.add(KeyboardKey.getOffset(KeyboardKey.KEY_HALF), 0);
            row3.add(KeyboardKey.getOffset(KeyboardKey.KEY_HALF), 0);
            row4.add(KeyboardKey.getOffset(KeyboardKey.KEY_HALF), 0);
            row5.add(KeyboardKey.getOffset(KeyboardKey.KEY_HALF+KeyboardKey.KEY_BIG+KeyboardKey.KEY_STD), 0);
        }

        // Add the RESET/BREAK keys, these must be clicked with both mouse buttons to 
        // reset the machine
        if (keyboard.isUK()) {        
            row3.add(new KeyboardKey("RESET", Keyboard.KEY_RESET, this));
            row3.add(new KeyboardKey("RESET", Keyboard.KEY_RESET, this));
        } else {
            row3.add(new KeyboardKey("BREAK", Keyboard.KEY_RESET, this));
        }

        // Add the keyboard 'raw' mode selector
        JCheckBox raw = new JCheckBox("Raw mode");
        raw.setAlignmentY(BOTTOM_ALIGNMENT);
        raw.addItemListener(this);
        row5.add(Box.createHorizontalGlue());
        row5.add(raw);

        // Build the full keyboard
        Container content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(row1);
        content.add(row2);
        content.add(row3);
        content.add(row4);
        content.add(row5);
        pack();
        setVisible(true);
    }

    // Build a row of standard keys
    JPanel makeRow(String[] names) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = 0; i < names.length; i++) {
            row.add(new KeyboardKey(names[i], 0, this));
        }
        return row;
    }

    /*
     * MouseListener is used for standard keys to detect key press and
     * key release events
     */
    public void mousePressed(MouseEvent e) {
        KeyboardKey key = (KeyboardKey)e.getSource();
        if (key.getCode() != Keyboard.KEY_RESET) {
            if (!rawMode && e.isShiftDown()) {
                keyboard.pressKey(Keyboard.KEY_LSHIFT);
            }
            keyboard.pressKey(key.getCode());
        } else {
            int m = e.getModifiersEx();
            if ((m & MouseEvent.BUTTON1_DOWN_MASK) != 0 &&
                    (m & (MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) != 0) {
                computer.reset();
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        KeyboardKey key = (KeyboardKey)e.getSource();
        if (key.getCode() != Keyboard.KEY_RESET) {
            keyboard.releaseKey(key.getCode());
            if (!rawMode && e.isShiftDown()) {
                keyboard.releaseKey(Keyboard.KEY_LSHIFT);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /*
     * Itemlistener is used to detect changes to locking keys (the SHIFT-LOCK)
     * and the keyboard mode selection.
     */
    public void itemStateChanged(ItemEvent e) {
        if (e.getItem() instanceof KeyboardLock) {
            KeyboardLock key = (KeyboardLock)e.getItem();
            if (key.isSelected())
                keyboard.pressKey(key.getCode());
            else
                keyboard.releaseKey(key.getCode());
        } else {
            rawMode = (e.getStateChange() == ItemEvent.SELECTED);
        }
    }

    /*
     * KeyListener is used to map the real keyboard.
     *
     * In normal mode we try to interpret the real PC keys and try to press the
     * matching character on the UK101 keyboard.  In 'raw' mode we just process
     * basic key-ups and key-downs.
     * 
     * Note: when the Ctrl key is pressed we always process as if in 'raw' mode,
     *       this is to ensure the BASIC editor works correctly.
     *       
     * Note: for Superboard II mappings, any of the keys to right of the P key (ie
     *       the square-brackets and the backslash (on a US keyboard)) will map to
     *       the LINEFEED key, and the Insert key will map to REPEAT.      
     */
    static final String SHIFT_CHARS = "!\"#$%&'()*=@[\\+]<>?";
    int mappedKey = 0, mappedShift = 0;

    public void keyPressed(KeyEvent e) {
        int key = mapKey(e);
        if (key != 0) {
            keyboard.pressKey(key);
            if (key == Keyboard.KEY_CTRL)
                ctrl = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        releaseMapped();
        int key = mapKey(e);
        if (key != 0) {
            keyboard.releaseKey(key);
            if (key == Keyboard.KEY_CTRL)
                ctrl = false;
        }
    }

    public void keyTyped(KeyEvent e) {
        if (!rawMode && !ctrl) {
            char key = e.getKeyChar();
            if (key > 31 && key < 127) 
                pressMapped(key);
        }
    }

    int mapKey(KeyEvent e) {
        int code = e.getKeyCode();
        int key = 0;
        switch (code) {
        case KeyEvent.VK_CONTROL:    key = Keyboard.KEY_CTRL;    break;
        case KeyEvent.VK_ENTER:      key = Keyboard.KEY_RETURN;  break;
        case KeyEvent.VK_BACK_SPACE: key = Keyboard.KEY_RUBOUT;  break;
        case KeyEvent.VK_ESCAPE:     key = Keyboard.KEY_ESC;     break;
        case KeyEvent.VK_INSERT:     key = Keyboard.KEY_REPEAT;  break;
        case KeyEvent.VK_DELETE:     key = Keyboard.KEY_RUBOUT;  break;
        case KeyEvent.VK_SHIFT:
            if (rawMode || ctrl) {
                if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT)
                    key = Keyboard.KEY_LSHIFT;
                else
                    key = Keyboard.KEY_RSHIFT;
            }
            break;
        default:
            if (rawMode || ctrl) {  // Note: the key(s) to the right of the 'P' key map to UPARROW/LINE FEED
                if (code == KeyEvent.VK_OPEN_BRACKET || code == KeyEvent.VK_CLOSE_BRACKET)
                    key = Keyboard.KEY_UPARROW;
                else if (code > 31 && code < 127)
                    key = code;
            }
            break;
        }
        return key;
    }
    
    void pressMapped(int key) {
        releaseMapped();
        if (SHIFT_CHARS.indexOf(key) != -1) {
            mappedShift = Keyboard.KEY_LSHIFT;
            keyboard.pressKey(mappedShift);
        }
        mappedKey = key;
        keyboard.pressKey(mappedKey);
    }
    
    void releaseMapped() {
        if (mappedKey != 0) {
            keyboard.releaseKey(mappedKey);
            keyboard.releaseKey(mappedShift);
            mappedKey = mappedShift = 0;
        }
    }
}
