/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import uk101.machine.Data;

/**
 * This class will print a formatted memory dump.
 *
 * Usage:
 *    PrintBytes [options] bytesfile [address]
 *
 * where:
 *    bytesfile: is the name of a file of bytes
 *    address: is the base address of the first byte in the file, in decimal
 *             or hex (possibly starting with a '$' or a '0x').
 *
 * options:
 *    -output outputfile: output file name, defaults to standard out
 *
 * Output displays a hex byte dump and the equivalent ASCII characters
 */
public class PrintBytes {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("output", "outputfile");
        Args parms = new Args("PrintBytes", "bytesfile [address]", args, options);
        File inputFile = parms.getInputFile(1);
        int address = parms.getHexInteger(2);
        File outputFile = parms.getOutputFile("output");

        // Check parameters
        if (inputFile == null) {
            parms.usage();
        }

        // Create input and output streams
        InputStream input = new FileInputStream(inputFile);
        PrintStream output = System.out;
        if (outputFile != null) {
            output = new PrintStream(outputFile);
        }

        // Format the output
        new PrintBytes(output).print(address, input);
        output.println();
    }

    /*
     * Instances of this class can be used by other utilities
     */

    private PrintStream output;

    public PrintBytes(PrintStream output) {
        this.output = output;
    }

    public void print(int address, InputStream input) throws IOException {
        byte[] bb = new byte[16];
        int size = input.read(bb);
        while (size != -1) {
            output.print(Data.toHexString(address) + ": ");
            for (int i = 0; i < size; i++)
                output.print(" "+Data.toHexString(bb[i]));
            for (int i = size; i < 16; i++)
                output.print("   ");
            output.print("  [");
            for (int i = 0; i < size; i++)
                output.print((bb[i] > 31 && bb[i] < 127) ? Character.toString((char)bb[i]) : ".");
            for (int i = size; i < 16; i++)
                output.print(" ");
            output.println("]");
            address += size;
            size = input.read(bb);
        }
    }
}
