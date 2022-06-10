/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import uk101.io.KansasCityDecoder;
import uk101.io.Tape;
import uk101.io.UK101OutputStream;

/**
 * Utility program produce an ASCII representation of a UK101 tape file.
 * The input file can be binary, ASCII or audio encoded (although ASCII 
 * would be a little pointless).
 *
 * Usage:
 *    TapeRead [options] inputtape [outputfile]
 *
 * where:
 *    inputtape: the name of the binary or audio tape to be read
 *    outputfile: the name for the output file, defaults to standard-out
 *
 * options:
 *    -binary: input is binary, defaults to auto-selected
 *    -maxlen: the maximum line length used for output, defaults to 132 or 80
 *    -baud: the baud rate if the file is an audio file, defaults to 300
 *    -phase: the audio phase angle if the file is an audio file, defaults to 90
 *
 * experimental options:
 *    -adaptive: use adaptive audio decoding, defaults to false
 *    -freq: the audio frequency used to encode "mark" bits
 *    -cycles: the number of cycles used to encode "mark" bits
 */
public class TapeRead {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("maxlen", "length (maximum line length)");
        options.put("baud", "baudrate (300, 600 or 1200)");
        options.put("phase", "-phaseangle (0, 90, 180 or 270)");
        options.put("adaptive", "?(audio adaptive decoding - experimental)");
        options.put("freq", "frequency (audio mark frequency - experimental)");
        options.put("cycles", "count (audio mark cycles - experimental)");
        Args parms = new Args(TapeRead.class, "inputtape [outputfile]", args, options);

        File inputFile = parms.getInputFile(1);
        File outputFile = parms.getOutputFile(2);
        int inputFormat = parms.getFlag("binary") ? Tape.STREAM_BINARY : Tape.STREAM_SELECT;
        int maxLength = parms.getInteger("maxlen", -1);
        int baudRate = parms.getInteger("baud", 300);
        int phaseAngle = parms.getInteger("phase", 90);

        // Audio freq and cycles are experimental options
        boolean adaptive = parms.getFlag("adaptive");
        int freq = parms.getInteger("freq", 0);
        int cycles = parms.getInteger("cycles", 0);

        // Check parameters
        if ((inputFile == null) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200) ||
                (phaseAngle%90 != 0)) {
            parms.usage();
        }

        // Choose maximum line length - 80 for screen output otherwise 132.
        if (maxLength < 0) {
            maxLength = (outputFile == null) ? 80 : 132;
        }

        // Create input/output streams and decoder
        KansasCityDecoder decoder = new KansasCityDecoder(baudRate, phaseAngle);
        if (freq > 0 && cycles > 0) {
            decoder = new KansasCityDecoder(freq, cycles, phaseAngle);
        }
        decoder.setAdaptive(adaptive);

        InputStream input = Tape.getInputStream(inputFile, inputFormat, decoder);
        OutputStream output = null;
        if (outputFile != null) {
            output = Tape.getOutputStream(outputFile, Tape.STREAM_ASCII, maxLength, null);
        } else {
            output = new UK101OutputStream(System.out, maxLength);
        }

        // Copy the input to the output
        Tape.copy(input, output);
        output.close();
        input.close();
    }
}
