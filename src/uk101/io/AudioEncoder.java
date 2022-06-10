/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014,2015
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
 */
public abstract class AudioEncoder {

    public static final int RATE48K = 48000;
    public static final int BIT16 = 16;
    public static final int BIT8 = 8;

    protected AudioFormat audioFormat;
    protected OutputStream outputStream;

    /*
     * AudioFormat for output is fixed to PCM encoding in either 8-bit
     * 16-bit signed big-endian mono format.
     */
    protected AudioEncoder(int rate, int bits, boolean sine) {
        audioFormat = new AudioFormat(rate, bits, 1, true, true);
        bytesPerSample = audioFormat.getFrameSize();
        sineWave = sine;
    }

    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }

    public AudioFormat getFormat() {
        return audioFormat;
    }

    private boolean sineWave;
    private int bytesPerSample;
    private double maxV;

    // Generate a set of sound samples that produce a number of cycles of
    // a fixed tone.
    protected byte[] getSamples(int cycles, int freq) {
        int sampleLength = (int)(audioFormat.getSampleRate() + freq/2)/freq;
        byte[] data = new byte[sampleLength*bytesPerSample*cycles];

        double cycleTime = 1.0/freq;
        double sampleTime = cycleTime/sampleLength;

        int k = 0;
        for (int i = 0; i < cycles; i++) {
            for (int j = 0; j < sampleLength; j++) {
                int a = (int)(25600 * waveFn(sampleTime*j, cycleTime));
                data[k++] = (byte)(a>>8);
                if (bytesPerSample > 1) 
                    data[k++] = (byte)a;
            }
        }

        return data;
    }

    // t = sample time from start of cycle
    // w = total cycle time
    private double waveFn(double t, double w) {
        return (sineWave) ? waveFnSin(t, w) : waveFnSys(t, w); 
    }

    // Correct Kansas City wave form should be a pure sine wave.
    private double waveFnSin(double t, double w) {
        return Math.sin(Math.PI*2*(t/w));
    }

    // UK101 hardware actually generated a 5V square wave and passed it through a
    // network of 3 resistors and a capacitor (see R54, R55, R56 and C13 on the
    // schematic on page 18 of the manual).  This generates a waveform equivalent
    // to 0.5V applied to a low-pass filter with an RC time constant of 0.0001.
    private double waveFnSys(double t, double w) {
        // Half cycle time (time for rising or falling part of signal).
        double x = w/2;

        // Applied voltage and RC time constant values for the equivalent
        // output network network low pass filter.
        double ev = 0.5;
        double rc = 0.0001;

        // Calculate the maximum amplitude for the current frequency and note the
        // largest possible value seen (which is used to scale the results).
        double mv = ev * (1 - Math.exp(-x/rc));
        maxV = Math.max(mv, maxV);

        // Get amplitude at sample time.  Generate identical values for each 
        // half of the cycle - we'll invert it later for the falling edge.
        // Add an adjustment so the higher frequency (lower amplitude) signals
        // still end up centred around 0 when scaled.
        double v = ev * (1 - Math.exp((t%x-x)/rc)) + (maxV-mv)/2;

        // Scale the result so it goes from -1 to 1 instead of 0 to maxV.
        double a = 2*v/maxV - 1;

        // And return either a rising or falling result
        return (t <= x) ? a : 0-a;
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
