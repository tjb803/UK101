/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
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
 * and writes bytes from and to any Input/OutputStream.
 *
 * By using the WaveInputStream and WaveOutputStream classes this can be
 * made to read or write Kansas City encoded audio data.
 */
public class TapeRecorder implements IOBus {

    private IODevice acia;
    private InputStream input;
    private OutputStream output;

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

    public void startTape() {
        if (input != null)
            acia.setRxBus(this);
        if (output != null)
            acia.setTxBus(this);
    }

    public void stopTape() {
        acia.setRxBus(null);
        acia.setTxBus(null);
    }

    public void ejectTape() {
        stopTape();
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
            setActive(false);
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
            setActive(true);
        }
    }

    /*
     * GUI visualisation
     */

    private CassetteView view;

    public void setView(CassetteView view) {
        this.view = view;
    }

    private void setActive(boolean write) {
        if (view != null) {
            view.setActive(write);
        }
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "Tape Recorder: input=" + input + " output=" + output;
    }
}
