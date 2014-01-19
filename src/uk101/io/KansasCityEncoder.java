/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Subclass of AudioEncoder that creates output in Kanasas City Standard
 * format. 
 * 
 * Bytes are encoded according to the rules:
 *   1 Start bit of 0
 *   8 data bits, LSB first
 *   2 Stop bits of 1
 * 
 * At the standard 300 baud:  
 *   bit 0 is represented as 4 cycles of 1200Hz
 *   bit 1 is represented as 8 cycles of 2400Hz
 *   
 * For faster (non-standard baud rates) the number of cycles is halved for 
 * 600 baud and halved again for 1200 baud.
 *
 * @author Baldwin
 */
public class KansasCityEncoder extends AudioEncoder {
    
    public static final int BAUD300 = 300;
    public static final int BAUD600 = 600;
    public static final int BAUD1200 = 1200;

    int baudRate;
    byte[] bit0, bit1;
    
    public KansasCityEncoder(int rate, int bits, int baud) {
        super(rate, bits);
        baudRate = baud;
        int n = 4/(baudRate/BAUD300);
        bit0 = getSamples(n, 1200);     // n cycles of 1200Hz
        bit1 = getSamples(2*n, 2400);   // 2n cycles of 2400Hz
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

    void encodeBit(int b) throws IOException {
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
    
    void encodeTone(int millis) throws IOException {
        int bits = (millis*baudRate)/1000;
        for (int i = 0; i < bits; i++) {
            encodeBit(1);
        }
    }
    
    /*
     * Encode all data from an InputStream
     */
    public void encodeStream(InputStream in) throws IOException {
        for (int b = in.read(); b != -1; b = in.read()) {
            encodeByte(b);
        }
    }
}
