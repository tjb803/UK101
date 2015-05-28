/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import uk101.machine.Data;

/**
 * 6502 instruction disassembler.
 *
 * @author Baldwin
 */
public class Disassembler {

    public static final int MODE_IMPLICIT = 0;
    public static final int MODE_ACCUMULATOR = 1;
    public static final int MODE_IMMEDIATE = 2;
    public static final int MODE_RELATIVE = 3;
    public static final int MODE_ABSOLUTE = 4;
    public static final int MODE_INDIRECT = 5;
    public static final int MODE_ZEROPAGE = 6;
    public static final int MODE_ABS_X = 7;
    public static final int MODE_ABS_Y = 8;
    public static final int MODE_PRE_X = 9;
    public static final int MODE_POST_Y = 10;
    public static final int MODE_0PAGE_X = 11;
    public static final int MODE_0PAGE_Y = 12;

    // Last decoded instruction details
    public String instrText;
    public int instrOperand;
    public int instrMode;
    public int instrSize;

    // Source details
    private byte[] sourceBytes;
    private int sourceLength;
    private int sourceOrigin;

    // Position in the source of the start of the next instruction
    private int origin;
    private int position;

    /*
     * The disassembler is provided with an array of bytes and an origin for the first
     * byte in the array and will return a sequence of disassembled 6502 instructions.
     */

    public Disassembler() {
        this(new byte[0], 0);
    }

    public Disassembler(byte[] bytes, int origin) {
        reset(bytes, bytes.length, 0, origin, origin);
    }

    // Reset can be used to reset and reuse a disassembler instance.
    public void reset(byte[] bytes, int length, int offset, int origin, int start) {
        sourceBytes = bytes;
        sourceLength = length;
        sourceOrigin = origin;
        position = start - origin + offset;
    }

    /*
     * Return true if there are more instructions to disassemble.
     */
    public boolean hasNext() {
        return (position < sourceLength);
    }

    /*
     * Return next disassembled instruction.
     */
    public String nextInstruction() {
        String result = "";

        // Should not really be called unless hasNext() returns true
        if (hasNext()) {
           origin = sourceOrigin + position;
           decodeInstruction();
           decodeOperand();

           // Build the result ...
           // ... address
           result = Data.toHexString(origin) + ":  ";

           // ... instruction bytes
           for (int i = 0; i < instrSize; i++)
               result += Data.toHexString((byte)sourceBytes[position+i]) + " ";
           for (int i = instrSize; i < 3; i++)
               result += "   ";

           // .. mnemonic and operand
           result += formatInstruction();

           // Step to the next instruction
           position += instrSize;
        }

        return result;
    }

    // Decode the next instruction in a very simple way (just a lot of typing)
    private void decodeInstruction() {
        switch (Data.asBits(sourceBytes[position])) {
        default:   instrText = "???";  instrMode = MODE_IMPLICIT;     break;
        case 0x6D: instrText = "ADC";  instrMode = MODE_ABSOLUTE;     break;
        case 0x65: instrText = "ADC";  instrMode = MODE_ZEROPAGE;     break;
        case 0x69: instrText = "ADC";  instrMode = MODE_IMMEDIATE;    break;
        case 0x7D: instrText = "ADC";  instrMode = MODE_ABS_X;        break;
        case 0x79: instrText = "ADC";  instrMode = MODE_ABS_Y;        break;
        case 0x61: instrText = "ADC";  instrMode = MODE_PRE_X;        break;
        case 0x71: instrText = "ADC";  instrMode = MODE_POST_Y;       break;
        case 0x75: instrText = "ADC";  instrMode = MODE_0PAGE_X;      break;
        case 0x2D: instrText = "AND";  instrMode = MODE_ABSOLUTE;     break;
        case 0x25: instrText = "AND";  instrMode = MODE_ZEROPAGE;     break;
        case 0x29: instrText = "AND";  instrMode = MODE_IMMEDIATE;    break;
        case 0x3D: instrText = "AND";  instrMode = MODE_ABS_X;        break;
        case 0x39: instrText = "AND";  instrMode = MODE_ABS_Y;        break;
        case 0x21: instrText = "AND";  instrMode = MODE_PRE_X;        break;
        case 0x31: instrText = "AND";  instrMode = MODE_POST_Y;       break;
        case 0x35: instrText = "AND";  instrMode = MODE_0PAGE_X;      break;
        case 0xCD: instrText = "CMP";  instrMode = MODE_ABSOLUTE;     break;
        case 0xC5: instrText = "CMP";  instrMode = MODE_ZEROPAGE;     break;
        case 0xC9: instrText = "CMP";  instrMode = MODE_IMMEDIATE;    break;
        case 0xDD: instrText = "CMP";  instrMode = MODE_ABS_X;        break;
        case 0xD9: instrText = "CMP";  instrMode = MODE_ABS_Y;        break;
        case 0xC1: instrText = "CMP";  instrMode = MODE_PRE_X;        break;
        case 0xD1: instrText = "CMP";  instrMode = MODE_POST_Y;       break;
        case 0xD5: instrText = "CMP";  instrMode = MODE_0PAGE_X;      break;
        case 0x4D: instrText = "EOR";  instrMode = MODE_ABSOLUTE;     break;
        case 0x45: instrText = "EOR";  instrMode = MODE_ZEROPAGE;     break;
        case 0x49: instrText = "EOR";  instrMode = MODE_IMMEDIATE;    break;
        case 0x5D: instrText = "EOR";  instrMode = MODE_ABS_X;        break;
        case 0x59: instrText = "EOR";  instrMode = MODE_ABS_Y;        break;
        case 0x41: instrText = "EOR";  instrMode = MODE_PRE_X;        break;
        case 0x51: instrText = "EOR";  instrMode = MODE_POST_Y;       break;
        case 0x55: instrText = "EOR";  instrMode = MODE_0PAGE_X;      break;
        case 0xAD: instrText = "LDA";  instrMode = MODE_ABSOLUTE;     break;
        case 0xA5: instrText = "LDA";  instrMode = MODE_ZEROPAGE;     break;
        case 0xA9: instrText = "LDA";  instrMode = MODE_IMMEDIATE;    break;
        case 0xBD: instrText = "LDA";  instrMode = MODE_ABS_X;        break;
        case 0xB9: instrText = "LDA";  instrMode = MODE_ABS_Y;        break;
        case 0xA1: instrText = "LDA";  instrMode = MODE_PRE_X;        break;
        case 0xB1: instrText = "LDA";  instrMode = MODE_POST_Y;       break;
        case 0xB5: instrText = "LDA";  instrMode = MODE_0PAGE_X;      break;
        case 0x0D: instrText = "ORA";  instrMode = MODE_ABSOLUTE;     break;
        case 0x05: instrText = "ORA";  instrMode = MODE_ZEROPAGE;     break;
        case 0x09: instrText = "ORA";  instrMode = MODE_IMMEDIATE;    break;
        case 0x1D: instrText = "ORA";  instrMode = MODE_ABS_X;        break;
        case 0x19: instrText = "ORA";  instrMode = MODE_ABS_Y;        break;
        case 0x01: instrText = "ORA";  instrMode = MODE_PRE_X;        break;
        case 0x11: instrText = "ORA";  instrMode = MODE_POST_Y;       break;
        case 0x15: instrText = "ORA";  instrMode = MODE_0PAGE_X;      break;
        case 0xED: instrText = "SBC";  instrMode = MODE_ABSOLUTE;     break;
        case 0xE5: instrText = "SBC";  instrMode = MODE_ZEROPAGE;     break;
        case 0xE9: instrText = "SBC";  instrMode = MODE_IMMEDIATE;    break;
        case 0xFD: instrText = "SBC";  instrMode = MODE_ABS_X;        break;
        case 0xF9: instrText = "SBC";  instrMode = MODE_ABS_Y;        break;
        case 0xE1: instrText = "SBC";  instrMode = MODE_PRE_X;        break;
        case 0xF1: instrText = "SBC";  instrMode = MODE_POST_Y;       break;
        case 0xF5: instrText = "SBC";  instrMode = MODE_0PAGE_X;      break;
        case 0x8D: instrText = "STA";  instrMode = MODE_ABSOLUTE;     break;
        case 0x85: instrText = "STA";  instrMode = MODE_ZEROPAGE;     break;
        case 0x9D: instrText = "STA";  instrMode = MODE_ABS_X;        break;
        case 0x99: instrText = "STA";  instrMode = MODE_ABS_Y;        break;
        case 0x81: instrText = "STA";  instrMode = MODE_PRE_X;        break;
        case 0x91: instrText = "STA";  instrMode = MODE_POST_Y;       break;
        case 0x95: instrText = "STA";  instrMode = MODE_0PAGE_X;      break;
        case 0xEC: instrText = "CPX";  instrMode = MODE_ABSOLUTE;     break;
        case 0xE4: instrText = "CPX";  instrMode = MODE_ZEROPAGE;     break;
        case 0xE0: instrText = "CPX";  instrMode = MODE_IMMEDIATE;    break;
        case 0xCC: instrText = "CPY";  instrMode = MODE_ABSOLUTE;     break;
        case 0xC4: instrText = "CPY";  instrMode = MODE_ZEROPAGE;     break;
        case 0xC0: instrText = "CPY";  instrMode = MODE_IMMEDIATE;    break;
        case 0xAE: instrText = "LDX";  instrMode = MODE_ABSOLUTE;     break;
        case 0xA6: instrText = "LDX";  instrMode = MODE_ZEROPAGE;     break;
        case 0xA2: instrText = "LDX";  instrMode = MODE_IMMEDIATE;    break;
        case 0xBE: instrText = "LDX";  instrMode = MODE_ABS_Y;        break;
        case 0xB6: instrText = "LDX";  instrMode = MODE_0PAGE_Y;      break;
        case 0xAC: instrText = "LDY";  instrMode = MODE_ABSOLUTE;     break;
        case 0xA4: instrText = "LDY";  instrMode = MODE_ZEROPAGE;     break;
        case 0xA0: instrText = "LDY";  instrMode = MODE_IMMEDIATE;    break;
        case 0xBC: instrText = "LDY";  instrMode = MODE_ABS_X;        break;
        case 0xB4: instrText = "LDY";  instrMode = MODE_0PAGE_X;      break;
        case 0x8E: instrText = "STX";  instrMode = MODE_ABSOLUTE;     break;
        case 0x86: instrText = "STX";  instrMode = MODE_ZEROPAGE;     break;
        case 0x96: instrText = "STX";  instrMode = MODE_0PAGE_Y;      break;
        case 0x8C: instrText = "STY";  instrMode = MODE_ABSOLUTE;     break;
        case 0x84: instrText = "STY";  instrMode = MODE_ZEROPAGE;     break;
        case 0x94: instrText = "STY";  instrMode = MODE_0PAGE_X;      break;
        case 0x0A: instrText = "ASL";  instrMode = MODE_ACCUMULATOR;  break;
        case 0x0E: instrText = "ASL";  instrMode = MODE_ABSOLUTE;     break;
        case 0x06: instrText = "ASL";  instrMode = MODE_ZEROPAGE;     break;
        case 0x1E: instrText = "ASL";  instrMode = MODE_ABS_X;        break;
        case 0x16: instrText = "ASL";  instrMode = MODE_0PAGE_X;      break;
        case 0x4A: instrText = "LSR";  instrMode = MODE_ACCUMULATOR;  break;
        case 0x4E: instrText = "LSR";  instrMode = MODE_ABSOLUTE;     break;
        case 0x46: instrText = "LSR";  instrMode = MODE_ZEROPAGE;     break;
        case 0x5E: instrText = "LSR";  instrMode = MODE_ABS_X;        break;
        case 0x56: instrText = "LSR";  instrMode = MODE_0PAGE_X;      break;
        case 0x2A: instrText = "ROL";  instrMode = MODE_ACCUMULATOR;  break;
        case 0x2E: instrText = "ROL";  instrMode = MODE_ABSOLUTE;     break;
        case 0x26: instrText = "ROL";  instrMode = MODE_ZEROPAGE;     break;
        case 0x3E: instrText = "ROL";  instrMode = MODE_ABS_X;        break;
        case 0x36: instrText = "ROL";  instrMode = MODE_0PAGE_X;      break;
        case 0x6A: instrText = "ROR";  instrMode = MODE_ACCUMULATOR;  break;
        case 0x6E: instrText = "ROR";  instrMode = MODE_ABSOLUTE;     break;
        case 0x66: instrText = "ROR";  instrMode = MODE_ZEROPAGE;     break;
        case 0x7E: instrText = "ROR";  instrMode = MODE_ABS_X;        break;
        case 0x76: instrText = "ROR";  instrMode = MODE_0PAGE_X;      break;
        case 0x2C: instrText = "BIT";  instrMode = MODE_ABSOLUTE;     break;
        case 0x24: instrText = "BIT";  instrMode = MODE_ZEROPAGE;     break;
        case 0xCE: instrText = "DEC";  instrMode = MODE_ABSOLUTE;     break;
        case 0xC6: instrText = "DEC";  instrMode = MODE_ZEROPAGE;     break;
        case 0xDE: instrText = "DEC";  instrMode = MODE_ABS_X;        break;
        case 0xD6: instrText = "DEC";  instrMode = MODE_0PAGE_X;      break;
        case 0xEE: instrText = "INC";  instrMode = MODE_ABSOLUTE;     break;
        case 0xE6: instrText = "INC";  instrMode = MODE_ZEROPAGE;     break;
        case 0xFE: instrText = "INC";  instrMode = MODE_ABS_X;        break;
        case 0xF6: instrText = "INC";  instrMode = MODE_0PAGE_X;      break;
        case 0xCA: instrText = "DEX";  instrMode = MODE_IMPLICIT;     break;
        case 0x88: instrText = "DEY";  instrMode = MODE_IMPLICIT;     break;
        case 0xE8: instrText = "INX";  instrMode = MODE_IMPLICIT;     break;
        case 0xC8: instrText = "INY";  instrMode = MODE_IMPLICIT;     break;
        case 0xAA: instrText = "TAX";  instrMode = MODE_IMPLICIT;     break;
        case 0xA8: instrText = "TAY";  instrMode = MODE_IMPLICIT;     break;
        case 0xBA: instrText = "TSX";  instrMode = MODE_IMPLICIT;     break;
        case 0x8A: instrText = "TXA";  instrMode = MODE_IMPLICIT;     break;
        case 0x9A: instrText = "TXS";  instrMode = MODE_IMPLICIT;     break;
        case 0x98: instrText = "TYA";  instrMode = MODE_IMPLICIT;     break;
        case 0x90: instrText = "BCC";  instrMode = MODE_RELATIVE;     break;
        case 0xB0: instrText = "BCS";  instrMode = MODE_RELATIVE;     break;
        case 0xF0: instrText = "BEQ";  instrMode = MODE_RELATIVE;     break;
        case 0x30: instrText = "BMI";  instrMode = MODE_RELATIVE;     break;
        case 0xD0: instrText = "BNE";  instrMode = MODE_RELATIVE;     break;
        case 0x10: instrText = "BPL";  instrMode = MODE_RELATIVE;     break;
        case 0x50: instrText = "BVC";  instrMode = MODE_RELATIVE;     break;
        case 0x70: instrText = "BVS";  instrMode = MODE_RELATIVE;     break;
        case 0x18: instrText = "CLC";  instrMode = MODE_IMPLICIT;     break;
        case 0xD8: instrText = "CLD";  instrMode = MODE_IMPLICIT;     break;
        case 0x58: instrText = "CLI";  instrMode = MODE_IMPLICIT;     break;
        case 0xB8: instrText = "CLO";  instrMode = MODE_IMPLICIT;     break;
        case 0x38: instrText = "SEC";  instrMode = MODE_IMPLICIT;     break;
        case 0xF8: instrText = "SED";  instrMode = MODE_IMPLICIT;     break;
        case 0x78: instrText = "SEI";  instrMode = MODE_IMPLICIT;     break;
        case 0xEA: instrText = "NOP";  instrMode = MODE_IMPLICIT;     break;
        case 0x48: instrText = "PHA";  instrMode = MODE_IMPLICIT;     break;
        case 0x08: instrText = "PHP";  instrMode = MODE_IMPLICIT;     break;
        case 0x68: instrText = "PLA";  instrMode = MODE_IMPLICIT;     break;
        case 0x28: instrText = "PLP";  instrMode = MODE_IMPLICIT;     break;
        case 0x00: instrText = "BRK";  instrMode = MODE_IMPLICIT;     break;
        case 0x4C: instrText = "JMP";  instrMode = MODE_ABSOLUTE;     break;
        case 0x6C: instrText = "JMP";  instrMode = MODE_INDIRECT;     break;
        case 0x20: instrText = "JSR";  instrMode = MODE_ABSOLUTE;     break;
        case 0x40: instrText = "RTI";  instrMode = MODE_IMPLICIT;     break;
        case 0x60: instrText = "RTS";  instrMode = MODE_IMPLICIT;     break;
        }
    }

    // Decode the instruction length and instruction operand
    private void decodeOperand() {
        switch (instrMode) {
        default:             instrSize = 1;  break;
        case MODE_IMMEDIATE: instrSize = 2;  break;
        case MODE_ABSOLUTE:  instrSize = 3;  break;
        case MODE_INDIRECT:  instrSize = 3;  break;
        case MODE_ZEROPAGE:  instrSize = 2;  break;
        case MODE_RELATIVE:  instrSize = 2;  break;
        case MODE_ABS_X:     instrSize = 3;  break;
        case MODE_ABS_Y:     instrSize = 3;  break;
        case MODE_PRE_X:     instrSize = 2;  break;
        case MODE_POST_Y:    instrSize = 2;  break;
        case MODE_0PAGE_X:   instrSize = 2;  break;
        case MODE_0PAGE_Y:   instrSize = 2;  break;
        }

        // Ensure we have enough data left to decode the full instruction
        if (instrSize > 1) {
            if (position + instrSize <= sourceLength) {
                if (instrSize == 2) {
                    byte lo = sourceBytes[position+1];
                    instrOperand = Data.asAddr(lo);
                } else if (instrSize == 3) {
                    byte lo = sourceBytes[position+1];
                    byte hi = sourceBytes[position+2];
                    instrOperand = Data.asAddr(Data.getWord(hi, lo));
                }
            } else {
                instrText = "!!!";              // Incomplete instruction!
                instrMode = MODE_IMPLICIT;
                instrSize = 1;
            }
        }
    }

    // Format the instruction mnemonic and operand
    private String formatInstruction() {
        String result = "  " + instrText + " ";

        switch (instrMode) {
        default: break;
        case MODE_ACCUMULATOR: result += "A";                                                   break;
        case MODE_IMMEDIATE:   result += "#$" + Data.toHexString((byte)instrOperand);           break;
        case MODE_ABSOLUTE:    result += "$"+Data.toHexString((short)instrOperand);             break;
        case MODE_INDIRECT:    result += "($"+ Data.toHexString((short)instrOperand) + ")";     break;
        case MODE_ZEROPAGE:    result += "$"+Data.toHexString((byte)instrOperand);              break;
        case MODE_RELATIVE:    result += "$"+Data.toHexString(origin + (byte)instrOperand + 2); break;
        case MODE_ABS_X:       result += "$"+Data.toHexString((short)instrOperand) + ",X";      break;
        case MODE_ABS_Y:       result += "$"+Data.toHexString((short)instrOperand) + ",Y";      break;
        case MODE_PRE_X:       result += "($" + Data.toHexString((byte)instrOperand) + ",X)";   break;
        case MODE_POST_Y:      result += "($" + Data.toHexString((byte)instrOperand) + "),Y";   break;
        case MODE_0PAGE_X:     result += "$"+Data.toHexString((byte)instrOperand) + ",X";       break;
        case MODE_0PAGE_Y:     result += "$"+Data.toHexString((byte)instrOperand) + ",Y";       break;
        }

        return result;
    }
}
