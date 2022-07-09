/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2022
 */
package uk101.hardware;

import java.io.IOException;

import uk101.machine.Computer;
import uk101.machine.Data;
import uk101.view.VideoView;

/**
 * An implementation of Premier Publications Screen Enhancement Kit.
 *
 * This provides 2K of video RAM and can set the display output to a
 * number of different formats, controlled by the write-only switch
 * location at $DE80:
 *
 *    $00 or $01     32x64
 *    $02 or $03     32x48
 *    $04            32x32 top
 *    $05            32x32 bottom
 *    $06            32x24 top
 *    $07            32x24 bottom
 *    $08            16x64 top
 *    $09            16x64 bottom
 *    $0A            16x48 top
 *    $0B            16x48 bottom
 *    $0C to $0F     illegal
 *
 * Adding the top most bit $80 gives an inverted (black-on-white) video.
 */
public class SEK extends Video {

    public Control control;

    private Computer computer;
    private byte defaultFormat;

    public SEK(int format, ROM chargen, Computer computer) throws IOException {
        // SEK is always a full 32x64 screen (2K) although it may display less
        super(32, 64, chargen);
        standard = false;

        this.computer = computer;
        defaultFormat = (byte)format;
        control = new Control();
    }

    // Override setView to allow the default SEK format to be set
    public void setView(VideoView view) {
        super.setView(view);
        setViewFormat(defaultFormat);
    }

    // Select format based on control byte value
    private void setViewFormat(byte format) {
        boolean valid = true;
        boolean invert = (format & 0x80) != 0;
        int rows = 0, cols = 0, start = 0, step = 0;
        switch (format & 0x0F) {
            case 0:
            case 1:  rows = 32; cols = 64; start = 0;     step = 64; break;
            case 2:
            case 3:  rows = 32; cols = 48; start = 12;    step = 64; break;
            case 4:  rows = 32; cols = 32; start = 0;     step = 32; break;
            case 5:  rows = 32; cols = 32; start = K1;    step = 32; break;
            case 6:  rows = 32; cols = 24; start = 5;     step = 32; break;
            case 7:  rows = 32; cols = 24; start = K1+5;  step = 32; break;
            case 8:  rows = 16; cols = 64; start = 0;     step = 64; break;
            case 9:  rows = 16; cols = 64; start = K1;    step = 64; break;
            case 10: rows = 16; cols = 48; start = 12;    step = 64; break;
            case 11: rows = 16; cols = 48; start = K1+12; step = 64; break;
            default: valid = false; break;
        }
        if (valid) {
            if (view != null)
                view.setFormat(rows, cols, start, step, invert);
            computer.setSEKFormat(format); // Push format value back to config
        }
    }

    // Mainly for debugging
    public String toString() {
        return "SEK" + super.toString() + "\n" + control.toString();
    }

    /*
     * The control byte is mapped as a single memory byte at $DE80.
     *
     * Note that the main SEK class extends Video and so has direct
     * access to the VideoView if it has been set. It is the VideoView
     * that takes the SEK "format" information to make the visible
     * output screen.
     */
    public class Control extends Memory {
        private byte format;

        public Control() {
            super(1);    // Only need 1 byte of memory
        }

        // Only read or write the single byte at 0xDE80
        public byte readByte(int a) {
            byte b = super.readByte(a);
            if (a == start)
                b = format;
            return b;
        }

        public void writeByte(int a, byte b) {
            if (a == start) {
                format = b;
                setViewFormat(b);
            }
        }

        // Mainly for debugging
        public String toString() {
            return "SEKControl" + memBase() + ": " + Data.toHexString(format);
        }
    }
}
