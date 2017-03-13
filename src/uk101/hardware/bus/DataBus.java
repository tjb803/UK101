/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2017
 */
package uk101.hardware.bus;

/**
 * The DataBus provides the CPU with methods to read and write data
 * to the ROM and RAM store.  A suitable DataBus implementation needs
 * to be provided to the CPU when it is created.
 */
public interface DataBus {

    public byte readByte(int addr);                 // Read 8-bit value
    public void writeByte(int addr, byte value);    // Write 8-bit value
    
    public byte traceByte(int addr);                // Read for trace
}
