/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * This class will disassemble and print a 6502 binary image.
 *
 * Usage:
 *    PrintCode [options] bytesfile [address]
 *
 * where:
 *    bytesfile: is the name of a file of bytes
 *    address: is the base address of the first byte in the file, in decimal
 *             or hex (possibly starting with a '$' or a '0x').
 *
 * options:
 *    -output outputfile: output file name, defaults to standard out
 *
 * @author Baldwin
 */
public class PrintCode {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Map<String,String> options = Args.optionMap();
        options.put("output", "outputfile");
        Args parms = new Args("PrintCode", "bytesfile [address]", args, options);
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

        new PrintCode(output).print(address, input);
    }

    /*
     * Instances of this class can be used by other utilities
     */

    PrintStream output;

    public PrintCode(PrintStream output) {
        this.output = output;
    }

    public void print(int address, InputStream input) throws IOException {
        // Read the image to a byte array
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int b = input.read(); b != -1; b = input.read()) {
            bout.write(b);
        }

        // Use the disassembler to format the image
        Disassembler disasm = new Disassembler(bout.toByteArray(), address);
        while (disasm.hasNext()) {
            output.println(disasm.nextInstruction());
        }
    }
}
