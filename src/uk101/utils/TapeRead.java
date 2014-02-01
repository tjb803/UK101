/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import uk101.io.KansasCityDecoder;
import uk101.io.Stream;
import uk101.io.UK101OutputStream;

/**
 * Utility program to take a UK101 binary or audio input and print or 
 * write an ASCII file representation.
 *
 * Usage:
 *    TapeRead [options] inputtape [outputfile]
 *
 * where:
 *    inputtape: the name of the binary or audio tape to be read
 *    outputfile: the name for the output file, defaults to standard output
 *
 * options:
 *    -binary: input is binary, defaults to auto-selected
 *    -baudRate: the baud rate if the file is an audio file, defaults to 300
 *
 * @author Baldwin
 */
public class TapeRead {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("baudRate", "baudrate (300, 600 or 1200");
        Args parms = new Args("TapeRead", "inputtape [outputfile]", args, options);
        
        File inputFile = parms.getInputFile(1); 
        File outputFile = parms.getOutputFile(2);    
        int inputFormat = parms.getFlag("binary") ? Stream.STREAM_BINARY : Stream.STREAM_SELECT;
        int baudRate = parms.getInteger("baudRate", 300);

        // Check parameters
        if ((inputFile == null) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200)) {
            parms.usage();
        }

        // Create input/output streams/readers
        KansasCityDecoder decoder = new KansasCityDecoder(baudRate);
        InputStream input = Stream.getInputStream(inputFile, inputFormat, decoder);
        UK101OutputStream output = null;
        if (outputFile != null) {
            output = new UK101OutputStream(new PrintWriter(outputFile));
        } else {
            output = new UK101OutputStream(System.out);
        }
    
        // Copy the input to the output
        Stream.copy(input, output);
        output.close();
        input.close();
    }
}
