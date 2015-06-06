/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014,2015
 */
package uk101.machine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import uk101.io.AudioEncoder;

/**
 * A simple sound output device implemented as an OutputStream. 
 */
public class Loudspeaker extends OutputStream {
    
    private AudioEncoder encoder;
    private SourceDataLine sound;
    private byte[] frame;
    int length;
    
    public Loudspeaker(AudioFormat format) throws LineUnavailableException {
        sound = AudioSystem.getSourceDataLine(format);
        frame = new byte[format.getFrameSize()];
        length = 0;
    }
    
    public Loudspeaker(AudioEncoder enc) throws LineUnavailableException {
        this(enc.getFormat());
        enc.setOutputStream(this);
        encoder = enc;
    }
    
    public void open() throws LineUnavailableException {
        sound.open();
        sound.start();          
    }
    
    public void close() {
        sound.drain();
        sound.stop();
        sound.close();
    }
    
    // Cannot write single bytes, must buffer up and write complete frames
    public void write(int b) throws IOException {
        frame[length++] = (byte)b;
        if (length == frame.length) {
            sound.write(frame, 0, length);
            length = 0;
        }
    }
    
    // Utility method to play from an input stream via an encoder
    public void play(InputStream in) throws IOException {
        encoder.encodeStart();
        encoder.encodeStream(in);
        encoder.encodeEnd();
    }
}
