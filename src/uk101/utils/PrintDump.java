/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;

import uk101.machine.Dump;

/**
 * This class will print a formatted system dump.
 *
 * Usage:
 *    PrintDump [options] dumpfile
 *
 * where:
 *    dumpfile: is the name of a system dump
 *
 * options:
 *    -output outputfile: output file name, defaults to standard out
 *    -hex: print output as a hex dump, default is hex format
 *    -code: print output as disassembled code
 */
public class PrintDump {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("output", "outputfile");
        options.put("hex");
        options.put("code");
        Args parms = new Args("PrintDump", "dumpfile", args, options);
        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile("output");
        boolean asHex = parms.getFlag("hex");
        boolean asCode = parms.getFlag("code");

        // Check parameters
        if (inputFile == null || (asHex && asCode)) {
            parms.usage();
        }

        // Create input and output streams
        PrintStream output = System.out;
        if (outputFile != null) {
            output = new PrintStream(outputFile);
        }

        // Read the memory dumps
        Dump dump = Dump.readDump(inputFile);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);

        if (dump != null) {
            output.println("UK101 Memory Dump");
            output.println("created: " + df.format(dump.timestamp));

            InputStream bytes = new ByteArrayInputStream(dump.store);
            output.println();
            if (asCode) {
                new PrintCode(output).print(0, bytes);
            } else {
                new PrintBytes(output).print(0, bytes);
            }
            output.println();
        }
    }
}
