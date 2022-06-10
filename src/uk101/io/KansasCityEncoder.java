/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014,2022
 */
package uk101.io;

import java.io.IOException;

/**
 * Subclass of AudioEncoder that creates output in Kanasas City Standard
 * format.
 *
 * Bytes are encoded according to the rules:
 *   1 Start bit of 0
 *   8 data bits, LSB first
 *   2 Stop bits of 1
 *
 * The default mark frequency is 2400Hz, so for 300 Baud:
 *   bit 0 ("space") is represented as 4 cycles of 1200Hz
 *   bit 1 ("mark") is represented as 8 cycles of 2400Hz
 *
 * For faster (non-standard baud rates) the number of cycles is halved for
 * 600 baud and halved again for 1200 baud.
 */
public class KansasCityEncoder extends AudioEncoder {

    public static final int BAUD300 = 300;
    public static final int BAUD600 = 600;
    public static final int BAUD1200 = 1200;

    static final int MARK_FREQ = 2400;

    private int leadIn, leadOut;
    private int baudRate;
    private byte[] bit0, bit1;

    // Standard Kansas City encoding, based on required baud rate
    public KansasCityEncoder(int rate, int bits) {
        this(rate, bits, KansasCityEncoder.BAUD300);
    }

    public KansasCityEncoder(int rate, int bits, int baud) {
        this(rate, bits, baud, true);
    }

    public KansasCityEncoder(int rate, int bits, int baud, boolean sine) {
        this(rate, bits, KansasCityEncoder.MARK_FREQ, KansasCityEncoder.MARK_FREQ/baud, sine);
    }

    // For experimentation the encoder allows the mark-frequency and cycle-count
    // to be defined (space-frequency/count is assumed to be mark/2).
    public KansasCityEncoder(int rate, int bits, int freq, int cycles, boolean sine) {
        super(rate, bits, sine);
        baudRate = freq/cycles;               // Bits-per-second
        bit0 = getSamples(cycles/2, freq/2);  // n/2 cycles of space-frequency
        bit1 = getSamples(cycles, freq);      // n cycles of mark-frequency
    }

    public void setLeader(int lin, int lout) {
        leadIn = lin;
        leadOut = lout;
    }

    /*
     * Encode a byte.
     */
    public void encodeByte(int b) throws IOException {
        encodeBit(0);
        for (int i = 0; i < 8; i++) {
            encodeBit(b & 0x1);
            b >>= 1;
        }
        encodeBit(1);
        encodeBit(1);
    }

    private void encodeBit(int b) throws IOException {
        outputStream.write((b == 0) ? bit0 : bit1);
    }

    /*
     * Encode start and end.  Lead-in or lead-out tone is 2400Hz so can
     * be written as a sequence of '1' bits.
     */
    public void encodeStart() throws IOException {
        encodeTone(leadIn);
    }

    public void encodeEnd() throws IOException {
        encodeTone(leadOut);
        outputStream.write(bit0, 0, audioFormat.getFrameSize());
    }

    private void encodeTone(int millis) throws IOException {
        int bits = (millis*baudRate)/1000;
        for (int i = 0; i < bits; i++) {
            encodeBit(1);
        }
    }
}
