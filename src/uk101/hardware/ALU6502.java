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
    boolean isZero;
    boolean isNegative;
    boolean isOverflow;
    boolean isCarry;

    private boolean isDecimal;

    ALU6502() {
        isNegative = isOverflow = isZero = isCarry = false;
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
            result = (byte)uresult;
            setFlags(uresult, sresult);
        }
        setFlags(result);
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
            result = (byte)uresult;
            setFlags(uresult, sresult);
            isCarry = !isCarry;
        }
        setFlags(result);
        return result;
    }

    void cmp(byte value1, byte value2) {
        boolean toCarry = ((value1 & 0xFF) >= (value2 & 0xFF));
        byte result = (byte)((value1 & 0xFF) - (value2 & 0xFF));
        setFlags(result, toCarry);
    }

    /*
     * Logical operations
     */

    byte and(byte value1, byte value2) {
        byte result = (byte)(value1 & value2);
        setFlags(result);
        return result;
    }

    byte or(byte value1, byte value2) {
        byte result = (byte)(value1 | value2);
        setFlags(result);
        return result;
    }

    byte xor(byte value1, byte value2) {
        byte result = (byte)(value1 ^ value2);
        setFlags(result);
        return result;
    }

    /*
     * Shift operations
     */

    byte shl(byte value) {
        boolean toCarry = (value < 0);
        byte result = (byte)(value << 1);
        setFlags(result, toCarry);
        return result;
    }

    byte rol(byte value, boolean carry) {
        boolean toCarry = (value < 0);
        byte result = (byte)((value << 1) | (carry ? 1 : 0));
        setFlags(result, toCarry);
        return result;
    }

    byte shr(byte value) {
        boolean toCarry = ((value & 1) != 0);
        byte result = (byte)((value & 0xFF) >> 1);
        setFlags(result, toCarry);
        return result;
    }

    byte ror(byte value, boolean carry) {
        boolean toCarry = (value & 0x01) != 0;
        byte result = (byte)(((value & 0xFF) >> 1) | (carry ? 0x80 : 0));
        setFlags(result, toCarry);
        return result;
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
     * Set status flags
     */
    private void setFlags(byte result) {
        isNegative = (result < 0);
        isZero = (result == 0);
    }

    private void setFlags(byte result, boolean carry) {
        isNegative = (result < 0);
        isZero = (result == 0);
        isCarry = carry;
    }

    private void setFlags(int uresult, int sresult) {
        isCarry = (uresult > 255) || (uresult < 0);
        isOverflow = (sresult > 127) || (sresult < -128);
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
