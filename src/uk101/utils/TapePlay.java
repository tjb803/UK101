/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2014
 */
package uk101.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import uk101.io.AudioEncoder;
import uk101.io.WaveOutputStream;
import uk101.io.KansasCityEncoder;
import uk101.io.Stream;
import uk101.machine.Loudspeaker;

/**
 * Utility program to "play" a tape file either to the speaker or to
 * a .WAV output file. Output is 300 baud Kansas City encoded audio.
 *
 * Usage:
 *    TapePlay [options] inputfile [outputwav]
 *
 * where:
 *    inputfile: the name of the input file
 *    outputput: the name of the output WAV file
 *
 * options:
 *    -binary: input file is encoded in Binary 
 *    -ascii: input file is encoded in ASCII
 *    -sampleRate: sample rate, defaults to 44.1kHz         
 *    -sampleBits: sample bit size, defaults to 16 
 *    -baudRate: baud rate, defaults to 300
 *    -leadIn: time to play lead-in tone, defaults to 5s
 *    -leadOut: time to play lead-out tone, defaults to leadIn             
 *
 * @author Baldwin
 */
public class TapePlay {

    public static void main(String[] args) throws Exception { 
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("ascii");
        options.put("sampleRate", "sample rate (default 44100)");
        options.put("sampleBits", "sample bits (8 or 16, default 16)");
        options.put("baudRate", "baud rate (300, 600 or 1200, default 300)");
        options.put("leadIn", "lead-in tone (default 5)");
        options.put("leadOut", "lead-out tone (default 5)");
        Args parms = new Args("TapeWrite", "inputfile [outputwav]", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        boolean isBinary = parms.getFlag("binary");
        boolean isAscii = parms.getFlag("ascii");
        int rate = parms.getInteger("sampleRate", 44100);
        int bits = parms.getInteger("sampleBits", 16);
        int baud = parms.getInteger("baudRate", 300);
        int leadIn = parms.getInteger("leadIn", 5);
        int leadOut = parms.getInteger("leadOut", leadIn);
        
        // Check parameters
        if (inputFile == null || 
              (bits != 8 && bits != 16) || 
              (baud != 300 && baud != 600 && baud != 1200)) {
            parms.usage();
        }
        
        // Create input stream from binary or ascii encoded file
        int tapeFormat = isBinary ? Stream.STREAM_BINARY : isAscii ? Stream.STREAM_ASCII : Stream.STREAM_SELECT;
        InputStream input = Stream.getInputStream(inputFile, tapeFormat);
        
        // Encode and save or play the sound
        AudioEncoder encoder = new KansasCityEncoder(rate, bits, baud);
        encoder.setLeadInOut(leadIn*1000, leadOut*1000);
        if (outputFile != null) {
            // Saving the output to a WAV file
            WaveOutputStream output = new WaveOutputStream(new FileOutputStream(outputFile), encoder);
            output.write(input);
            output.close();
        } else {
            // Playing to the speaker
            Loudspeaker speaker = new Loudspeaker(encoder.getFormat());
            speaker.open();
            encoder.setOutputStream(speaker);
            encoder.encodeStart();
            encoder.encodeStream(input);
            encoder.encodeEnd();
            speaker.close();
        }
        input.close();
    }
}
