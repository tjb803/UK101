/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

/**
 * The arithmetic and logic unit that forms part of the 6502 CPU.
 *
 * @author Baldwin
 */
class ALU6502 {

    // ALU status flags
    boolean isOverflow;
    boolean isCarry;

    private boolean isDecimal;

    ALU6502() {
        isOverflow = isCarry = false;
        isDecimal = false;
    }

    void setDecimal(boolean decimal) {
        isDecimal = decimal;
    }

    /*
     * Arithmetic operations.  These can work in binary or BCD mode.
     */

    byte add(byte value1, byte value2, boolean carry) {
        byte result = 0;
        int c = (carry) ? 1 : 0;
        if (isDecimal) {
            int i = fromBCD(value1) + fromBCD(value2) + c;
            isCarry = (i > 100);
            if (isCarry) i = i - 100;
            result = toBCD(i);
        } else {
            int uresult = (value1 & 0xFF) + (value2 & 0xFF) + c;
            int sresult = value1 + value2 + c;
            isCarry = (uresult < 0 || uresult > 255);
            isOverflow = (sresult > 127 || sresult < -128);
            result = (byte)uresult;
        }
        return result;
    }

    byte sub(byte value1, byte value2, boolean carry) {
        byte result = 0;
        int c = (carry) ? 0 : 1;
        if (isDecimal) {
            int i = fromBCD(value1) - fromBCD(value2) - c;
            isCarry = (i < 0);
            if (isCarry) i = 100 + i;
            result = toBCD(i);
        } else {
            int uresult = (value1 & 0xFF) - (value2 & 0xFF) - c;
            int sresult = value1 - value2 - c;
            isCarry = (uresult >= 0 && uresult <= 255);
            isOverflow = (sresult > 127 || sresult < -128);
            result = (byte)uresult;
        }
        return result;
    }

    byte cmp(byte value1, byte value2) {
        int v1 = (value1 & 0xFF);
        int v2 = (value2 & 0xFF);
        isCarry = (v1 >= v2);
        return (byte)(v1- v2);
    }

    /*
     * Logical operations
     */

    byte and(byte value1, byte value2) {
        return (byte)(value1 & value2);
    }

    byte or(byte value1, byte value2) {
        return (byte)(value1 | value2);
    }

    byte xor(byte value1, byte value2) {
        return (byte)(value1 ^ value2);
    }

    /*
     * Shift operations
     */

    byte shl(byte value) {
        isCarry = (value < 0);
        return (byte)(value << 1);
    }

    byte rol(byte value, boolean carry) {
        isCarry = (value < 0);
        return (byte)((value << 1) | (carry ? 1 : 0));
    }

    byte shr(byte value) {
        isCarry = ((value & 1) != 0);
        return (byte)((value & 0xFF) >> 1);
    }

    byte ror(byte value, boolean carry) {
        isCarry = ((value & 1) != 0);
        return (byte)(((value & 0xFF) >> 1) | (carry ? 0x80 : 0));
    }

    /*
     * BCD operations
     */
    private int fromBCD(byte b) {
        int lo = b & 0xF;
        int hi = (b>>4) & 0xF;
        return lo + 10*hi;
    }

    private byte toBCD(int i) {
        int lo = i % 10;
        int hi = (i/10) % 10;
        return (byte)(lo + (hi<<4));
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        StringBuffer s = new StringBuffer("ALU: ");
        s.append(isDecimal ? "BCD" : "BIN");
        return s.toString();
    }
}
