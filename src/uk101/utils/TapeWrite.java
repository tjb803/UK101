/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2017
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import uk101.io.KansasCityDecoder;
import uk101.io.KansasCityEncoder;
import uk101.io.Tape;

/**
 * Utility program to produce a UK101 binary format tape.  Input can be one 
 * or more ASCII, binary or audio tapes which are combined into a single output.
 *
 * Usage:
 *    TapeWrite [options] inputfile(s) outputtape
 *
 * where:
 *    inputfile(s): the names of the ASCII, binary or audio input files
 *    outputtape: the name for the output tape
 *
 * options:
 *    -baud: the baud rate if the file is an audio file, defaults to 300
 *    -phase: the audio phase angle if the file is an audio file, defaults to 90
 *    -adaptive: use adaptive audio decoding, defaults to false
  */
public class TapeWrite {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("adaptive");
        options.put("baud", "baudrate (300, 600 or 1200)");
        options.put("phase", "phaseangle (0, 90, 180 or 270)");
        Args parms = new Args(TapeWrite.class, "inputfile(s) outputtape", args, options);
        int count = parms.getParameterCount();
 
        List<File> inputFiles = parms.getInputFiles(1, count-1);
        File outputFile = parms.getOutputFile(count);
        int baudRate = parms.getInteger("baud", KansasCityEncoder.BAUD300);
        int phase = parms.getInteger("phase", 90);
        boolean adpative = parms.getFlag("adaptive");

        // Check parameters
        if ((inputFiles.isEmpty() || outputFile == null) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200) ||
                (phase%90 != 0)) {
            parms.usage();
        }

        // Create decoder for audio input and create output stream
        KansasCityDecoder decoder = new KansasCityDecoder(baudRate, phase);
        OutputStream output = Tape.getOutputStream(outputFile, Tape.STREAM_BINARY, null);
        
        // Copy the inputs to the output
        decoder.setAdaptive(adpative);
        for (File inputFile : inputFiles) {    
            InputStream input = Tape.getInputStream(inputFile, Tape.STREAM_SELECT, decoder);
            Tape.copy(input, output);
            input.close();
        }    
        output.close();
    }
}
