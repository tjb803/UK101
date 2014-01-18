/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import javax.sound.sampled.AudioFormat;

/**
 * AudioFormat used for Kansas City data encoding, extended to
 * include the baud rate and initial lead-in and lead-out times.
 * 
 * Only a subset of AudioFormats are supported: data must be PCM
 * encoded in mono as either unsigned 8-bit or signed 16-bit 
 * big-endian data.
 * 
 * Baud rate must be 300, 600 or 1200.
 * 
 * @author Baldwin
 */
public class KansasCityFormat extends AudioFormat {
    
    public static final int BIT8 = 8;
    public static final int BIT16 = 16;
    public static final int BAUD300 = 300;
    public static final int BAUD600 = 600;
    public static final int BAUD1200 = 1200;
    
    int baudRate;
    int leadIn, leadOut;
    
    public KansasCityFormat(int rate, int bits, int baud, int li, int lo) {
        super(rate, bits, 1, (bits == BIT16), true);
        leadIn = li;  leadOut = lo;
        baudRate = baud;
    }
    
    public int getBaudRate() {
        return baudRate;
    }
    
    public int getLeadIn() {
        return leadIn;
    }
    
    public int getLeadOut() {
        return leadOut;
    }
}
