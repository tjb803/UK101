/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware.bus;

/**
 * The IODevice provides methods for peripheral devices (such as the 
 * cassette recorder) to interface to the ACIA.
 *
 * @author Baldwin
 */
public interface IODevice {
    
    public int getBaudRate();           // Return required baud rate 

    public void setTxBus(IOBus bus);    // Set current transmit device
    public void setRxBus(IOBus bus);    // Set current receive device
}
