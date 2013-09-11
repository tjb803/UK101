/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import junit.framework.TestCase;
import uk101.io.UK101InputStream;
import uk101.io.UK101OutputStream;
import uk101.utils.PrintBytes;

/**
 * JUnit tests for the UK101 input stream
 *
 * @author Baldwin
 */
public class TestUK101Streams extends TestCase {

    String end = System.getProperty("line.separator");

    String[] lines1 = { "10 PRINT \"HELLO\u0001:\u0080\u00fe\"", "20 GOTO 10" };
    String text1 = end + "10 PRINT \"HELLO\\01:\\80\\FE\"" + end + "20 GOTO 10" + end;


    public void testOutputStream1() throws Exception {
        StringWriter sw = new StringWriter();
        UK101OutputStream out = new UK101OutputStream(sw);

        out.write(makeBytes(lines1));
        out.close();
        System.out.println(sw);

        assertTrue(sw.toString().equals(text1));
    }

    public void testInputStream1() throws Exception {
        StringReader sr = new StringReader(text1);
        UK101InputStream in = new UK101InputStream(sr);

        byte[] b1 = new byte[2000];
        int size = in.read(b1);
        byte[] b2 = new byte[size];
        System.arraycopy(b1, 0, b2, 0, size);
        new PrintBytes(System.out).print(0, new ByteArrayInputStream(b2));
        in.close();

        assertTrue(Arrays.equals(b2, makeBytes(lines1)));
    }

    // Make a sequence of output bytes
    byte[] makeBytes(String[] text) throws IOException {
        byte[] lineEnd = new byte[] { 0x0D, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x0A };
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(lineEnd);
        for (String s : text) {
            for (int i = 0; i < s.length(); i++) {
                bout.write(s.charAt(i) & 0xFF);
            }
            bout.write(lineEnd);
        }
        return bout.toByteArray();
    }
}
