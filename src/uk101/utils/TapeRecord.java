/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2017
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import uk101.io.AudioEncoder;
import uk101.io.KansasCityDecoder;
import uk101.io.KansasCityEncoder;
import uk101.io.Tape;
import uk101.io.WaveOutputStream;

/**
 * Utility program to produce audio encoded tapes.  Input can be one 
 * or more ASCII, binary or audio tapes which are combined into a single
 * output WAV file.
 *
 * Usage:
 *    TapeRecord [options] inputfile(s) outputfile 
 *
 * where:
 *    inputfile(s): the names of the ASCII, binary or audio input files
 *    outputfile: the name for the output WAV file
 *
 * options:
 *    -binary: input is binary, defaults to auto-selected
 *    -sampleRate: audio sample rate (default 48kHz)
 *    -sampleSize: audio sample size (default 16 bits)
 *    -baud: audio baud rate (default 300)
 *    -leadIn: time to play lead-in tone (default to 5s)
 *    -leadOut: time to play lead-out tone (defaults to leadIn) 
 *    -leadGap: time between tape segments (default to 2s)
 *    -sineWave: generate pure sine wave audio tones 
 *    -inputBaud: audio baud rate of input, if audio encoded (defaults to baudRate)
 *    -inputPhase: audio phase angle of input, if audio encoded (defaults to 90) 
 */
public class TapeRecord {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("sampleRate", "samplerate (8000 to 96000)");
        options.put("sampleSize", "samplesize (8 or 16)");
        options.put("baud", "baudrate (300, 600 or 1200)");
        options.put("leadIn", "+leadin");
        options.put("leadOut", "+leadout");
        options.put("leadGap", "segmentgap");
        options.put("sineWave");
        options.put("inputBaud", "inputbaudrate (300, 600 or 1200)");
        options.put("inputPhase", "inputphaseangle (0, 90, 180 or 270)");
        Args parms = new Args(TapeRecord.class, "inputfile(s) outputfile", args, options);
        int count = parms.getParameterCount();
        
        List<File> inputFiles = parms.getInputFiles(1, count-1);
        File outputFile = parms.getOutputFile(count);
        int inputFormat = parms.getFlag("binary") ? Tape.STREAM_BINARY : Tape.STREAM_SELECT;
        int sampleRate = parms.getInteger("sampleRate", AudioEncoder.RATE48K);
        int sampleSize = parms.getInteger("sampleSize", AudioEncoder.BIT16);
        int baudRate = parms.getInteger("baud", KansasCityEncoder.BAUD300);
        int leadIn = parms.getInteger("leadIn", 5);
        int leadOut = parms.getInteger("leadOut", leadIn);
        int leadGap = parms.getInteger("leadGap", 2);
        int inputBaud = parms.getInteger("inputBaud", baudRate);
        int inputPhase = parms.getInteger("inputPhase", 90);
        boolean sineWave = parms.getFlag("sineWave");

        // Check parameters
        if ((inputFiles.isEmpty() || outputFile == null) ||
                (sampleRate < 8000 || sampleRate > 96000) ||
                (sampleSize != 8 && sampleSize != 16) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200) ||
                (inputBaud != 300 && inputBaud != 600 && inputBaud != 1200) ||
                (inputPhase%90 != 0)) {
            parms.usage();
        }
        
        // Create encoder/decoder and audio output stream.
        KansasCityDecoder decoder = new KansasCityDecoder(inputBaud, inputPhase);
        KansasCityEncoder encoder = new KansasCityEncoder(sampleRate, sampleSize, baudRate, sineWave);
        encoder.setLeader(leadIn*1000, leadOut*1000);
        OutputStream output = Tape.getOutputStream(outputFile, Tape.STREAM_AUDIO, encoder);
        
        // Copy the inputs to the output
        count = 0;
        for (File inputFile : inputFiles) {    
            InputStream input = Tape.getInputStream(inputFile, inputFormat, decoder);
            if (count++ > 0) {
                encoder.setLeader(leadGap*1000, leadOut*1000);
                ((WaveOutputStream)output).reset();
            }
            Tape.copy(input, output);
            input.close();
        }    
        output.close();
    }
}
