/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Utility class to create an output stream that can write an audio WAV
 * file in format defined by the AudioEncoder.
 *
 * To write a WAV file we need to know its total length, so we have to 
 * build the RAW audio data in a temporary byte buffer before writing the 
 * WAV as part of the close operation.  Is there a better way to do this?
 */
public class WaveOutputStream extends OutputStream {
 
    private OutputStream outputStream;
    private AudioEncoder audioEncoder;
    private ByteArrayOutputStream audioStream;

    public WaveOutputStream(OutputStream out, AudioEncoder encoder) {
        outputStream = out;
        audioEncoder = encoder;
        audioStream = new ByteArrayOutputStream();
        audioEncoder.setOutputStream(audioStream);
    }
    
    /*
     * Override the required OutputStream methods
     */

    public void write(int b) throws IOException {
        if (audioStream.size() == 0) {
            audioEncoder.encodeStart();
        }    
        audioEncoder.encodeByte(b);
    }
    
    public void close() throws IOException {
        if (audioStream.size() != 0) {
            audioEncoder.encodeEnd();
            ByteArrayInputStream in = new ByteArrayInputStream(audioStream.toByteArray());
            int frames = in.available() / audioEncoder.getFormat().getFrameSize();
            AudioInputStream audioIn = new AudioInputStream(in, audioEncoder.getFormat(), frames);
            AudioSystem.write(audioIn, Type.WAVE, outputStream);
            audioIn.close();
        }
        outputStream.close();
    }
}
