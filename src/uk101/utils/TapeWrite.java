/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import uk101.io.AudioEncoder;
import uk101.io.KansasCityDecoder;
import uk101.io.KansasCityEncoder;
import uk101.io.Tape;

/**
 * Utility program to take ASCII, binary or audio input and write an 
 * equivalent UK101 binary or audio tape
 *
 * Usage:
 *    TapeWrite [options] inputfile outputtape
 *
 * where:
 *    inputfile: the name of the ASCII, binary or audio input file
 *    outputtape: the name for the output tape
 *
 * options:
 *    -audio: create audio tape (default is binary)
 *    -sampleRate: audio sample rate (default 48kHz)
 *    -sampleSize: audio sample size (default 16 bits)
 *    -baudRate: audio baud rate (default 300)
 *    -leadIn: time to play lead-in tone (default to 5s)
 *    -leadOut: time to play lead-out tone (defaults to leadIn) 
 *    -inputRate: audio baud rate of input, if audio encoded (defaults to baudRate)
 */
public class TapeWrite {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("audio");
        options.put("sampleRate", "samplerate (8000 to 96000)");
        options.put("sampleSize", "samplesize (8 or 16)");
        options.put("baudRate", "baudrate (300, 600 or 1200)");
        options.put("leadIn", "+leadin");
        options.put("leadOut", "leadout");
        options.put("inputRate", "inputBaudRate (300, 600 or 1200");
        Args parms = new Args(TapeWrite.class, "inputfile outputtape", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        int outputFormat = parms.getFlag("audio") ? Tape.STREAM_AUDIO : Tape.STREAM_BINARY;
        int sampleRate = parms.getInteger("sampleRate", AudioEncoder.RATE48K);
        int sampleSize = parms.getInteger("sampleSize", AudioEncoder.BIT16);
        int baudRate = parms.getInteger("baudRate", KansasCityEncoder.BAUD300);
        int leadIn = parms.getInteger("leadIn", 5);
        int leadOut = parms.getInteger("leadOut", leadIn);
        int inputRate = parms.getInteger("inputRate", baudRate);

        // Check parameters
        if ((inputFile == null || outputFile == null) ||
                (sampleRate < 8000 || sampleRate > 96000) ||
                (sampleSize != 8 && sampleSize != 16) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200) ||
                (inputRate != 300 && inputRate != 600 && inputRate != 1200)) {
            parms.usage();
        }

        // Create input/output streams
        KansasCityEncoder encoder = new KansasCityEncoder(sampleRate, sampleSize, baudRate);
        KansasCityDecoder decoder = new KansasCityDecoder(inputRate);
        encoder.setLeader(leadIn*1000, leadOut*1000);
        InputStream input = Tape.getInputStream(inputFile, Tape.STREAM_SELECT, decoder); 
        OutputStream output = Tape.getOutputStream(outputFile, outputFormat, encoder);

        // Copy the input to the output
        Tape.copy(input, output);
        output.close();
        input.close();
    }
}
