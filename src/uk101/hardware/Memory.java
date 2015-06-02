/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2013
 */
package uk101.hardware;

import uk101.machine.Data;

/**
 * Base class for all memory mapped hardware including ROM and RAM.
 */
public abstract class Memory {

    // Memory is mapped and managed in 256 byte blocks
    public static final int BLKSIZE = 256;  
    
    public static final int K1 = 1024;
    public static final int K64 = 64*K1;

    public int base;        // Stores the starting memory address
    public int blocks;      // Stores the number of blocks

    public byte[] store;    // Storage area, if present     
    
    public Memory(int bytes) {
        blocks = toBlocks(bytes);
    }
    
    public Memory(byte[] sb) {
        store = sb;
        blocks = toBlocks(sb.length);
    }

    public byte readByte(int offset) {
        return store[offset];
    }

    public void writeByte(int offset, byte b) {
        store[offset] = b;
    }
    
    // Memory size in Kbytes 
    public int kBytes() {
        return toK(blocks*BLKSIZE);
    }

    // Round up to the nearest 1K
    public static final int toK(int bytes) {
        return (bytes + K1-1)/K1;
    }
    
    // Round up to block size
    public static final int toBlocks(int bytes) {
        return (bytes + BLKSIZE-1)/BLKSIZE;
    }
    
    // Get block from an address
    public static final int asBlock(int addr) {
        return addr/BLKSIZE;
    }
  
    /*
     * Mainly for debugging
     */
    public String toString() {
        return "@" + Data.toHexString(base) + "-" + Data.toHexString(base+blocks*BLKSIZE);
    }
}
