/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.machine;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A simple sound output device implemented as an OutputStream. 
 *
 * Note: data is expected to be written as byte arrays containing one or 
 *       more complete sound sample frames.  Single bytes cannot be written.
 *
 * @author Baldwin
 */
public class Loudspeaker extends OutputStream {
    
    private SourceDataLine sound;
    
    public Loudspeaker(AudioFormat format) throws LineUnavailableException {
        sound = AudioSystem.getSourceDataLine(format);
    }
    
    public void open() throws LineUnavailableException {
        sound.open();
    }
    
    public void close() {
        sound.drain();
        sound.stop();
        sound.close();
    }
    
    public void write(byte[] b) {
        sound.write(b, 0, b.length);
        sound.start();  
    }

    public void write(int b) throws IOException {
        throw new IOException();    // Cannot write single bytes
    }
}
