/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.machine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Capture and record a system memory dump.
 *
 * @author Baldwin
 */
public class Dump implements Serializable {
    private static final long serialVersionUID = 1L;

    // Identification
    public String name;        // System name;
    public String version;     // System version
    public Date timestamp;     // Time-stamp of dump

    // Storage dump
    public byte[] store;

    /*
     * Create a dump
     */
    public Dump(Computer computer) {
        name = computer.name;
        version = computer.version;
        timestamp = new Date();

        store = computer.ram.store;
    }

    /*
     * Write a dump file.
     */
    public void write() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
        String filename = "uk101-" + df.format(timestamp) + ".ram";
         try {
            OutputStream stream = new DeflaterOutputStream(new FileOutputStream(filename));
            write(stream);
            stream.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void write(OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(this);
        out.flush();
    }

   /*
    * Read a dump file.
    */
    public static Dump readDump(File file) {
        Dump dump = null;
        try {
            InputStream stream = new InflaterInputStream(new FileInputStream(file));
            dump = readDump(stream);
            stream.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return dump;
    }

    public static Dump readDump(InputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(stream);
        Dump dump = (Dump)in.readObject();
        return dump;
    }
}
