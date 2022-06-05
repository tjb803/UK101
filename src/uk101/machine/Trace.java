/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.machine;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Capture and record a CPU instruction trace.
 */
public class Trace implements Serializable {
    private static final long serialVersionUID = 1L;

    static final int BUFFER_SIZE = 8192;

    /*
     * Trace entry
     */
    public static class Entry implements Serializable {
        private static final long serialVersionUID = 1L;

        public short PC;
        public byte A, X, Y, S, P;
        public boolean RST, NMI, IRQ;
        public byte[] instruction;
        public int length;
        public int addr;
        public byte data;

        public Entry(Cpu cpu) {
            PC = cpu.PC;
            A = cpu.A;  X = cpu.X;  Y = cpu.Y;
            S = cpu.S;
            P =cpu.P;
            RST = cpu.RST;  NMI = cpu.NMI;  IRQ = cpu.IRQ;
            instruction = new byte[3];
            length = 0;
        }

        // Update current entry with decoded instruction details
        public void addByte(byte b) {
            instruction[length++] = b;
        }

        public void addWord(short w) {
            instruction[length++] = Data.getLoByte(w);
            instruction[length++] = Data.getHiByte(w);
        }

        public void addAddr(int i, byte b) {
            addr = i;
            data = b;
        }
    }

    // Identification
    public String name;         // System name;
    public String version;      // System version
    public Date timestamp;      // Time-stamp of dump

    transient private Entry[] traceLog;
    transient private int maximum;
    transient private int position;

    transient private ObjectOutputStream out;
    transient private ObjectInputStream in;

    public Trace(Computer computer) {
        name = computer.name;
        version = computer.version;
        timestamp = new Date();

        maximum = BUFFER_SIZE;
        position = 0;
        traceLog = new Entry[maximum];
    }

    // Log a new trace entry
    public Entry trace(Cpu cpu) {
        if (position == maximum) {
            flush(false);
            position = 0;
        }
        Entry entry = new Entry(cpu);
        traceLog[position++] = entry;
        return entry;
    }

    // Write and close the trace file
    public void write() {
        flush(true);
    }

    // Flush buffer to disk
    synchronized void flush(boolean close) {
        if (out == null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
            String filename = "uk101-" + df.format(timestamp) + ".trace";
            try {
                FileOutputStream fout = new FileOutputStream(filename);
                DeflaterOutputStream zout = new DeflaterOutputStream(fout);
                out = new ObjectOutputStream(zout);
                out.writeObject(this);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
        if (out != null) {
            try {
                for (int i = 0; i < position; i++) {
                    out.writeObject(traceLog[i]);
                }
                if (close) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    // Return trace entries from saved trace file
    public Entry nextEntry() {
        Entry entry = null;
        if (in != null) {
            try {
                entry = (Entry)in.readObject();
            } catch (EOFException e) {
                // Assume end of file reached
                try {
                    in.close();
                    in = null;
                } catch (IOException e1) {
                    System.err.println(e1);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        return entry;
    }

    /*
     * Read a trace file
     */
    @SuppressWarnings("resource")
    public static Trace readTrace(File file) {
        Trace trace = null;
        try {
            InputStream stream = new InflaterInputStream(new FileInputStream(file));
            ObjectInputStream in = new ObjectInputStream(stream);
            trace = (Trace)in.readObject();
            trace.in = in;
        } catch (Exception e) {
            System.err.println(e);
        }
        return trace;
    }
}
