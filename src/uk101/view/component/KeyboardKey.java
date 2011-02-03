/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Displays a single keyboard key.
 *
 * @author Baldwin
 */
public class KeyboardKey extends JButton {
    private static final long serialVersionUID = 1L;

    public static final int KEY_STD = 2;
    public static final int KEY_BIG = 3;
    public static final int KEY_HALF = 1;

    static Font KEY_FONT;
    static Border KEY_BORDER;
    static Dimension KEY_SIZE;
    static {
        KEY_FONT = Font.decode("SansSerif-bold-9");
        KEY_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(3,0,3,0));
        int kw = new JLabel().getFontMetrics(KEY_FONT).stringWidth(" RESET ");
        KEY_SIZE = new Dimension(kw, kw);
    }

    // Returns filler whose size is related to the standard key size
    public static Component getOffset(int size) {
        return Box.createHorizontalStrut(KEY_SIZE.width*size/KEY_STD);
    }

    int code;
    
    public KeyboardKey(String text, int keycode, MouseListener handler) {
        this(text, KEY_STD, keycode, handler);
    }

    public KeyboardKey(String text, int size, int keycode, MouseListener handler) {
        setLayout(new BorderLayout());
        setBorder(KEY_BORDER);
        setFocusable(false);
        setAlignmentY(BOTTOM_ALIGNMENT);
        addMouseListener(handler);

        // Default key size is 2.  Other sizes need to be scaled in width.
        Dimension d = KEY_SIZE;
        if (size != KEY_STD) {
            d = new Dimension(d.width*size/KEY_STD, d.height);
        }
        setMaximumSize(d);
        setPreferredSize(d);

        // Set the text for the key (as either one or two lines) and extract
        // possible key code.
        int code2 = 0;
        int i = text.indexOf(' ');
        if (i == -1) {
            JLabel l1 = new JLabel(text, CENTER);
            l1.setFont(KEY_FONT);
            add(l1, BorderLayout.CENTER);
            if (text.length() > 0) {
                code2 = text.charAt(0);
            }
        } else {
            String t1 = text.substring(0,i);
            String t2 = text.substring(i+1);
            JLabel l1 = new JLabel(t1, CENTER);
            l1.setFont(KEY_FONT);
            JLabel l2 = new JLabel(t2, CENTER);
            l2.setFont(KEY_FONT);
            // Minor hack to get the 'up arrow' key right!
            if (t2.equals("^")) {
                add(l1, BorderLayout.CENTER);
            } else {
                add(l1, BorderLayout.NORTH);
                add(l2, BorderLayout.SOUTH);
            }
            code2 = t2.charAt(0);
        }

        // Store the matrix code to be used when the key is pressed/released
        code = (keycode != 0) ? keycode : code2;
    }

    public int getCode() {
        return code;
    }
}
