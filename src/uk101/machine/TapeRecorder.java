/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.machine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk101.hardware.bus.IOBus;

/**
 * Program storage via 'tapes'.  This simple implementation just reads
 * and writes bytes from and to a file.
 *
 * It might not be _too_ hard to create a subclass of this that read and
 * writes to a real CUTS-encoded WAV file?
 *
 * @author Baldwin
 */
public class TapeRecorder implements IOBus {

    InputStream input;
    OutputStream output;
    Object reader;

    /*
     * Set new input and output tape streams
     */
    public void setInputTape(InputStream in) {
        // Close any existing stream before setting the new one
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        input = in;
        if (reader != null) {
            synchronized (reader) {
                reader.notify();
            }
        }
    }

    public void setOutputTape(OutputStream out) {
        // Close any existing stream before setting the new one
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        output = out;
    }

    public void shutdown() {
        setInputTape(null);
        setOutputTape(null);
    }

    /*
     * Implement the IOBus interface to allow the recorder to be be
     * hooked up to the output of the ACIA.
     */

    public int readByte() {
        int b = -1;
        if (input != null) {
            try {
                b = input.read();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        return b;
    }

    public void writeByte(int value) {
        if (output != null) {
            try {
                output.write(value);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    public void setListener(Object listener) {
        reader = listener;
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "Tape Recorder: input=" + input + " output=" + output;
    }
}
