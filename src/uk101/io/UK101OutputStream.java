/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import uk101.machine.Data;

/**
 * Utility class to take a stream of output bytes written by the UK101 and convert
 * them to Java output characters using a Java Writer.  This should create a file
 * that is viewable and can be edited easily on the PC.
 *
 * The conversion rules are:
 *
 * - Any standard ASCII character (i.e. code value 32 to 126, except for a
 *   backslash) is written directly to the output.
 *
 * - Any non-ASCII character (i.e. code <32 or >126 or a backslash) is written
 *   as a hex escape beginning with a backslash '\nn'.
 *
 * - The sequence CR <NUL> LF is written as a line end (where <NUL>
 *   means zero or more NUL characters).
 *
 * - Long lines can be split using a trailing backslash character.
 */
public class UK101OutputStream extends OutputStream {

    private PrintWriter outputWriter;
    private int maxLength;

    // The writer needs to be wrapped in a PrintWriter (if it is not already
    // one) to allow correct handling of line ends.

    public UK101OutputStream(Writer out, int maxlen) {
        this(new PrintWriter(out, true), maxlen);
    }

    public UK101OutputStream(PrintWriter out, int maxlen) {
        outputWriter = out;
        maxLength = maxlen;
    }

    // Also allow for a PrintStream (typically System.out).

    public UK101OutputStream(PrintStream out, int maxlen) {
        this(new OutputStreamWriter(out), maxlen);
    }

    /*
     * Override the required OutputStream methods
     */

    private boolean pendingLineEnd;
    private int pendingNuls;
    private int lineLength;

    public void write(int ch) throws IOException {
        // If we're expecting a line-end, count up any pending NULs.  These will be 
        // ignored if it turns out to be a real line-end, but will need to 
        // be output if he line-end never happens.
        if (ch == 0x00 && pendingLineEnd) {
            pendingNuls += 1;
        } else {
            if (ch == 0x0A && pendingLineEnd) {
                // Real CR <NUL> LF line-end detected
                outputWriter.println();
                pendingLineEnd = false;
                lineLength = 0;
            } else {
                checkCR();
                if (ch == 0x0D) {
                    pendingLineEnd = true;
                    pendingNuls = 0;
                } else if (ch > 31 && ch < 127 && ch !='\\') {
                    checkSplit(1);
                    outputWriter.write(ch);
                } else {
                    checkSplit(3);
                    outputWriter.write("\\" + Data.toHexString((byte)ch));
                }
            }
        }
    }

    public void flush() throws IOException {
        checkCR();
        outputWriter.flush();
    }

    public void close() throws IOException {
        checkCR();
        // Most files should have ended with a line-end, but if it didn't we need
        // to add the trailing '\' marker to ensure we don't generate a line-end 
        // when we read it back.
        if (lineLength > 0) {
            outputWriter.write("\\");
        }
        outputWriter.close();
    }

    private void checkSplit(int size) {
        // Split a line if required by adding a trailing '\' to the first part.
        lineLength += size;
        if (maxLength > 0 && lineLength >= maxLength) {
            outputWriter.println("\\");
            lineLength = size;
        }
    }

    private void checkCR() {
        // Check if we previously saw a CR but it did not turn out to be the 
        // start of a line-end sequence.  In this case we need to write the actual
        // CR character and any accumulated pending NULs.
        if (pendingLineEnd) {
            checkSplit(3);
            outputWriter.write("\\0D");
            for (int i = 0; i < pendingNuls; i++) {
                checkSplit(3);
                outputWriter.write("\\00");
            }
            pendingLineEnd = false;
        }
    }
}
