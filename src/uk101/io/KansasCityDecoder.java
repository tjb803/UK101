/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014,2022
 */
package uk101.io;

import java.io.EOFException;
import java.io.IOException;

/**
 * Subclass of AudioDecoder that processes input in Kansas City Standard
 * format - see KansasCityEncoder for format details.
 *
 * This is a fairly simple decoder implementation but makes a good job of
 * processing high quality audio files (such as those written by the matching
 * encoder).  It also makes a fairly good job of handling lower quality files
 * such as original tapes - but it is best if they are captured at a high
 * sample rate (say 44.1kHz).  It probably won't correctly decode very poor
 * quality recordings.
 */
public class KansasCityDecoder extends AudioDecoder {

    private int markFreq, markCycles;
    private int spaceFreq, spaceCycles;
    private int countLo, countHi;
    private boolean adaptive;

    // Standard Kansas City encoding, based on required baud rate
    public KansasCityDecoder() {
        this(KansasCityEncoder.BAUD300);
    }

    public KansasCityDecoder(int baud) {
        this(baud, 0);
    }

    public KansasCityDecoder(int baud, int phase) {
        this(KansasCityEncoder.MARK_FREQ, KansasCityEncoder.MARK_FREQ/baud, phase);
    }

    // For experimentation the decoder allows the mark-frequency and cycle-count
    // to be defined (space-frequency/count is assumed to be mark/2).
    public KansasCityDecoder(int freq, int cycles, int phase) {
        super(phase);
        markFreq = freq;          // Frequency used for mark-bits
        markCycles = cycles;      // No. of cycles encoding mark bits
        spaceFreq = freq/2;       // Frequency used for space-bits
        spaceCycles = cycles/2;   // No. of cycles encoding space bits
    }

    public void setAdaptive(boolean adapt) {
        adaptive = adapt;
    }

    /*
     * Decode the next byte from the audio input.  We need to look
     * for the next '0' start-bit, then read the next 8 bits to form
     * the byte.  This will be followed by 2 '1' stop-bits which can
     * be ignored (which actually provides a little recovery space if
     * we have errors).
     */

    public int decodeByte() throws IOException {
        int b = 0;
        try {
            b = decodeBit();
            while (b != 0) {            // Search for next start bit
                b = decodeBit();
            }
            for (int i = 0; i < 8; i++) {
                b |= decodeBit()<<i;    // Read 8 data bits, LSB first
            }
        } catch (EOFException e) {
            b = -1;                     // Return -1 to indicate EOF
        }
        return b;
    }

    // Process cycles until we find the correct number of LO or HI tone
    // consecutive cycles to form a "0" or "1" bit.
    private int decodeBit() throws IOException {
        int bit = decodeCycle();
        int count = 1;
        while ((bit == 0 && count < spaceCycles) || (bit == 1 && count < markCycles)) {
            int cycle = decodeCycle();
            if (cycle == bit) {
                count += 1;
            } else {
                bit = cycle;
                count = 1;
            }
        }
        return bit;
    }

    // Read next tone cycle and classify it as either LO or HI tone.
    private int decodeCycle() throws IOException {
        // Count the number of samples in the next cycle.
        int count = readCycle();

        // Is it closer to the expected LO or HI tone count value?  For
        // wildly unexpected values we return HI, as this is safe blank
        // carrier tone.
        int lo = Math.abs(count-countLo);
        int hi = Math.abs(count-countHi);
        int result = (lo < hi && lo < countLo/2) ? 0 : 1;

        // A common problem is that tape speed can be variable so cycle lengths
        // can vary.  As long as this is fairly gradual we can try to handle it
        // by updating the expected sample counts to be closer to the actual
        // current values. This is not always a good idea though, so this function
        // is optional!
        if (adaptive) {
            if (result == 0 && lo < countLo/2) {
                countLo = (countLo*3 + count)/4;
                countHi = countLo/2;
            } else if (result == 1 && hi < countHi/2) { 
                countHi = (countHi*3 + count)/4;
                countLo = countHi*2;
            }
        }

        return result; 
    }

    /*
     * Begin and end decoding.
     */
    public void decodeStart() throws IOException {
        // We don't know the AudioFormat sample rate until decoding starts.  Need to
        // calculate how many samples are expected for 1 cycle of mark and space tone.
        countLo = (int)(audioFormat.getSampleRate() + spaceFreq/2)/spaceFreq;
        countHi = (int)(audioFormat.getSampleRate() + markFreq/2)/markFreq;
     }

    public void decodeEnd() throws IOException {
    }
}
