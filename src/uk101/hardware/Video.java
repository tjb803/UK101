/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import java.io.IOException;
import java.util.Arrays;

import uk101.view.VideoView;

/**
 * Video is memory mapped starting at D000.  Standard screen was 16 rows of 64
 * 1-byte characters (=1K memory), although only around 46-48 characters were
 * visible on a standard TV screen.
 */
public class Video extends RAM {

    public boolean standard;
    public int rows, cols;

    public ROM charSet;
    public int charWidth, charHeight;

    public Video(int height, int width, ROM chargen) throws IOException {
        // Allocate the video RAM as seen by the CPU.
        super(toK(height*width));
        standard = true;

        // Fill the video buffer with spaces as this helps avoid some random
        // character flashes when the MONUK02 monitor scrolls the screen.
        Arrays.fill(store, (byte)0x20);

        // Save video configuration
        rows = height;
        cols = width;

        // Character generator ROM - characters are defined as 8x8 pixels.
        charSet = chargen;
        charWidth = 8;
        charHeight = 8;
    }

    /*
     * Processing of the video is fairly simple here as we delegate most of the
     * work of actually displaying something to the user to VideoView module.
     */

    public void writeByte(int offset, byte b) {
        super.writeByte(offset, b);
        if (view != null) {
            view.updateVideo(offset, b);
        }
    }

    /*
     * GUI visualisation
     */

    protected VideoView view;

    public void setView(VideoView view) {
        this.view = view;
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "Video" + memBase() + ": " + kBytes() + "K (" + rows + "x" + cols + ")";
    }
}
