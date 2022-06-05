/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2022
 */
package uk101.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * JUnit tests for printing (compacted) hex dumps
 */
public class TestPrintBytes extends TestCase {

    protected void setUp() {
        fullFormatter = new PrintBytes(System.out);
        compactFormatter = new PrintBytes(System.out, true);
    }

    PrintBytes fullFormatter;
    PrintBytes compactFormatter;

    public void testOne() throws Exception {
        byte[] data = makeData(1);
        checkPrint(data, 0);
    }

    public void testOneAndSome() throws Exception {
        byte[] data = new byte[16+5];
        Arrays.fill(data, (byte)0x10);
        checkPrint(data, 0);
    }

    public void testTwo() throws Exception {
        byte[] data = makeData(1,2);
        checkPrint(data, 0x200);
    }

    public void testTwoAA() throws Exception {
        byte[] data = makeData(1,1);
        checkPrint(data, 0x200);
    }

    public void testThree() throws Exception {
        byte[] data = makeData(1,2,3);
        checkPrint(data, 0x30);
    }

    public void testThreeAAA() throws Exception {
        byte[] data = makeData(1,1,1);
        checkPrint(data, 0x30);
    }

    public void testThreeABB() throws Exception {
        byte[] data = makeData(1,2,2);
        checkPrint(data, 0x30);
    }

    public void testThreeAAB() throws Exception {
        byte[] data = makeData(1,1,2);
        checkPrint(data, 0x30);
    }

    public void testFourAAAA() throws Exception {
        byte[] data = makeData(1,1,1,1);
        checkPrint(data, 0x4000);
    }

    public void testFourABBB() throws Exception {
        byte[] data = makeData(1,2,2,2);
        checkPrint(data, 0x4000);
    }

    public void testFourAABB() throws Exception {
        byte[] data = makeData(1,1,2,2);
        checkPrint(data, 0x4000);
    }

    public void testFourAAAB() throws Exception {
        byte[] data = makeData(1,1,1,2);
        checkPrint(data, 0x4000);
    }

    public void testFourABBA() throws Exception {
        byte[] data = makeData(1,2,2,1);
        checkPrint(data, 0x4000);
    }

    public void testFiveAAAAA() throws Exception {
        byte[] data = makeData(1,1,1,1,1);
        checkPrint(data, 0xB);
    }

    public void testFiveABBBB() throws Exception {
        byte[] data = makeData(1,2,2,2,2);
        checkPrint(data, 0xB);
    }

    public void testFiveAAAAB() throws Exception {
        byte[] data = makeData(1,1,1,1,2);
        checkPrint(data, 0xB);
    }

    public void testFiveABBBA() throws Exception {
        byte[] data = makeData(1,2,2,2,1);
        checkPrint(data, 0xB);
    }


    private byte[] makeData(int ...lines) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < 16; j++)
                bs.write(lines[i]);
        }
        return bs.toByteArray();
    }

    private void checkPrint(byte[] data, int addr) throws IOException {
        System.out.println(getName());
        fullFormatter.print(addr, new ByteArrayInputStream(data));
        System.out.println("-");
        compactFormatter.print(addr, new ByteArrayInputStream(data));
        System.out.println();
    }
}
