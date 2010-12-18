/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware.bus;

/**
 * The IOBus provides methods for the ACIA to communicate with
 * peripheral devices (such as the cassette recorder).
 *
 * @author Baldwin
 */
public interface IOBus {

    public int readByte();                  // Read 8-bit value
    public void writeByte(int value);       // Write 8-bit value

    public void setListener(Object reader); // Object to notify when reading
}
