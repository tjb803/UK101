/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

import uk101.hardware.Memory;
import uk101.hardware.ROM;
import uk101.hardware.Video;

/**
 * Displays the video output that would appear on a standard TV screen.
 */
public class VideoScreen extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final Color SCREEN_BLACK = Color.BLACK;
    public static final Color SCREEN_WHITE = Color.WHITE;
    public static final Color SCREEN_GREEN = Color.GREEN;
    public static final Color SCREEN_AMBER = Color.ORANGE;

    private int vw, vh;             // Video width and height
    private int cw, ch;             // Character width and height
    private int sw, sh;             // Scaled character width and height on screen

    private Memory vram;            // Video RAM
    private int vstart, vstep;      // Top left screen cell offset and row step size
    private boolean syncPaint;      // Window update style

    private Image charset[];        // Character set images

    /*
     * The screen displays an array of character images addressed as a set of
     * character cells in a row,column matrix.  This represents just the visible
     * portion of the screen.
     *
     * Screen is updated by repainting any cells that are part of the graphics
     * dirty rectangle - the contents of those cells are found by directly peeking
     * in the video RAM based on knowing the starting offset for the top left corner
     * and the step size to address a new row.
     */
    public VideoScreen(Video video, int rows, int cols, int start, int step,
            int scale, Color colour, boolean invert, boolean sync) {
        setOpaque(true);
        setBackground(SCREEN_BLACK);

        // Video RAM access details
        vram = video;
        vstart = start;
        vstep = step;

        // Screen and character size details
        int hscale = (cols > 31) ? 1 : 2;
        int vscale = (rows > 16) ? 1 : 2;
        vw = cols;             vh = rows;
        cw = video.charWidth;  ch = video.charHeight;
        sw = cw*hscale*scale;  sh = ch*vscale*scale;
        syncPaint = sync;

        // Build the character set images
        makeCharacterSet(video.charSet, colour, invert);

        // Calculate the pixel size of the display area
        setPreferredSize(new Dimension(vw*sw, vh*sh));
    }

    /*
     * Repaint the screen based on the contents of the character cell array.
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int minRow = 0, minCol = 0;
        int maxRow = vh-1, maxCol = vw-1;
        Rectangle clip = g.getClipBounds();
        if (clip != null) {
            minRow = (clip.y)/sh;  maxRow = Math.min((clip.y+clip.height)/sh, maxRow);
            minCol = (clip.x)/sw;  maxCol = Math.min((clip.x+clip.width)/sw, maxCol);
        }

        int y = minRow*sh;
        for (int r = minRow; r <= maxRow; r++) {
            int p = vstart + r*vstep;
            int x = minCol*sw;
            for (int c = minCol; c <= maxCol; c++) {
                g.drawImage(charset[vram.readByte(p+c) & 0xFF], x, y, null);
                x += sw;
            }
            y += sh;
        }
    }

    /*
     * Indicate a screen character needs updating
     */
    public void screenUpdate(int row, int col, byte b) {
        // Support two types of update: asynchronous and synchronous.
        // async - this is the proper swing update method, we just invalidate
        //         the changed rectangle and allow the Swing event dispatch
        //         thread to repaint some time later.
        // sync  - draws directly to the output graphics context.  This gives
        //         faster updates which works better for this application on
        //         slower machines, but can give screen corruption if another
        //         simulator window overlays the video.
        if (syncPaint) {
            Graphics g = getGraphics();
            if (g != null) {
                g.drawImage(charset[b & 0xFF], col*sw, row*sh, null);
                g.dispose();
            } else {
                repaint(col*sw, row*sh, sw, sh);
            }
        } else {
            repaint(col*sw, row*sh, sw, sh);
        }
    }

    // Builds a set of Images, one per character based on the information from
    // the character generator ROM.
    private void makeCharacterSet(ROM chargen, Color colour, boolean invert) {
        // The character generator ROM contains 8 bytes per character, with each
        // byte being 1-bit per pixel for each character row. To build Java Image
        // objects we need the pixel data as 1 byte per pixel.
        int inv = (invert) ? 0xFF : 0x00;
        byte[] charmap = new byte[256 * cw * ch];
        for (int b = 0, i = 0; i < 256*ch; i++) {
            byte pixels = (byte)(chargen.readByte(i) ^ inv);
            for (int j = 7; j >= 0; j--) {
                charmap[b++] = (byte)((pixels>>j) & 1);
            }
        }

        // Now build the images and colour mapping
        byte[] r = new byte[] { (byte)SCREEN_BLACK.getRed(),   (byte)colour.getRed()   };
        byte[] g = new byte[] { (byte)SCREEN_BLACK.getGreen(), (byte)colour.getGreen() };
        byte[] b = new byte[] { (byte)SCREEN_BLACK.getBlue(),  (byte)colour.getBlue()  };
        IndexColorModel colours = new IndexColorModel(1, 2, r, g, b);

        charset = new Image[256];
        int offset = 0;
        for (int c = 0; c < 256; c++) {
            // Create image from the bitmap data
            Image img = createImage(new MemoryImageSource(cw, ch, colours, charmap, offset, cw));
            offset += cw * ch;
            // Save a scaled image to speed up drawing
            charset[c] = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB);
            Graphics gc = charset[c].getGraphics();
            gc.drawImage(img, 0, 0, sw, sh, null);
            gc.dispose();
        }
    }
}
