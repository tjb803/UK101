/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.hardware;

import junit.framework.TestCase;

/**
 * JUnit tests for the arithmetic and logic functions
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
    
    // Test BCD operation (from examples in http://www.6502.org/tutorials/decimal_mode.html)
    // Strictly, in an original 6502, only the C flag is defined after a BCD operation.
    public void testDecimalAdd() throws Exception {
        alu.setDecimal(true);
        
        result = alu.add((byte)0x05, (byte)0x05, false);
        assertResult((byte)0x10, false);
        
        result = alu.add((byte)0x09, (byte)0x01, false);
        assertResult((byte)0x10, false);
        
        result = alu.add((byte)0x58, (byte)0x46, true);
        assertResult((byte)0x05, true);
        
        result = alu.add((byte)0x12, (byte)0x34, false);
        assertResult((byte)0x46, false);
        
        result = alu.add((byte)0x15, (byte)0x26, false);
        assertResult((byte)0x41, false);
        
        result = alu.add((byte)0x81, (byte)0x92, false);
        assertResult((byte)0x73, true);
  
        result = alu.add((byte)0x99, (byte)0x01, false);
        assertResult((byte)0x00, true);
    }
    
    public void testDecimalSub() throws Exception {
        alu.setDecimal(true);
        
        result = alu.sub((byte)0x46, (byte)0x12, true);
        assertResult((byte)0x34, true);
        
        result = alu.sub((byte)0x40, (byte)0x13, true);
        assertResult((byte)0x27, true);
        
        result = alu.sub((byte)0x32, (byte)0x02, false);
        assertResult((byte)0x29, true);
                
        result = alu.sub((byte)0x12, (byte)0x21, true);
        assertResult((byte)0x91, false);
        
        result = alu.sub((byte)0x21, (byte)0x34, true);
        assertResult((byte)0x87, false);
        
        result = alu.sub((byte)0x01, (byte)0x01, true);
        assertResult((byte)0x00, true);
    }
 
    private void assertResult(byte value, boolean negative, boolean overflow, boolean zero, boolean carry) {
        assertEquals(value, (byte)result);
        assertTrue("N", (result < 0) == negative);
        assertTrue("Z", (result == 0) == zero);
        assertTrue("V", alu.isOverflow == overflow);
        assertTrue("C", alu.isCarry == carry);
    }
    
    private void assertResult(byte value, boolean carry) {
        assertEquals(value, (byte)result);
        assertTrue("C", alu.isCarry == carry);
    }
}
