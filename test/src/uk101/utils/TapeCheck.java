/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2017,2022
 */
package uk101.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import uk101.io.CheckingDecoder;
import uk101.io.Tape;

/**
 * Utility program to check an audio file for potential errors.  The result
 * is a second WAV file containing markers at points possible errors are 
 * noted. As this is the same size as the original it can be loaded as a 
 * second track to help pinpoint places tp look for problems.
 *
 * Usage:
 *    TapeCheck [options] inputwav [outputwav]
 *
 * where:
 *    inputwav: the name of the audio tape to be checked
 *    outputwav: the name for the output WAV, defaults to input.check.wav
 *
 * options:
 *    -factor: the scaling factor for signal processing, defaults to 250
 *    -adaptive: use adaptive audio decoding
 */
public class TapeCheck {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("factor", "error_factor");
        options.put("baud", "baud_rate");
        options.put("phase", "phase_angle");
        options.put("freq", "mark_frequency");
        options.put("cycles", "mark_cycles");
        options.put("adaptive");
        Args parms = new Args(TapeCheck.class, "inputwav [outputwav]", args, options);

        File inputFile = parms.getInputFile(1); 
        File outputFile = parms.getOutputFile(2);
        int factor = parms.getInteger("factor", 250);
        int baud = parms.getInteger("baud", 300);
        int phase = parms.getInteger("phase", 90);
        boolean adaptive = parms.getFlag("adaptive");
        int freq = parms.getInteger("freq", 0);
        int cycles = parms.getInteger("cycles", 0);

        // Check parameters
        if (inputFile == null) {
            parms.usage();
        }

        if (outputFile == null) {
            String name = inputFile.getName().replace(".wav", "").concat(".check.wav");
            outputFile = new File(inputFile.getParentFile(), name);
        }

        // Create input/output stream and special checking decoder
        CheckingDecoder decoder;
        if (freq == 0 || cycles == 0) {
            decoder = new CheckingDecoder(baud, phase, adaptive, factor);
        } else {
            decoder = new CheckingDecoder(freq, cycles, phase, adaptive, factor);
        }
        InputStream input = Tape.getInputStream(inputFile, Tape.STREAM_AUDIO, decoder);
        OutputStream output = new FileOutputStream(outputFile);

        // Read through the input which will perform the checking
        int b = input.read();
        while (b != -1) 
            b = input.read();
        input.close();

        // Write the check data as a WAV file
        InputStream checkIn = decoder.getCheckStream();
        AudioFormat checkFmt = decoder.getCheckFormat();
        int frames = checkIn.available() /checkFmt.getFrameSize();
        AudioInputStream audioIn = new AudioInputStream(checkIn, checkFmt, frames);
        AudioSystem.write(audioIn, Type.WAVE, output);
        audioIn.close();
        output.close();
    }
}
