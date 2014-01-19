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
    
    public static final int BIT8 = 8;
    public static final int BIT16 = 16;
    public static final int WAVE_SINE = 1;
 
    protected AudioFormat audioFormat;
    protected OutputStream outputStream;
    protected int waveForm;
    protected int leadIn, leadOut;
    
    /*
     * AudioFormat is fixed to PCM encoding in either unsigned 8-bit
     * or signed 16-bit big-endian mono format.
     */
    protected AudioEncoder(int rate, int bits) {
        audioFormat = new AudioFormat(rate, bits, 1, (bits==BIT16), true);
        waveForm = WAVE_SINE;
    }
    
    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }

    public AudioFormat getFormat() {
        return audioFormat;
    }
    
    public void setLeadInOut(int lin, int lout) {
        leadIn = lin;
        leadOut = lout;
    }
    
    // Generate a set of sound samples that produce a number of cycles of
    // a fixed tone.
    protected byte[] getSamples(int cycles, int freq) {
        int byteCount = audioFormat.getFrameSize();
        int sampleCount = (int)(audioFormat.getSampleRate()*cycles + freq/2)/freq;
        byte[] data = new byte[sampleCount*byteCount];
        for (int i = 0, j = 0; i < sampleCount; i++) {
            double s = waveFn((i*cycles*2*Math.PI)/sampleCount);
            if (byteCount == 1) {
                int a = 128 + (int)(100*s); // 8-bit is unsigned
                data[j++] = (byte)a;
            } else {
                int a = (int)(25600*s);     // 16-bit is signed
                data[j++] = (byte)(a>>8);
                data[j++] = (byte)a;
            }
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
    double waveFn(double r) {
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
