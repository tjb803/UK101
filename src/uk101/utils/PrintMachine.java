/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;

import uk101.machine.Configuration;
import uk101.machine.Cpu;
import uk101.machine.Dump;
import uk101.machine.Trace;
import uk101.view.MachineImage;
import uk101.view.ViewImage;

/**
 * Print the contents of a saved machine image.  Mainly for debugging.
 * 
 * Usage:
 *    PrintMachine [options] machine [outputfile]
 *
 * where:
 *    machine: is the name of a saved machine image
 *    outputfile: is the name of the formatted output file, default is standard-out.
 *
 * options:
 *    -hex: Include memory dump as hex, default is no memory dump
 *    -code: Include memory dump as disassembled code
 */
public class PrintMachine {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("hex");
        options.put("code");
        Args parms = new Args(PrintMachine.class, "machine [outputfile]", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        boolean asHex = parms.getFlag("hex");
        boolean asCode = parms.getFlag("code");

        // Check parameters
        if (inputFile == null) {
            parms.usage();
        }

        // Create input and output streams
        PrintStream output = System.out;
        if (outputFile != null) {
            output = new PrintStream(outputFile);
        }

        // Machine image consists of a memory dump followed by an 
        // optional view image with all window positions and machine
        // configuration.
        MachineImage image = MachineImage.readImage(inputFile);
        Dump dump = image.imageDump;
        Cpu cpu = image.imageCpu;
        Dump video = image.imageVid;
        Configuration config = image.imageCfg;
        ViewImage view = image.imageView;

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        output.println("Compukit UK101 Machine Image");
        output.println("created: " + df.format(dump.timestamp));
        output.println();

        // Start with CPU state, if we have it
        if (cpu != null) {
            output.println("CPU:");
            output.print("  ");
            new PrintTrace(output).print(new Trace.Entry(cpu));
        } else {
            output.println("No CPU state.");
        }
        output.println();

        // Then the memory dump
        if (asHex || asCode) {
            output.println("RAM:");
            InputStream ram = new ByteArrayInputStream(dump.store);
            if (asHex) {
                new PrintBytes(output, true).print(0, ram);
            } else {
                new PrintCode(output).print(0, ram);
            }
        } else {
            output.println("RAM omitted.");
        }
        output.println();

        // Then the video RAM, if we have it and want it
        if (video != null) {
            if (asHex || asCode) {
                output.println("Video:");
                InputStream vid = new ByteArrayInputStream(video.store);
                int vaddr = config != null ? config.getVideoAddr() : 0xD000;
                new PrintBytes(output, true).print(vaddr, vid);
            } else {
                output.println("Video RAM omitted.");
            }
            output.println();
        }

        // Then any saved configuration
        if (config != null) {
            output.println("Configuration:");
            output.print("  ");
            output.print(config.toString().replace("\n", "\n  "));
        } else {
            output.println("No configuration.");
        }
        output.println();

        // Finally the saved window positions
        if (view != null) {
            output.println("View details:");
            output.print(view);
        } else {
            output.println("No view details.");
        }
        output.println();
    }
}
