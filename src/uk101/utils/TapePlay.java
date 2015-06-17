/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015
 */
package uk101.utils;

import java.io.File;
import java.io.InputStream;

import uk101.io.AudioEncoder;
import uk101.io.KansasCityDecoder;
import uk101.io.KansasCityEncoder;
import uk101.io.Tape;
import uk101.machine.Loudspeaker;

/**
 * Utility program to take ASCII, binary or audio input and play as 
 * Kansas City encoded audio to the speaker.
 *
 * Usage:
 *    TapePlay [options] inputfile 
 *
 * where:
 *    inputfile: the name of the ASCII, binary or audio input file
 *
 * options:
 *    -binary: input is binary, defaults to auto-selected
 *    -sampleRate: audio sample rate (default 48kHz)
 *    -sampleSize: audio sample size (default 16 bits)
 *    -baudRate: audio baud rate (default 300)
 *    -leadIn: time to play lead-in tone (default to 5s)
 *    -leadOut: time to play lead-out tone (defaults to leadIn) 
 *    -inputRate: audio baud rate of input, if audio encoded (defaults to baudRate)
 *    -systemWave: generate system waveform, rather than pure sine waves
 */
public class TapePlay {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("binary");
        options.put("sampleRate", "samplerate (8000 to 96000)");
        options.put("sampleSize", "samplesize (8 or 16)");
        options.put("baudRate", "baudrate (300, 600 or 1200)");
        options.put("leadIn", "+leadin");
        options.put("leadOut", "leadout");
        options.put("inputRate", "inputBaudRate (300, 600 or 1200");
        options.put("systemWave");
        Args parms = new Args(TapePlay.class, "inputfile", args, options);

        File inputFile = parms.getInputFile(1);
        int inputFormat = parms.getFlag("binary") ? Tape.STREAM_BINARY : Tape.STREAM_SELECT;
        int sampleRate = parms.getInteger("sampleRate", AudioEncoder.RATE48K);
        int sampleSize = parms.getInteger("sampleSize", AudioEncoder.BIT16);
        int baudRate = parms.getInteger("baudRate", KansasCityEncoder.BAUD300);
        int leadIn = parms.getInteger("leadIn", 5);
        int leadOut = parms.getInteger("leadOut", leadIn);
        int inputRate = parms.getInteger("inputRate", baudRate);
        boolean sineWave = !parms.getFlag("systemWave");

        // Check parameters
        if ((inputFile == null) ||
                (sampleRate < 8000 || sampleRate > 96000) ||
                (sampleSize != 8 && sampleSize != 16) ||
                (baudRate != 300 && baudRate != 600 && baudRate != 1200) ||
                (inputRate != 300 && inputRate != 600 && inputRate != 1200)) {
            parms.usage();
        }

        // Create input stream and encoders/decoders
        KansasCityDecoder decoder = new KansasCityDecoder(inputRate);
        KansasCityEncoder encoder = new KansasCityEncoder(sampleRate, sampleSize, baudRate, sineWave);
        encoder.setLeader(leadIn*1000, leadOut*1000);
        InputStream input = Tape.getInputStream(inputFile, inputFormat, decoder);
        
        // Create a speaker for output and play the input file
        Loudspeaker speaker = new Loudspeaker(encoder);
        speaker.open();
        speaker.play(input);
        speaker.close();
        input.close(); 
    }
}
