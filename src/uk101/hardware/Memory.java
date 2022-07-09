/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
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

    public int base;            // Stores the starting base memory address
    public int start;           // Stores the offset of the start address
    public int blocks;          // Stores the number of blocks

    public byte[] store;        // Storage area, if present
    protected boolean readOnly; // true if read-only storage

    public Memory(int bytes) {
        blocks = toBlocks(bytes);
    }

    public Memory(byte[] sb) {
        this(sb.length);
        store = sb;
    }

    public void setAddress(int addr) {
        base = blockBase(addr);
        start = addr - base;
    }

    public byte readByte(int a) {
        if (store != null) {
            return store[a];
        }
        return Data.getHiByte((short)(base+a));
    }

    public void writeByte(int a, byte b) {
        if (!readOnly) {
            store[a] = b;
        }
    }

    public byte traceByte(int a) {
        return readByte(a);
    }

    // Memory size in bytes
    public int bytes() {
        return blocks*BLKSIZE;
    }

    // Memory size in Kbytes
    public int kBytes() {
        return toK(bytes());
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

    // Turn address into block boundary
    public static final int blockBase(int addr) {
        return asBlock(addr)*BLKSIZE;
    }

    /*
     * Mainly for debugging
     */
    protected String memBase() {
        StringBuilder s = new StringBuilder("@");
        s.append(Data.toHexString(base));
        if (start > 0)
            s.append("/").append(Data.toHexString(base+start));
        s.append("-").append(Data.toHexString(base+blocks*BLKSIZE-1));
        return s.toString();
    }
}
