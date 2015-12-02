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
        readOnly = false;
    }
    
    // Allow RAM to restored from a saved image
    public void restore(byte[] image) {
        System.arraycopy(image, 0, store, 0, Math.min(image.length, store.length));
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "RAM" + super.toString() + ": " + kBytes() + "K";
    }
}
