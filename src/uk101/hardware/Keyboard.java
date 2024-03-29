/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2021
 */
package uk101.hardware;

import java.util.HashMap;
import java.util.Map;

import uk101.machine.Configuration;
import uk101.machine.Data;

/**
 * The keyboard is mapped into a 1K block of memory at DF00-DFFF, although it
 * only uses 1 byte.
 *
 * This class emulates both the UK101 and the Ohio Superboard II keyboards.
 */
public class Keyboard extends Memory {

    public static final int KEY_RUBOUT = -1;
    public static final int KEY_UPARROW = -2;
    public static final int KEY_LINEFEED = KEY_UPARROW;
    public static final int KEY_RETURN = -3;
    public static final int KEY_CTRL = -4;
    public static final int KEY_SHIFTLOCK = -5;
    public static final int KEY_LSHIFT = -6;
    public static final int KEY_RSHIFT = -7;
    public static final int KEY_SPACE = -8;
    public static final int KEY_ESC = -9;
    public static final int KEY_REPEAT = -10;
    public static final int KEY_RESET = -99;

    /*
     * There's no real storage associated with the keyboard, just an 8x8 matrix
     * of key switches.  The system writes a byte to the keyboard address which
     * activates one or more "row" lines on the matrix then reads a byte
     * which returns the "column" lines showing which switches on the selected
     * row(s) were closed.  By default all column bits are held high (so show
     * as 1) and will only be set low (show as 0) if a switch is pressed and
     * the corresponding row has been set low.
     *
     *  The keyboard matrix has the following layout:
     *
     *          C7    C6    C5    C4    C3    C2    C1    C0
     *           |     |     |     |     |     |     |     |
     *         ! |   " |   # |   $ |   % |   & |   ' |     |
     *         1 |   2 |   3 |   4 |   5 |   6 |   7 |     |
     *  R7 ------+-----+-----+-----+-----+-----+-----+-----+
     *         ( |   ) |(1)  |   * |   = | RUB |     |     |
     *         8 |   9 |   0 |   : |   - | OUT |     |     |
     *  R6 ------+-----+-----+-----+-----+-----+-----+-----+
     *         > |   \ |     |(2)  |     |     |     |     |
     *         . |   L |   O |   ^ |  CR |     |     |     |
     *  R5 ------+-----+-----+-----+-----+-----+-----+-----+
     *           |     |     |     |     |     |     |     |
     *         W |   E |   R |   T |   Y |   U |   I |     |
     *  R4 ------+-----+-----+-----+-----+-----+-----+-----+
     *           |     |     |     |     |  LF |   [ |     |
     *         S |   D |   F |   G |   H |   J |   K |     |
     *  R3 ------+-----+-----+-----+-----+-----+-----+-----+
     *           | ETX |     |     |     |   ] |   < |     |
     *         X |   C |   V |   B |   N |   M |   , |     |
     *  R2 ------+-----+-----+-----+-----+-----+-----+-----+
     *           |     |     |     |   ? |   + |   @ |     |
     *         Q |   A |   Z |space|   / |   ; |   P |     |
     *  R1 ------+-----+-----+-----+-----+-----+-----+-----+
     *      (3)  |     |(4)  |     |     | left|right|SHIFT|
     *           | CTRL|     |     |     |SHIFT|SHIFT| LOCK|
     *  R0 ------+-----+-----+-----+-----+-----+-----+-----+
     *  
     *  (1) Both MONUK02 and CEGMON decode shift-0 as @
     *  
     * Notes for Ohio Superboard II keyboard:
     *  (2) This key is labelled LINE FEED
     *  (3) This position is the REPEAT key
     *  (4) This position is the ESC key
     */

    private boolean isUK;
    private Map<Integer,Key> keys;
    private byte[] matrix;
    private byte kbport;
    private boolean paused;

    public Keyboard(String type) {
        super(K1);                  // Decodes to 1K of store
        kbport = (byte)0xFF;        // Default is to return nothing
        
        // UK or US keyboard?
        isUK = type.equals(Configuration.UK);

        // Build the key matrix.  One byte per row, one bit per column.
        // Keys set bits to 0 when pressed, so we start out with all bits
        // set to 1.
        matrix = new byte[8];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = (byte)0xFF;
        }

        // Define the supported keys
        keys = new HashMap<Integer,Key>();
        addKey('1', '!', 7, 7);
        addKey('2', '\"', 7, 6);
        addKey('3', '#', 7, 5);
        addKey('4', '$', 7, 4);
        addKey('5', '%', 7, 3);
        addKey('6', '&', 7, 2);
        addKey('7', '\'', 7, 1);
        addKey('8', '(', 6, 7);
        addKey('9', ')', 6, 6);
        addKey('0', '@', 6, 5);     // Note: Shift-0 decodes as @
        addKey(':', '*', 6, 4);
        addKey('-', '=', 6, 3);
        addKey(KEY_RUBOUT, 0, 6, 2);
        addKey('.', '>', 5, 7);
        addKey('L', 'l', 5, 6);  
        addKey('O', 'o', 5, 5);
        addKey(KEY_RETURN, 0, 5, 3);
        addKey('W', 'w', 4, 7);
        addKey('E', 'e', 4, 6);
        addKey('R', 'r', 4, 5);
        addKey('T', 't', 4, 4);
        addKey('Y', 'y', 4, 3);
        addKey('U', 'u', 4, 2);
        addKey('I', 'i', 4, 1);
        addKey('S', 's', 3, 7);
        addKey('D', 'd', 3, 6);
        addKey('F', 'f', 3, 5);
        addKey('G', 'g', 3, 4);
        addKey('H', 'h', 3, 3);
        addKey('J', 'j', 3, 2);
        addKey('K', 'k', 3, 1);  
        addKey('X', 'x', 2, 7);
        addKey('C', 'c', 2, 6);
        addKey('V', 'v', 2, 5);
        addKey('B', 'b', 2, 4);
        addKey('N', 'n', 2, 3);
        addKey('M', 'm', 2, 2);  
        addKey(',', '<', 2, 1);
        addKey('Q', 'q', 1, 7);
        addKey('A', 'a', 1, 6);
        addKey('Z', 'z', 1, 5);
        addKey(KEY_SPACE, ' ', 1, 4);
        addKey('/', '?', 1, 3);
        addKey(';', '+', 1, 2);
        addKey('P', 'p', 1, 1);  
        addKey(KEY_CTRL, 0, 0, 6);
        addKey(KEY_LSHIFT, 0, 0, 2);
        addKey(KEY_RSHIFT, 0, 0, 1);
        addKey(KEY_SHIFTLOCK, 0, 0, 0);
        addKey('\\', 0, 5, 6);
        addKey('[', 0, 3, 1);
        addKey(']', 0, 2, 2);
        addKey('_', 0, 5, 5);
        if (isUK) {
            addKey(KEY_UPARROW, '^', 5, 4);
        } else {
            addKey(KEY_LINEFEED, 0, 5, 4);
            addKey(KEY_ESC, 0, 0, 5);
            addKey(KEY_REPEAT, 0, 0, 7);
            addKey('^', 0, 2, 3);
        }

        // Start with SHIFTKOCK pressed
        pressKey(KEY_SHIFTLOCK);
    }

    public synchronized byte readByte(int offset) {
        // Returns the column values for any row that has been set to a 0
        // in a value previously written to the kbport address.
        byte b = (byte)0xFF;
        for (int i = 0, k = kbport; i < matrix.length; i++) {
            if ((k & 1) == 0) {
                b &= matrix[i];
            }
            k >>= 1;
        }
        return b;
    }

    public synchronized void writeByte(int offset, byte b) {
        kbport = b;
    }

    /*
     * Handle key presses and releases.
     * Note we suspend processing key strokes when the CPU is paused for timing
     * control, as those keys actions might get lost.
     */
    public synchronized void pressKey(int key) {
    	try {
    	    if (paused) wait();
	        Key k = keys.get(key);
	        if (k != null) {
	            matrix[k.row] &= ~k.col;
	        }
    	} catch (InterruptedException e) {
    	}
    }

    public synchronized void releaseKey(int key) {
    	try {
    		if (paused) wait();
            Key k = keys.get(key);
	        if (k != null) {
	            matrix[k.row] |= k.col;
	        }
    	} catch (InterruptedException e) {
    	}
    }
    
    public synchronized void pause(boolean state) {
    	paused = state;
    	if (!paused) {
    		notifyAll();
    	}
    }
    

    // Add details of a key
    private void addKey(int k1, int k2, int row, int col) {
        Key key = new Key(row, col);
        if (k1 != 0) keys.put(k1, key);
        if (k2 != 0) keys.put(k2, key);
    }
    
    /*
     * Return keyboard type
     */
    public boolean isUK() {
        return isUK;
    }

    /*
     * Representation of the key
     */
    private static class Key {
        int row;  byte col;
        Key(int r, int c) {
            row = r;
            col = (byte)(1<<c);
        }
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        StringBuilder s = new StringBuilder("Keyboard: ");
        s.append(Data.toBinaryString(kbport)).append(" [");
        for (int i = 0; i < matrix.length; i++)
            s.append(" ").append(Data.toBinaryString(matrix[i]));
        s.append("] = ").append(Data.toBinaryString(readByte(0)));
        return s.toString();
    }
}
