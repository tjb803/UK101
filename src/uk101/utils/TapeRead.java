/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import uk101.io.UK101OutputStream;

/**
 * Utility program to take a UK101 binary tape and print or write an ASCII
 * file representation.
 *
 * Usage:
 *    TapeRead [options] inputtape [outputfile]
 *
 * where:
 *    inputtape: the name of the binary tape to be read
 *    outputfile: the name for the output file, defaults to standard output
 *
 * options:
 *    -encoding encoding_name: the encoding of the output file, defaults to the
 *                             standard platform encoding
 *
 * @author Baldwin
 */
public class TapeRead {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("encoding", "outputencoding");
        Args parms = new Args("TapeRead", "inputtape [outputfile]", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        String encoding = parms.getOption("encoding");

        // Check parameters
        if (inputFile == null) {
            parms.usage();
        }

        // Create input/output streams/readers
        FileInputStream input = new FileInputStream(inputFile);
        UK101OutputStream output = null;
        if (outputFile != null) {
            if (encoding == null)
                output = new UK101OutputStream(new PrintWriter(outputFile));
            else
                output = new UK101OutputStream(new PrintWriter(outputFile, encoding));
        } else {
            if (encoding == null)
                output = new UK101OutputStream(System.out);
            else
                output = new UK101OutputStream(new OutputStreamWriter(System.out, encoding));
        }

        // Print the input to the output
        output.write(input);
        output.close();
    }
}
