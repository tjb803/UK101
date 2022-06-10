/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2017,2022
 */
package uk101.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

/**
 * Special version of the KansasCityDecoder than can flag potential
 * problems in the audio stream. 
 */
public class CheckingDecoder extends KansasCityDecoder {

    private AudioFormat checkFormat;
    private ByteArrayOutputStream checkOutput;

    private int errorFactor = 0;
    private int lastSample = 0;
    private boolean rising = false;
    private boolean falling = false;
    private boolean zeroed = true;

    private byte[] goodSample, errorSample;

    public CheckingDecoder(int baud, int phase, boolean adaptive, int factor) {
        super(baud, phase);
        setAdaptive(adaptive);
        checkOutput = new ByteArrayOutputStream();
        errorFactor = factor;
    }

    public CheckingDecoder(int freq, int cycles, int phase, boolean adaptive, int factor) {
        super(freq, cycles, phase);
        setAdaptive(adaptive);
        checkOutput = new ByteArrayOutputStream();
        errorFactor = factor;
    }

    public void decodeStart() throws IOException {
        super.decodeStart();
        checkFormat = new AudioFormat(audioFormat.getSampleRate(), 8, 1, false, true);
        goodSample = new byte[checkFormat.getFrameSize()];
        errorSample = new byte[checkFormat.getFrameSize()];
        Arrays.fill(goodSample, (byte)0x7F);
        Arrays.fill(errorSample, (byte)0x00); 
    }

    public InputStream getCheckStream() {
        return new ByteArrayInputStream(checkOutput.toByteArray());
    }

    public AudioFormat getCheckFormat() {
        return checkFormat;
    }

    // Override readSample() method so we can look for anomalies.
    protected int readSample(boolean invert) throws IOException {
        int sample = super.readSample(invert);

        boolean error = false;
        if (Math.abs(sample - lastSample) > errorFactor) {
            // Signal should be a steady rise and steady fall, only switching
            // from one to the other once after crossing zero.
            if ((rising && sample < lastSample && !zeroed) ||
                    (falling && sample > lastSample && !zeroed)) {
                error = true; 
            }

            if (sample > lastSample) {
                if (falling) {
                    zeroed = false;
                }
                rising = true;
                falling = false;
            } else if (sample < lastSample) {
                if (rising) {
                    zeroed = false;
                }
                rising = false;
                falling = true;
            }

            if (sample == 0 ||
                    (rising && lastSample < 0 && sample > 0) ||
                    (falling && lastSample > 0 && sample < 0)) {
                zeroed = true;
            }
        }
        checkOutput.write(error ? errorSample : goodSample);

        lastSample = sample;
        return sample;
    }
}
