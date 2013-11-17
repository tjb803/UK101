/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2013
 */
package uk101.machine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk101.hardware.bus.IOBus;
import uk101.hardware.bus.IODevice;
import uk101.view.CassetteView;

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

    IODevice acia;
    InputStream input;
    OutputStream output;
    
    public TapeRecorder(IODevice io) {
        acia = io;
    }

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
        acia.setRxBus((in == null) ? null : this);
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
        acia.setTxBus((out == null) ? null : this);
    }

    public void shutdown() {
        setInputTape(null);
        setOutputTape(null);
    }

    /*
     * Implement the IOBus interface to allow the recorder to be be
     * hooked up to the ACIA.
     */

    public int readByte() {
        int b = -1;
        if (input != null) {
            try {
                b = input.read();
            } catch (IOException e) {
                System.err.println(e);
            }
            if (view != null) {
                view.setActive();
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
            if (view != null) {
                view.setActive();
            }
        }
    }

    /*
     * GUI visualisation
     */

    CassetteView view;

    public void setView(CassetteView view) {
        this.view = view;
    }

    
    /*
     * Mainly for debugging
     */
    public String toString() {
        return "Tape Recorder: input=" + input + " output=" + output;
    }
}
