/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
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
import java.util.Arrays;

import javax.swing.JPanel;

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

    private int rows, cols;

    private Image charset[];            // Character set images
    private byte[][] cells;             // Video cells

    private int cw, ch;                 // Character width and height
    private int sw, sh;                 // Scaled character width and height on screen
    
    private boolean syncPaint;          // Window update style

    /*
     * Video size and character set can be found from the video hardware
     * component.  The screen displays an array of character images addressed
     * as a set of character cells in a row,column matrix.  This represents
     * just the visible portion of the screen.
     */
    public VideoScreen(Video video, int rows, int cols, int hscan, int vscan, int scale, Color colour) {
        setOpaque(true);
        setBackground(SCREEN_BLACK);

        // Details of the displayed video screen
        this.rows = rows;
        this.cols = cols;
        cw = video.charWidth;  ch = video.charHeight;
        sw = cw*hscan*scale;  sh = ch*vscan*scale;

        // Internal copy of the current character cells.  Ensure this is filled
        // with spaces to begin with, so we don't see garbage on the screen.
        cells = new byte[rows][];
        for (int i = 0; i < rows; i++) {
            cells[i] = new byte[cols];
            Arrays.fill(cells[i], (byte)' ');
        }

        // Calculate the pixel size of the display area.
        Dimension size = new Dimension(cols*sw, rows*sh);
        setPreferredSize(size);
        setMaximumSize(size);

        // Build the character set images
        makeCharacterSet(video.charSet, colour);
    }

    /*
     * Repaint the screen based on the contents of the character cell array.
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int minRow = 0, minCol = 0;
        int maxRow = rows-1, maxCol = cols-1;
        Rectangle clip = g.getClipBounds();
        if (clip != null) {
            minRow = (clip.y)/sh;  maxRow = Math.min((clip.y+clip.height)/sh, maxRow);
            minCol = (clip.x)/sw;  maxCol = Math.min((clip.x+clip.width)/sw, maxCol);
        }

        synchronized (this) {
            int y = minRow*sh;
            for (int r = minRow; r <= maxRow; r++) {
                int x = minCol*sw;
                byte[] rcells = cells[r];
                for (int c = minCol; c <= maxCol; c++) {
                    g.drawImage(charset[rcells[c] & 0xFF], x, y, this);
                    x += sw;
                }
                y += sh;
            }
        }
    }
    
    /*
     * Set window update mode
     */
    public void setUpdateMode(boolean sync) {
        syncPaint = sync;
    }

    /*
     * Indicate a screen character needs updating
     */
    public synchronized void screenUpdate(int row, int col, byte b) {
        if (cells[row][col] != b) {
            cells[row][col] = b;
            
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
                    g.drawImage(charset[b & 0xFF], col*sw, row*sh, this);
                    g.dispose();
                } else {    
                    repaint(col*sw, row*sh, sw, sh);
                }
            } else {
                repaint(col*sw, row*sh, sw, sh);
            }    
        }
    }
    
    /*
     * Return character cell size
     */
    public Dimension getCellSize() {
        return new Dimension(sw, sh);
    }
    
    // Builds a set of Images, one per character based on the information from
    // the character generator ROM.
    private void makeCharacterSet(ROM chargen, Color colour) {
        // The character generator ROM contains 8 bytes per character, with each
        // byte being 1-bit per pixel for each character row. To build Java Image
        // objects we need the pixel data as 1 byte per pixel.
        byte[] charmap = new byte[256 * cw * ch];
        for (int b = 0, i = 0; i < 256*ch; i++) {
            byte pixels = chargen.readByte(i);
            for (int j = 7; j >= 0; j--) {
                charmap[b++] = (byte)((pixels>>j) & 1);
            }
        }
        
        // Now build the images and colour mapping
        byte[] r = new byte[] { 0, (byte)colour.getRed() };
        byte[] g = new byte[] { 0, (byte)colour.getGreen() };
        byte[] b = new byte[] { 0, (byte)colour.getBlue() };
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
