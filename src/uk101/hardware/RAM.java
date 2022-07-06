/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2013
 */
package uk101.hardware;

/**
 * This is a section of RAM.
 */
public class RAM extends Memory {

    /*
     * RAM is just an area of read/write bytes.  Size is in Kbytes.
     */
    public RAM(int k) {
        super(new byte[k*K1]);
    }

    // Allow RAM to restored from a saved image
    public void restore(byte[] image) {
        for (int i = 0; i < Math.min(image.length, store.length); i++)
            writeByte(i, image[i]);
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "RAM" + memBase() + ": " + kBytes() + "K";
    }
}
