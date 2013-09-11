/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import uk101.io.UK101InputStream;

/**
 * Utility program to take an input ASCII file and write an equivalent
 * UK101 binary tape.
 *
 * Usage:
 *    TapeWrite [options] inputfile outputtape
 *
 * where:
 *    inputfile: the name of the text input file
 *    outputtape: the name for the output tape
 *
 * options:
 *    -encoding encoding_name: the encoding of the input file, defaults to the
 *                             standard platform encoding
 *
 * @author Baldwin
 */
public class TapeWrite {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("encoding", "inputencoding");
        Args parms = new Args("TapeWrite", "inputfile outputtape", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        String encoding = parms.getOption("encoding");

        // Check parameters
        if (inputFile == null || outputFile== null) {
            parms.usage();
        }

        // Create input/output streams
        FileOutputStream output = new FileOutputStream(outputFile);
        UK101InputStream input = null;
        if (encoding == null) {
            input = new UK101InputStream(new FileReader(inputFile));
        } else {
            input = new UK101InputStream(new InputStreamReader(new FileInputStream(inputFile), encoding));
        }

        // Write the input to the output
        input.write(output);
        output.close();
        input.close();
    }
}
