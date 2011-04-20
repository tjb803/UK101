/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import uk101.hardware.bus.IOBus;
import uk101.hardware.bus.IODevice;
import uk101.machine.Data;

/**
 * Simulation of the base 6850 ACIA used by the UK101.
 *
 * @author Baldwin
 */
public class ACIA6850 extends Memory implements IODevice, Runnable {

    // Status bits we are interested in
    static final byte STATUS_RDRF = (byte)0x01;     // Byte received
    static final byte STATUS_TDRE = (byte)0x02;     // Byte transmitted

    // Control bits we are interested in
    static final byte CONTROL_RESET = (byte)0x03;   // Master reset signal

    /*
     * The ACIA decodes to two consecutive addresses: a status/control port and a tx/rx
     * port.  Most of the work of 'transmitting' and 'receiving' characters needs to
     * happen on a worker thread so it does not interfere with the processor execution
     * or the GUI event threads.
     *
     * The UK101 doesn't use any interrupts and only works by polling the status
     * port, so this logic is fairly simple.
     */

    byte statusReg;                 // STatus register
    byte txByte, rxByte;            // Transmit/receive buffers
    
    int txClock;                    // Transmit clock rate
    int txTime;                     // Millisecond time to transmit one character
    int baudRate;                   // Real baud rate
    
    IOBus txBus, rxBus;

    public ACIA6850(int baud) {
        blocks = 1;                 // Fills a complete block

        statusReg = STATUS_TDRE;    // Initial state - ready to transmit
        
        // By default the UK101 uses a frequency division of 16, so the clock 
        // rate is set to 16x the required baud rate.
        txClock = baud*16;
        baudRate = baud;

        // Start the worker thread
        Thread worker = new Thread(this);
        worker.setName(getClass().getSimpleName());
        worker.start();
    }
    
    // A real UK101 would decode the ACIA address to at least a 256 byte block and
    // any read/write to that block would be accepted.  But this causes me a
    // problem when loading the extended monitor as at one point it ends up
    // setting an address in the monitor of F007 which gets interpreted as a 'read
    // character' and we lose an input character (this didn't happen on the real
    // machine as presumably characters were delivered too slowly).  So I'm only 
    // decoding the two addresses F000 and F001.

    public synchronized byte readByte(int offset) {
        byte b = 0;
        if (offset == 0) {
            b = statusReg;
        } else if (offset == 1) {
            b = rxByte;
            statusReg &= ~STATUS_RDRF;
            notify();
        }
        return b;
    }

    public synchronized void writeByte(int offset, byte b) {
        if (offset == 0) {
            if (b == CONTROL_RESET) {
                statusReg = STATUS_TDRE;
                setSpeed((byte)0);
            } else {
                setSpeed(b);
            }
        } else if (offset == 1) {
            txByte = b;
            statusReg &= ~STATUS_TDRE;
            notify();
        }
    }
    
    // Sets the time (in milliseconds) to send and receive a single character
    // based on the clock frequency and the control register settings.
    private void setSpeed(byte controlReg) {
        int sb = controlReg & 0x03;
        int wb = (controlReg>>2) & 0x07;
        int divide = (sb == 1) ? 16 : (sb == 2) ? 64 : 1;
        int length = (wb == 2 || wb == 3 || wb == 5) ? 10 : 11;
        
        baudRate = txClock / divide;
        txTime = (1000 * length * divide) / txClock;
    }
    
   /*
    * IODevice interface allows external devices (such as the cassette recorder)
    * to set an IOBus when they have the ability to send or receive data.
    */
    public synchronized void setTxBus(IOBus bus) {
        txBus = bus;
    }

    public synchronized void setRxBus(IOBus bus) {
        rxBus = bus;
        if (bus != null) {
            notify();
        }
    }
   
    public int getBaudRate() {
        return baudRate;
    }
    
    /*
     * Worker thread that handles transmitting and receiving characters.
     */
    public void run() {
        while (true) {
            try {
                byte status;
                int txb = -1, rxb = -1;
                synchronized (this) {
                    wait();
                    status = statusReg;
                    txb = txByte;
                }    
                    
                // Anything waiting to be transmitted? 
                if ((status & STATUS_TDRE) == 0) {
                    // If a device is attached we assume it handles the timing of the
                    // character; if there is no device we pause for the correct time
                    // to write a single character.  (This allows the ACIA to be used 
                    // to generate a timing signal, but still allows saving to simulated
                    // tape files to happen as quickly as possible.)
                    if (txBus != null) {
                        txBus.writeByte(txb);
                    } else {
                        Thread.sleep(txTime);
                    }
                    status |= STATUS_TDRE;
                }

                // Anything to receive
                if ((status & STATUS_RDRF) == 0) {
                    if (rxBus != null) {
                        rxb = rxBus.readByte();
                        if (rxb != -1) {
                            status |= STATUS_RDRF;
                        }
                    }
                }
                
                synchronized (this) {
                    if (rxb != -1) {
                        rxByte = (byte)rxb;
                    }
                    statusReg = status;
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("ACIA").append(super.toString());
        sb.append(" Status=").append(Data.toBinaryString(statusReg));
        sb.append(" Tx=").append(Data.toHexString(txByte));
        sb.append(" Rx=").append(Data.toHexString(rxByte));
        return sb.toString();
    }
}
