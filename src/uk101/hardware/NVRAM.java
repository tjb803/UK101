/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015,2022
 */
package uk101.hardware;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Non-volatile RAM is read/write memory backed by a persistent disk file.
 * Read operations are fast as the content is cached, but write is 
 * slow as we write-through to the backing file.
 */
public class NVRAM extends ROM {

    private RandomAccessFile file;

    /*
     * NVRAM should only be installed if the file is available for writing
     */
    public NVRAM(String id) throws IOException {
        super(validate(id));
        file = new RandomAccessFile(name, "rwd");
        file.setLength(bytes());
    }

    // Write should update file as well as store image
    public void writeByte(int offset, byte b) {
        super.writeByte(offset, b);
        try {
            file.seek(offset);
            file.writeByte(b);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    // Write out updated content on close
    public void close() {
        try {
            file.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    // Validate file exists and can be written
    private static String validate(String id) throws IOException {
        File f = new File(id);
        if (!f.exists() || !f.isFile() || !f.canWrite()) {
            throw new FileNotFoundException(id);
        }
        return f.getCanonicalPath();
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "NVRAM" + memBase() + ": " + bytes() + " " + name;
    }
}
