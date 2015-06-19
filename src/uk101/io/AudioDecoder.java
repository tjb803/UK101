/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
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
    
    protected AudioDecoder() {
        audioFormat = new AudioFormat(AudioEncoder.RATE48K, AudioEncoder.BIT16, 1, true, true);
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
    private boolean hasNextSample;
    private int nextSample;
    
    protected int peekSample() throws IOException {
        if (!hasNextSample) {
            nextSample = nextSample();
            hasNextSample = true;
        }
        return nextSample;
    }
    
    protected int readSample() throws IOException {
        int sample = peekSample();
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
     * Assumes we are positioned at the start of a new cycle (zero amplitude)
     * and reads until we reach the next start point.
     * TODO: This will be confused by glitches in the signal. 
     */
    
    protected int readCycle() throws IOException {
        int count = 0;
        while (peekSample() >= 0) {
            readSample();
            count += 1;
        }
        while (peekSample() < 0) {
            readSample();
            count += 1;
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
