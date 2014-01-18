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
 * file in Kansas City encoded format.
 *
 * To write a WAV file we need to know its total length, so we have to 
 * build the RAW audio data in a temporary byte buffer before writing the 
 * WAV as part of the close operation.
 *
 * @author Baldwin
 */
public class WaveOutputStream extends OutputStream {
 
    OutputStream outputStream;
    KansasCityFormat kcFormat;
    KansasCityEncoder kcEncoder;
    ByteArrayOutputStream rawOutput;

    public WaveOutputStream(OutputStream out, KansasCityFormat format) {
        outputStream = out;
        kcFormat = format;
        kcEncoder = new KansasCityEncoder(kcFormat);
        rawOutput = new ByteArrayOutputStream();
    }
    
    /*
     * Override the required OutputStream methods
     */

    public void write(int b) throws IOException {
        if (rawOutput.size() == 0)
            kcEncoder.encodeTone(kcFormat.getLeadIn(), rawOutput);
        kcEncoder.encodeByte(b, rawOutput);
    }
    
    public void close() throws IOException {
        if (rawOutput.size() != 0) {
            kcEncoder.encodeTone(kcFormat.getLeadOut(), rawOutput);
            kcEncoder.encodeEnd(rawOutput);
            ByteArrayInputStream raw = new ByteArrayInputStream(rawOutput.toByteArray());
            int frames = raw.available() / kcFormat.getFrameSize();
            AudioInputStream audioIn = new AudioInputStream(raw, kcFormat, frames);
            AudioSystem.write(audioIn, Type.WAVE, outputStream);
            audioIn.close();
        }
        outputStream.close();
    }
    
    /*
     * Extra useful methods
     */
    
    public void write(InputStream input) throws IOException {
        for (int b = input.read(); b != -1; b = input.read())
            write(b);
    }
}
