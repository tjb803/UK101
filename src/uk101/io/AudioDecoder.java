/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014,2017
 */
package uk101.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;

/**
 * Provides a base class and common function for converting audio tones
 * into data bytes.
 */
public abstract class AudioDecoder {

    protected AudioFormat audioFormat;
    protected InputStream inputStream;

    /*
     * Default AudioFormat only gets used if recording directly from the
     * sound card, otherwise format is provided when setting the input stream.
     * Format must be linear PCM with signed big-endian data.
     */

    protected AudioDecoder(int phase) {
        audioFormat = new AudioFormat(AudioEncoder.RATE48K, AudioEncoder.BIT16, 1, true, true);
        phase = (phase%360)/90;
        phaseShift = (phase == 1 || phase == 3);
        phaseInvert = (phase == 2 || phase == 3);
    }

    public void setInputStream(InputStream in, AudioFormat format) {
        inputStream = in;
        audioFormat = format;
        channels = audioFormat.getChannels();
        bytesPerFrame = audioFormat.getFrameSize();
        bytesPerChannel = bytesPerFrame/channels;
    }

    public AudioFormat getFormat() {
        return audioFormat;
    }

    /*
     * Return the next data sample from the input stream.  This assumes
     * a signed big-endian format and averages the value for all channels.
     */

    private int bytesPerFrame, bytesPerChannel, channels;
    private boolean phaseShift, phaseInvert;
    private boolean hasNextSample;
    private int nextSample;

    protected int peekSample(boolean invert) throws IOException {
        if (!hasNextSample) {
            nextSample = nextSample();
            hasNextSample = true;
        }
        return (invert) ? -nextSample : nextSample;
    }

    protected int readSample(boolean invert) throws IOException {
        int sample = peekSample(invert);
        hasNextSample = false;
        return sample;
    }

    private int nextSample() throws IOException {
        byte[] frame = new byte[bytesPerFrame];
        if (inputStream.read(frame) != bytesPerFrame)
            throw new EOFException();

        int total = 0, count = 0;
        for (int i = 0, k = 0; i < channels; i++) {
            int a = frame[k++];
            for (int j = 1; j < bytesPerChannel; j++) {
                a = (a<<8)|(frame[k++]&0xFF);
            }
            if (a != 0) {       // Try to ignore blank channels
               total += a;
               count += 1;
            }
        }

        return (count == 0) ? 0 : total/count;
    }

    /*
     * Read the next tone cycle and return the number of samples it spanned.
     * Assumes we are positioned at the start of a new cycle and reads until
     * we reach the next cycle start point.  This can be confused by glitches
     * in the signal.
     */

    protected int readCycle() throws IOException {
        // For phase angles of 0 or 180 we start at zero amplitude, read 
        // the pair of +ve/-ve half-cycles and return to zero.
        int count = 0, last = 0;
        while (peekSample(phaseInvert) >= 0) {
            last = readSample(phaseInvert);
            count += 1;
        }
        while (peekSample(phaseInvert) < 0) {
            last = readSample(phaseInvert);
            count += 1;
        }
        if (phaseShift) {
            // For phase angles of 90 or 270 we start at the max/min amplitude
            // so need to continue until we reach the next max/min.
            while (peekSample(phaseInvert) > last) {
                last = readSample(phaseInvert);
                count += 1;
            }
        }
        return count;
    }

    /*
     * Decoding methods need to be supplied by the concrete subclass
     */

    public abstract void decodeStart() throws IOException;
    public abstract void decodeEnd() throws IOException;
    public abstract int decodeByte() throws IOException;
}
