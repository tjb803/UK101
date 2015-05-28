/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2015
 */
package uk101.machine;

/**
 * Utility class to manipulate 8-bit bytes and 16-bit words
 *
 * @author Baldwin
 */
public abstract class Data {

    public static final int BYTE_MASK = 0xFF;
    public static final int WORD_MASK = 0xFFFF;

    // Return 8- and 16-bit values as unsigned integers
    public static final int asBits(byte b) {
        return (b & BYTE_MASK);
    }

    public static final int asBits(short w) {
        return (w & WORD_MASK);
    }

    // Return 8- and 16-bit values as signed integers
    public static final int asInt(byte b) {
        return b;
    }

    public static final int asInt(short w) {
        return w;
    }

    // Return 16-bit value as address (integer 0 to 65535)
    public static final int asAddr(byte b) {
        return (b & BYTE_MASK);
    }

    public static final int asAddr(short w) {
        return (w & WORD_MASK);
    }

    // Byte to Word and Word to Byte conversions
    public static final short getWord(byte bh, byte bl) {
        return (short)((bh & BYTE_MASK)<<8 | (bl & BYTE_MASK));
    }

    public static final byte getLoByte(short w) {
        return (byte)w;
    }

    public static final byte getHiByte(short w) {
        return (byte)(w>>8);
    }

    // Format bytes and words in various ways
    public static final String toHexString(byte b) {
        StringBuilder result = new StringBuilder();
        String text = Integer.toHexString(asBits(b)).toUpperCase();
        result.append("00", 0, 2-text.length()).append(text);
        return result.toString();
    }

    public static final String toHexString(short w) {
        StringBuilder result = new StringBuilder();
        String text = Integer.toHexString(asBits(w)).toUpperCase();
        result.append("0000", 0, 4-text.length()).append(text);
        return result.toString();
    }

    public static final String toHexString(int addr) {
        return toHexString((short)addr);
    }

    public static final String toBinaryString(byte b) {
        StringBuilder result = new StringBuilder();
        String text = Integer.toBinaryString(asBits(b));
        result.append("00000000", 0, 8-text.length()).append(text);
        return result.toString();
    }
}

