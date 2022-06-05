/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
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
 * JUnit tests for the UK101 input and output streams
 */
public class TestUK101Streams extends TestCase {

    String end = System.getProperty("line.separator");
    byte[] lineEnd = new byte[] { 0x0D, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x0A };

    String[] lines1 = { "10 PRINT \"HELLO\u0001:\u0080\u00fe\"", "20 GOTO 10" };

    String text1 = end + "10 PRINT \"HELLO\\01:\\80\\FE\"" + end + "20 GOTO 10" + end;
    String text2 = end + "10 PRINT \\" + end + "\"HELLO\\01\\" + end + ":\\80\\FE\"" + end + "20 GOTO 1\\" + end + "0" + end;
    String text3 = "10 PRINT \"HELLO\\01:\\80\\FE\"" + end + "20 GOTO 10\\";


    // Basic ASCII output
    public void testOutputStream1() throws Exception {
        StringWriter sw = new StringWriter();
        UK101OutputStream out = new UK101OutputStream(sw, 80);

        out.write(makeBytes(lines1));
        out.close();
        System.out.println(sw);

        assertTrue(sw.toString().equals(text1));
    }

    // Split lines at 10 characters maximum
    public void testOutputStream2() throws Exception {
        StringWriter sw = new StringWriter();
        UK101OutputStream out = new UK101OutputStream(sw, 10);

        out.write(makeBytes(lines1));
        out.close();
        System.out.println(sw);

        assertTrue(sw.toString().equals(text2));
    }

    // No line-end after last line
    public void testOutputStream3() throws Exception {
        StringWriter sw = new StringWriter();
        UK101OutputStream out = new UK101OutputStream(sw, 80);

        out.write(makeBytes2(lines1));
        out.close();
        System.out.println(sw);

        assertTrue(sw.toString().equals(text3));
    }

    // Convert back to to UK101 form
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

    byte[] makeBytes2(String[] text) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int j = 0; j < text.length; j++) {
            String s = text[j];
            for (int i = 0; i < s.length(); i++) {
                bout.write(s.charAt(i) & 0xFF);
            }
            if (j < text.length-1) {
                bout.write(lineEnd);
            }
        }
        return bout.toByteArray();
    }
}
