/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import uk101.hardware.bus.IOBus;
import uk101.machine.Data;

/**
 * Simulation of the base 6850 ACIA used by the UK101.
 *
 * @author Baldwin
 */
public class ACIA6850 extends Memory implements Runnable {

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

    byte statusReg;                 // Control registers
    byte txByte, rxByte;            // Transmit/receive buffers

    IOBus bus;

    public ACIA6850() {
        blocks = 1;                 // Fills a complete block

        statusReg = STATUS_TDRE;    // Initial state - ready to transmit

        // Start the worker thread
        Thread worker = new Thread(this);
        worker.setName(getClass().getSimpleName());
        worker.start();
    }

    public void setDevice(IOBus bus) {
        this.bus = bus;
        bus.setListener(this);
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
            if ((statusReg & STATUS_RDRF) != 0) {
                b = rxByte;
                statusReg &= ~STATUS_RDRF;
                notify();
            }
        }
        return b;
    }

    public synchronized void writeByte(int offset, byte b) {
        if (offset == 0) {
            if (b == CONTROL_RESET) {
                statusReg = STATUS_TDRE;
            }
        } else if (offset == 1) {
            if ((statusReg & STATUS_TDRE) != 0) {
                txByte = b;
                statusReg &= ~STATUS_TDRE;
                notify();
            }
        }
    }

    /*
     * Worker thread that handles transmitting and receiving characters.
     */
    public synchronized void run() {
        while (true) {
            try {
                wait();
                // Anything waiting to be transmitted?
                if ((statusReg & STATUS_TDRE) == 0) {
                    if (bus != null) {
                        bus.writeByte(txByte);
                    }
                    statusReg |= STATUS_TDRE;
                }

                // Anything to receive
                if ((statusReg & STATUS_RDRF) == 0) {
                    if (bus != null) {
                        int b = bus.readByte();
                        if (b != -1) {
                            rxByte = (byte)b;
                            statusReg |= STATUS_RDRF;
                        }
                    }
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
