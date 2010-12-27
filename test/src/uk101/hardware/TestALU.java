/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import junit.framework.TestCase;

/**
 * JUnit tests for the arithmetic and logic functions
 *
 * @author Baldwin
 */
public class TestALU extends TestCase {

    protected void setUp() {
        alu = new ALU6502();
        alu.setDecimal(false);
    }

    ALU6502 alu;
    int result;

    // Test correct function of binary add - must return correct result and flags!
    public void testAdd() throws Exception {
        result = alu.add((byte)0x00, (byte)0x00, false);
        assertResult((byte)0x00, false, false, true, false);

        result = alu.add((byte)0x01, (byte)0x01, false);
        assertResult((byte)0x02, false, false, false, false);

        result = alu.add((byte)0x00, (byte)0x00, true);
        assertResult((byte)1, false, false, false, false);

        result = alu.add((byte)0x7F, (byte)0x01, false);
        assertResult((byte)0x80, true, true, false, false);

        result = alu.add((byte)0x7F, (byte)0x7F, false);
        assertResult((byte)0xFE, true, true, false, false);

        result = alu.add((byte)0xFF, (byte)0x01, false);
        assertResult((byte)0x00, false, false, true, true);

        result = alu.add((byte)0xFF, (byte)0x80, false);
        assertResult((byte)0x7F, false, true, false, true);

        result = alu.add((byte)0xFF, (byte)0xFF, false);
        assertResult((byte)0xFE, true, false, false, true);

        result = alu.add((byte)0xFF, (byte)0xFF, true);
        assertResult((byte)0xFF, true, false, false, true);

        result = alu.add((byte)0x3F, (byte)0x40, true);
        assertResult((byte)0x80, true, true, false, false);
    }

    // Test correct function of binary subtract - must return correct result and flags!
    public void testSub() throws Exception {
        result = alu.sub((byte)0x00, (byte)0x00, true);
        assertResult((byte)0x00, false, false, true, true);

        result = alu.sub((byte)0x00, (byte)0x00, false);
        assertResult((byte)0xFF, true, false, false, false);

        result = alu.sub((byte)0x00, (byte)0x01, true);
        assertResult((byte)0xFF, true, false, false, false);

        result = alu.sub((byte)0x80, (byte)0x01, true);
        assertResult((byte)0x7F, false, true, false, true);

        result = alu.sub((byte)0x7F, (byte)0xFF, true);
        assertResult((byte)0x80, true, true, false, false);

        result = alu.sub((byte)0xC0, (byte)0x40, false);
        assertResult((byte)0x7F, false, true, false, true);
    }

    // Test correct operation of comparison
    public void testCmp() throws Exception {
        alu.isOverflow = false;             // Overflow not used in CMP instructions
        
        result = alu.cmp((byte)0x01, (byte)0xFF);
        assertResult((byte)0x02, false, false, false, false);
        
        result = alu.cmp((byte)0x7F, (byte)0x80);
        assertResult((byte)0xFF, true, false, false, false);
    }
    
    private void assertResult(byte value, boolean negative, boolean overflow, boolean zero, boolean carry) {
        assertEquals(value, (byte)result);
        assertTrue("N", (result < 0) == negative);
        assertTrue("Z", (result == 0) == zero);
        assertTrue("V", alu.isOverflow == overflow);
        assertTrue("C", alu.isCarry == carry);
    }
}
