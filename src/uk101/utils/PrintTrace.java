/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;

import uk101.hardware.CPU6502;
import uk101.machine.Data;
import uk101.machine.Trace;

/**
 * This class will print a formatted instruction trace.
 *
 * Usage:
 *    PrintTrace [options] tracefile
 *
 * where:
 *    tracefile: is the name of an instruction trace file
 *
 * options:
 *    -output outputfile: output file name, defaults to standard out
 *
 * Trace output format is
 *
 * location  bb bb bb  <instruction> ; A=$xx[c] X=$xx Y=$xx S=$xx P=<flags> <signals>  EA=<addr>,<data>
 *
 * location: address location
 * bb bb bb: instruction bytes
 * <instruction>: disassembled instruction
 * A=xx etc: register values before execution
 * <flags>: Flags bits, set UPPER clear lower: [N]egative o[V]erflow [B]reak [D]ecimal [I]nterrupt [Z]ero [C]arry 
 * <signals>: CPU signals raised before execution: [R]ST [N]MI [I]RQ
 * EA=addr,data: effective address of instruction (if it addresses memory) and data byte
 */
public class PrintTrace {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("output", "outputfile");
        Args parms = new Args(PrintTrace.class, "tracefile", args, options);
        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile("output");

        // Check parameters
        if (inputFile == null) {
            parms.usage();
        }

        // Create input and output streams
        PrintStream output = System.out;
        if (outputFile != null) {
            output = new PrintStream(outputFile);
        }

        // Read the trace file
        Trace trace = Trace.readTrace(inputFile);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);

        output.println("UK101 6502 Instruction Trace");
        output.println("created: " + df.format(trace.timestamp));
        output.println();

        // Format the output
        PrintTrace tracer = new PrintTrace(output);
        for (Trace.Entry entry = trace.nextEntry(); entry != null; entry = trace.nextEntry()) {
            tracer.print(entry);
        }
        output.println();
    }

    /*
     * Instances of this class can be used by other utilities
     */

    private static final String SPACES32 = "                                ";

    private PrintStream output;
    private Disassembler disasm;

    public PrintTrace(PrintStream output) {
        this.output = output;
        disasm = new Disassembler();
    }

    public void print(Trace.Entry entry) throws IOException {
        disasm.reset(entry.instruction, entry.length, 0, entry.PC, entry.PC);
        String instr = disasm.nextInstruction();
        output.print(instr + SPACES32.substring(instr.length()) + ";");
        output.print(" A=" + Data.toHexString(entry.A));
        if (entry.A > 31 && entry.A < 127)
            output.print("[" + Character.toString((char)entry.A) + "]");
        else
            output.print("   ");
        output.print(" X=" + Data.toHexString(entry.X));
        output.print(" Y=" + Data.toHexString(entry.Y));
        output.print(" S=" + Data.toHexString(entry.S));
        output.print(" P=" + CPU6502.toFlagString(entry.P));
        output.print(" " + CPU6502.toSigString(entry.RST, entry.NMI, entry.IRQ));
        if (disasm.instrMode > Disassembler.MODE_RELATIVE) {
            output.print("  EA=" + Data.toHexString(entry.addr));
            output.print("," + Data.toHexString(entry.data));
        }
        output.println();
    }
}
