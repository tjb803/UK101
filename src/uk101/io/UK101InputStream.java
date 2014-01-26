/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to take a stream of Java characters and provide a stream
 * of input bytes in the form expected by the UK101. This is to allow a file
 * that can be easily viewed and edited on the PC to be provided as input
 * to the cassette player.
 *
 * The conversion rules are:
 *
 * - Any standard ASCII character (i.e. code value 32 to 126) is written
 *   directly to the output
 *
 * - Any hex escapes of the form '\nn' are converted to the byte value.
 *
 * - Line ends are written as CR 10xNUL LF.
 *
 * @author Baldwin
 */
public class UK101InputStream extends InputStream {

    static final String NEWLINE = "\r\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\n";
    static final Pattern ESCAPE = Pattern.compile("\\\\([0-9A-F]{2})");

    private BufferedReader inputReader;
    private String line;
    private int lineLen, linePos;

    // The reader needs to be wrapped in a BufferedReader (if it is not already
    // buffered) so complete lines can be read and the BufferedReader code can
    // handle correctly detecting line ends.

    public UK101InputStream(Reader in) {
        this(new BufferedReader(in));
    }

    public UK101InputStream(BufferedReader in) {
        inputReader = in;
    }

    /*
     * Override the required InputStream methods
     */

    public int read() throws IOException {
        int ch = -1;
        // Read the next full line and remove any hex-coded escapes
        if (line == null) {
            line = inputReader.readLine();
            if (line != null) {
                if (line.indexOf('\\') != -1) {
                    StringBuffer sb = new StringBuffer();
                    Matcher m = ESCAPE.matcher(line);
                    while (m.find()) {
                        char codepoint = (char)Integer.parseInt(m.group(1), 16);
                        m.appendReplacement(sb, Character.toString(codepoint));
                    }
                    m.appendTail(sb);
                    line = sb.toString();
                }
                lineLen = line.length();
                linePos = 0;
            }
        }
        // Return the next character
        if (line != null) {
            if (linePos == lineLen) {
                line = NEWLINE;
                lineLen = line.length();
                linePos = 0;
            }
            ch = line.charAt(linePos++);
            if (line == NEWLINE && linePos == lineLen) {
                line = null;
            }
        }
        return ch;
    }

    public void close() throws IOException {
        inputReader.close();
    }
}
