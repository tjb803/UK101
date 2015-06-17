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
    
    private int bytesPerSample;
    private boolean sineWave;
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
                double f; 
                if (sineWave) 
                    f = waveFn(sampleTime*j, cycleTime);
                else 
                    f = waveFnHw(sampleTime*j, cycleTime);
                
                int a = (int)(25600 * f);
                data[k++] = (byte)(a>>8);
                if (bytesPerSample > 1) 
                    data[k++] = (byte)a;
            }    
        }

        return data;
    }
    
    // Correct Kansas City wave form should be a pure sine wave.  Shift back  
    // by 90deg so we start from -1 with a rising edge
    private double waveFn(double t, double w) {
        // t = sample time from start of cycle, w = total cycle time
        return Math.sin((Math.PI*2*t)/w - Math.PI/2);
    }
    
    // UK101 hardware actually generated a square wave and integrated it via a 
    // simple passive low-pass filter with R=100k and C=10n which gives a 
    // "capacitor charge/discharge" shape instead.  Since the same filter was used 
    // for both high and low frequencies this also had the effect of making the 
    // high frequency slightly quieter.
    private double waveFnHw(double t, double w) {
        // Half cycle time (time for rising or falling part of signal).
        double x = w/2;         
        
        // Time constant - should be 100k x 10n (0.001) but a smaller value seems
        // to give better results.
        double rc = 0.0003;    
        
        // Assume we are first called for the lowest tone (which will generate the
        // largest amplitude) and note the maximum amplitude reached for a 5V signal.
        if (maxV == 0) {
            maxV = 5 * (1 - Math.exp(-x/rc));
        }    
        
        // Calculate the maximum amplitude for the current frequency
        double m = 5 * (1 - Math.exp(-x/rc));
        
        // Get amplitude at sample time.  Generate identical values for each 
        // half of the cycle - we'll invert it later for the falling edge.
        // Add an adjustment so the higher frequency (lower amplitude) signal
        // still ends up centred around 0 when scaled.
        double v = 5 * (1 - Math.exp(-(t%x)/rc)) + (maxV-m)/2;
        
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
