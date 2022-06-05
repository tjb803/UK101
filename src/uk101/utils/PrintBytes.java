/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
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
 *    -compact: compact repeated data
 *
 * Output displays a hex byte dump and the equivalent ASCII characters
 */
public class PrintBytes {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("output", "outputfile");
        options.put("compact");
        Args parms = new Args(PrintBytes.class, "bytesfile [address]", args, options);

        File inputFile = parms.getInputFile(1);
        int address = parms.getHexInteger(2, 0);
        File outputFile = parms.getOutputFile("output");
        boolean compact = parms.getFlag("compact");

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
        new PrintBytes(output, compact).print(address, input);
        output.println();
    }

    /*
     * Instances of this class can be used by other utilities
     */

    private PrintStream output;
    private boolean compact;

    public PrintBytes(PrintStream output) {
        this(output, false);
    }

    public PrintBytes(PrintStream output, boolean compact) {
        this.output = output;
        this.compact = compact;
    }

    public void print(int address, InputStream input) throws IOException {

        String addr = "", data = "", chars = "";
        String lastAddr= "", lastData = "", lastChars= "";
        int skip = 0;

        byte[] bb = new byte[16];
        int size = input.read(bb);
        while (size != -1) {
            lastAddr = addr;
            lastData = data;
            lastChars = chars;

            addr = Data.toHexString(address);
            data = toData(bb, size);
            chars = toChars(bb, size);

            if (!compact || !data.equals(lastData)) {
                if (skip > 0) {
                    printRepeat(skip, lastAddr, lastData, lastChars);
                    skip = 0;
                }
                printLine(addr, data, chars);
            } else {
                skip += 1;
            }

            address += size;
            size = input.read(bb);
        }
        if (skip > 0) {
           printRepeat(skip-1, lastAddr, lastData, lastChars);
           printLine(addr, data, chars);
        }
    }

    private String toData(byte[] bb, int size) {
        String data = "";
        for (int i = 0; i < size; i++)
            data += " " + Data.toHexString(bb[i]);
        for (int i = size; i < 16; i++)
            data += "   ";
        return data;
    }

    private String toChars(byte[] bb, int size) {
        String chars = "[";
        for (int i = 0; i < size; i++)
            chars += (bb[i] > 31 && bb[i] < 127) ? Character.toString((char)bb[i]) : ".";
        chars += "]";
        return chars;
    }

    private void printRepeat(int skip, String addr, String data, String chars) {
        if (skip > 1)
            output.println("...");
        else if (skip > 0)
            printLine(addr, data, chars);
    }

    private void printLine(String addr, String data, String chars) {
        output.println(addr + ": " + data + "  " + chars);
    }
}
