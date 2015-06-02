/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Utility class to create an input stream that can read an audio WAV
 * file in format defined by the AudioDecoder.
 */
public class WaveInputStream extends InputStream {
 
    private AudioDecoder audioDecoder;

    public WaveInputStream(InputStream in, AudioDecoder decoder) throws UnsupportedAudioFileException, IOException {
        audioDecoder = decoder;
        
        // Some Java versions will fail to process Audio streams if the underlying
        // input stream does not support mark()/reset().  So we wrap in a 
        // BufferedInputStream just to be sure.
        AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
        AudioFormat af1 = audio.getFormat();
        
        // The AudioDecoder requires PCM data in signed big-endian format.  The AudioSystem
        // should be able to convert the input to a suitable format, provided we keep all
        // the other parameters (such as sample rate, frame size, etc) the same.
        AudioFormat af2 = new AudioFormat(af1.getSampleRate(), af1.getSampleSizeInBits(), af1.getChannels(), true, true);
        audioDecoder.setInputStream(AudioSystem.getAudioInputStream(af2, audio), af2);
        audioDecoder.decodeStart();
    }

    /*
     * Override the required InputStream methods
     */

    public int read() throws IOException {
        return audioDecoder.decodeByte();
    }
    
    public void close() throws IOException {
        audioDecoder.decodeEnd();
    }
}
