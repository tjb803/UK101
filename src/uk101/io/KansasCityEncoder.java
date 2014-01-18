/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Utility class to encode data as RAW audio in Kansas City format.  
 * 
 * Bytes are encoded according to the rules:
 *   1 Start bit of 0
 *   8 data bits, LSB first
 *   2 Stop bits of 1
 * 
 * At the standard 300 baud:  
 *   0 is represented as 4 cycles of 1200Hz
 *   1 is represented as 8 cycles of 2400Hz
 *   
 * For faster (non-standard baud rates) the number of cycles is halved for 
 * 600 baud and halved again for 1200 baud.
 *
 * @author Baldwin
 */
public class KansasCityEncoder {
    
    static final int LO_TONE = 1200;
    static final int HI_TONE = 2400;

    KansasCityFormat format;
    byte[] bit0, bit1, zero;
    
    public KansasCityEncoder(KansasCityFormat fmt) {
        format = fmt;
        int n = 4/(format.getBaudRate()/KansasCityFormat.BAUD300);
        bit0 = getSamples(n, LO_TONE);      // n cycles of 1200Hz
        bit1 = getSamples(2*n, HI_TONE);    // 2n cycles of 2400Hz
        zero = getZeros(bit0.length);
    }

    // Samples are encoded using a somewhat squared-off sine wave, as this 
    // sounds more like the original!
    byte[] getSamples(int cycles, int freq) {
        int byteCount = format.getFrameSize();
        int sampleCount = (int)(format.getSampleRate()*cycles + freq/2)/freq;
        byte[] data = new byte[sampleCount*byteCount];
        for (int i = 0, j = 0; i < sampleCount; i++) {
            double s = Math.sin((i*cycles*2*Math.PI)/sampleCount);
            double r = Math.signum(s)*Math.sqrt(Math.abs(s));
            if (byteCount == 1) {
                int a = 128 + (int)(100*r); // 8-bit is unsigned
                data[j++] = (byte)a;
            } else {
                int a = (int)(25600*r);     // 16-bit is signed
                data[j++] = (byte)(a>>8);
                data[j++] = (byte)a;
            }
        }
        return data;
    }
    
    byte[] getZeros(int length) {
        byte[] data = new byte[length];
        Arrays.fill(data, (format.getFrameSize() == 1) ? (byte)128 : (byte)0);
        return data;
    }
    
    /*
     * Encode a byte
     */
    public void encodeByte(int b, OutputStream out) throws IOException {
        encodeBit(0, out);
        for (int i = 0; i < 8; i++) {
            encodeBit(b & 0x1, out);
            b >>= 1;
        }
        encodeBit(1, out);
        encodeBit(1, out);
    }
    
    /*
     * Encode some carrier tone
     */
    public void encodeTone(int millis, OutputStream out) throws IOException {
        int bits = (millis*format.getBaudRate())/1000;
        for (int i = 0; i < bits; i++) {
            encodeBit(1, out);
        }
    }
    
    /*
     * Encode all data from an InputStream
     */
    public void encodeStream(InputStream in, OutputStream out) throws IOException {
        for (int b = in.read(); b != -1; b = in.read()) {
            encodeByte(b, out);
        }
    }
    
    /*
     * Complete encoding by ensuring samples return to zero
     */
    public void encodeEnd(OutputStream out) throws IOException {
        out.write(zero);
    }
    
    void encodeBit(int b, OutputStream out) throws IOException {
        out.write((b == 0) ? bit0 : bit1);
    }
}
