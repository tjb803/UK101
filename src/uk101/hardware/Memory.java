/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import uk101.machine.Data;

/**
 * Base class for all memory mapped hardware including ROM and RAM.
 *
 * @author Baldwin
 */
public abstract class Memory {

    public static final int K1 = 1024;
    public static final int K64 = 64*K1;
    public static final int BLKSIZE = 1024;

    public int base;            // Stores the starting memory address
    public int blocks;          // Memory size in addressable blocks

    public byte[] store;

    public byte readByte(int offset) {
        return store[offset];
    }

    public void writeByte(int offset, byte b) {
        store[offset] = b;
    }
    
    public int kBytes() {       // Memory size in Kbytes 
        return store.length/K1;
    }

    protected void setStore(byte[] sb) {
        store = sb;
        blocks = (store.length + BLKSIZE-1)/BLKSIZE;
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "@" + Data.toHexString(base) + "-" + Data.toHexString(base+blocks*BLKSIZE);
    }
}
