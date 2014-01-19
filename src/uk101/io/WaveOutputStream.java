/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * WAV as part of the close operation.
 *
 * @author Baldwin
 */
public class WaveOutputStream extends OutputStream {
 
    OutputStream outputStream;
    AudioEncoder audioEncoder;
    ByteArrayOutputStream audioStream;

    public WaveOutputStream(OutputStream out, AudioEncoder encoder) {
        outputStream = out;
        audioEncoder = encoder;
        audioStream = new ByteArrayOutputStream();
    }
    
    /*
     * Override the required OutputStream methods
     */

    public void write(int b) throws IOException {
        if (audioStream.size() == 0) {
            audioEncoder.encodeStart(audioStream);
        }    
        audioEncoder.encodeByte(b, audioStream);
    }
    
    public void close() throws IOException {
        if (audioStream.size() != 0) {
            audioEncoder.encodeEnd(audioStream);
            ByteArrayInputStream in = new ByteArrayInputStream(audioStream.toByteArray());
            int frames = in.available() / audioEncoder.getFormat().getFrameSize();
            AudioInputStream audioIn = new AudioInputStream(in, audioEncoder.getFormat(), frames);
            AudioSystem.write(audioIn, Type.WAVE, outputStream);
            audioIn.close();
        }
        outputStream.close();
    }
    
    /*
     * Extra useful methods
     */
    
    public void write(InputStream in) throws IOException {
        if (audioStream.size() == 0) {
            audioEncoder.encodeStart(audioStream);
        } 
        audioEncoder.encodeStream(in, audioStream); 
    }
}
