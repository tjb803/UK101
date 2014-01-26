/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

/**
 * Provides a base class and common function for converting data bytes
 * into audio tones.  The conversion provides a RAW audio datastream
 * based on the supplied AudioFormat details.
 * 
 * @author Baldwin
 */
public abstract class AudioEncoder {
    
    public static final int RATE44K = 44100;
    public static final int BIT16 = 16;
    public static final int BIT8 = 8;
 
    protected AudioFormat audioFormat;
    protected OutputStream outputStream;
    
    /*
     * AudioFormat for output is fixed to PCM encoding in either 8-bit
     * 16-bit signed big-endian mono format.
     */
    protected AudioEncoder(int rate, int bits) {
        audioFormat = new AudioFormat(rate, bits, 1, true, true);
        bytesPerSample = audioFormat.getFrameSize();
    }
    
    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }

    public AudioFormat getFormat() {
        return audioFormat;
    }
    
    private int bytesPerSample;
    
    // Generate a set of sound samples that produce a number of cycles of
    // a fixed tone.
    protected byte[] getSamples(int cycles, int freq) {
        int sampleLength = (int)(audioFormat.getSampleRate()*cycles + freq/2)/freq;
        byte[] data = new byte[sampleLength*bytesPerSample];
        for (int i = 0, k = 0; i < sampleLength; i++) {
            double s = waveFn((i*cycles*2*Math.PI)/sampleLength);
            int a = (int)(25600*s); 
            data[k++] = (byte)(a>>8);
            if (bytesPerSample > 1) 
                data[k++] = (byte)a;
        }
        return data;
    }
    
    // Correct Kansas City wave function should be a pure sine wave, but the
    // UK101 hardware actually generated a square wave and integrated it via a 
    // simple passive low-pass filter with R=100k and C=10n which gives a 
    // "capacitor charge/discharge" shape instead.  Since the same filter was used 
    // for both high and low frequencies this also had the effect of making the 
    // high frequency slightly quieter.
    // TODO: Implement the hardware wave function?
    private double waveFn(double r) {
        return Math.sin(r);
    }

    /*
     * Encoding methods need to be supplied by the concrete subclass
     */
    
    public abstract void encodeStart() throws IOException;
    public abstract void encodeEnd() throws IOException;
    public abstract void encodeByte(int b) throws IOException;
    
    public void encodeStream(InputStream in) throws IOException {
        for (int b = in.read(); b != -1; b = in.read()) {
            encodeByte(b);
        }    
    }
}
