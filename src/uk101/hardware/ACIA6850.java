/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2013
 */
package uk101.hardware;

import uk101.hardware.bus.IOBus;
import uk101.hardware.bus.IODevice;
import uk101.machine.Computer;
import uk101.machine.Data;

/**
 * Simulation of the base 6850 ACIA used by the UK101.
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

    private byte statusReg;             // Status register
    private byte txByte, rxByte;        // Transmit/receive buffers
    
    private int txClock;                // Transmit clock rate
    private int txTime;                 // Millisecond time to transmit one character
    private int baudRate;               // Real baud rate
    
    IOBus txBus, rxBus;

    public ACIA6850(int baud, int priority) {
        super(256);                 // Decodes to 256 bytes of store

        statusReg = STATUS_TDRE;    // Initial state - ready to transmit
        
        // By default the UK101 uses a frequency division of 16, so the clock 
        // rate is set to 16x the required baud rate.
        txClock = baud*16;
        baudRate = baud;

        // Start the worker thread.  Use a lower priority than the CPU thread.
        Thread worker = new Thread(this);
        worker.setName(getClass().getSimpleName());
        worker.setPriority(Math.max(Thread.MIN_PRIORITY, priority-1));
        worker.start();
    }
    
    /*
     * The address decodes to a 256 byte block, but really there are only 
     * two registers that are accessed.
     */
    
    public synchronized byte readByte(int offset) {
        byte b = 0;
        if ((offset & 1) == 0) {
            b = statusReg;
        } else {
            b = rxByte;
            // TODO: There's an issue here when loading the EXMON tape (and maybe  
            // others?) when using the standard monitor.  It briefly ends up setting
            // an address of F007 which causes this code to read (and so lose) a 
            // character.  This wouldn't happen on a real machine as characters were
            // delivered much slower, not on demand as happens here.  So, if we have 
            // that monitor we'll only flag the character as read if we read from 
            // address F001.  This is fine, F001 is the address that should be used,
            // rather than some other random value in the 256 byte block.
            if (!Computer.aciaFix1 || offset == 1) {
                statusReg &= ~STATUS_RDRF;
                notify();
            }    
        }
        return b;
    }

    public synchronized void writeByte(int offset, byte b) {
        if ((offset & 1) == 0) {
            if (b == CONTROL_RESET) {
                statusReg = STATUS_TDRE;
                setSpeed((byte)0);
            } else {
                setSpeed(b);
            }
        } else {
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
        txTime = (txTime*2)/3;  // Reduce a little for inaccuracies! 
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
        boolean tx, rx;
        byte sb;
        int tb, rb;

        rb = -1;
        sb = 0;
        while (true) {
            try {
                synchronized (this) {
                    if (sb != 0) statusReg |= sb;
                    if (rb != -1) rxByte = (byte)rb;
                    wait();
                    
                    tx = ((statusReg & STATUS_TDRE) == 0);
                    rx = ((statusReg & STATUS_RDRF) == 0);
                    tb = txByte;
                    rb = -1;
                    sb = 0;
                }
                   
                // Anything waiting to be transmitted? 
                if (tx) {
                    // If a device is attached we assume it handles the timing of the
                    // character; if there is no device we pause for the correct time
                    // to write a single character.  (This allows the ACIA to be used 
                    // to generate a timing signal, but still allows saving to simulated
                    // tape files to happen as quickly as possible.)
                    if (txBus != null) {
                        txBus.writeByte(tb);
                    } else {
                        Thread.sleep(txTime);
                    }
                    sb |= STATUS_TDRE;
                }

                // Anything to receive
                if (rx) {
                    if (rxBus != null) {
                        rb = rxBus.readByte();
                        if (rb != -1) {
                            sb |= STATUS_RDRF;
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
