/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Utility class to manipulate tape input and output streams.
 */
public class Tape {

    public static final int STREAM_SELECT = 0;
    public static final int STREAM_ASCII = 1;
    public static final int STREAM_BINARY = 2;
    public static final int STREAM_AUDIO = 3;
    public static final int STREAM_UNKNOWN = -1;

    /*
     * Create an OutputStream in either ASCII, Binary or Audio format
     */
    public static OutputStream getOutputStream(File file, int format, int maxlen, AudioEncoder enc) {
        OutputStream out = null;
        try {
            if (format == STREAM_ASCII) {
                out = new UK101OutputStream(new FileWriter(file), maxlen);
            } else if (format == STREAM_BINARY) {
                out = new FileOutputStream(file);
            } else if (format == STREAM_AUDIO) {
                out = new WaveOutputStream(new FileOutputStream(file), enc);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return out;
    }

    /*
     * Create an InputStream in either ASCII or Binary format, with the option
     * to automatically select the format.
     */
    public static InputStream getInputStream(File file, int format, AudioDecoder dec) {
        InputStream in = null;
        try {
            if (format == STREAM_SELECT) {
                format = checkFormat(file, dec);
            }
            if (format == STREAM_ASCII) {
                in = new UK101InputStream(new FileReader(file));
            } else if (format == STREAM_BINARY) {
                in = new FileInputStream(file);
            } else if (format == STREAM_AUDIO) {
                in = new WaveInputStream(new FileInputStream(file), dec);
            }
        } catch (IOException e) {
            System.err.println(e);
        } catch (UnsupportedAudioFileException e) {
            System.err.println(e);
        }
        return in;
    }

    /*
     * Check a file format to try to determine if it is audio, binary or ASCII.  
     * Assume binary if the file contains any non-ASCII character bytes.
     * Note: only checks first 256 bytes of the file.
     */
    public static int checkFormat(File file, AudioDecoder dec) throws IOException {
        int format = STREAM_ASCII;
        byte[] b = new byte[256];
        InputStream in = new FileInputStream(file);
        int size = in.read(b);
        if (dec != null && size > 3 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F') {
            format = STREAM_AUDIO;
        } else {
            for (int i = 0; i < size && format == STREAM_ASCII; i++) {
                if ((b[i] < 32 || b[i] > 126) && b[i] != '\r' && b[i] != '\n' && b[i] != '\t')
                    format = STREAM_BINARY;
                else if ((b[i] == '\r' && i+1 < size && b[i+1] != '\n'))
                    format = STREAM_BINARY;
            }
        }
        in.close();
        return format;
    }

    /*
     * Return the format of an input/output stream 
     */
    public static int getFormat(InputStream stream) {
        return (stream instanceof WaveInputStream) ? STREAM_AUDIO :
                (stream instanceof UK101InputStream) ? STREAM_ASCII : STREAM_BINARY;
    }

    public static int getFormat(OutputStream stream) {
        return (stream instanceof WaveOutputStream) ? STREAM_AUDIO : 
                (stream instanceof UK101OutputStream) ? STREAM_ASCII : STREAM_BINARY;
    }

    /*
     * Copy one stream to another
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        for (int b = in.read(); b != -1; b = in.read()) {
            out.write(b);
        }
    }
}
