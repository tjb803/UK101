/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2013
 */
package uk101.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import uk101.hardware.Video;
import uk101.machine.Configuration;
import uk101.view.component.VideoScreen;
import uk101.view.component.ViewFrame;

/**
 * A visual representation of the video display output.
 */
public class VideoView extends ViewFrame {
    private static final long serialVersionUID = 1L;

    private VideoScreen screen;
    private int vrows, vcols;
    private int srows, scols, sstart;

    public VideoView(Video video, Configuration cfg) {
        super("Video", false);

        video.setView(this);

        // Standard screen would display about 48 characters with the first
        // character of each line starting at an offset of about 13 into the
        // equivalent line in the video RAM.  Characters are displayed double
        // height (ie 16 scan lines for each character, even though there are
        // only 8 rows in the character generator ROM bitmaps).
        vrows = video.rows;  vcols = video.cols;
        srows = vrows;  
        scols = cfg.getScreenWidth();  
        sstart = cfg.getScreenOffset();
        int vscale = (vrows > 16) ? 1 : 2;
        int hscale = (vcols > 32) ? 1 : 2;
        int ssize = (cfg.getScreenSize() < 2) ? 1 : 2;
        Color colour = getScreenColour(cfg.getScreenColour());
        screen = new VideoScreen(video, srows, scols, hscale, vscale, ssize, colour);
        screen.setUpdateMode(isSyncUpdate(cfg.getScreenUpdate()));

        // Surround the screen by a small border (about half a character cell wide).
        Dimension cell = screen.getCellSize();
        JPanel tv = new JPanel();
        tv.setOpaque(true);
        tv.setBackground(VideoScreen.SCREEN_BLACK);
        tv.setBorder(BorderFactory.createEmptyBorder(cell.height/2, cell.width/2, cell.height/2, cell.width/2));
        tv.add(screen);

        Container content = getContentPane();
        content.setBackground(VideoScreen.SCREEN_BLACK);
        content.add(tv);
    }

    // Map configuration screen colour
    private Color getScreenColour(String name) {
        Color colour = VideoScreen.SCREEN_WHITE;
        if (name.equals(Configuration.GREEN))
            colour = VideoScreen.SCREEN_GREEN;
        else if (name.equals(Configuration.AMBER))
            colour = VideoScreen.SCREEN_AMBER;
        return colour;
    }
    
    // Map configuration screen update mode
    private boolean isSyncUpdate(String name) {
        return name.equals(Configuration.SYNC);
    }

    /*
     * Called when the video memory is updated
     */
    public void updateVideo(int addr, byte b) {
        // Calculate the row and column position on the screen for the character
        // that has been updated.  This needs to account for the 'offset' of the
        // first visible character on each row.
        int r = addr / vcols;
        int c = addr % vcols - sstart;

        // Update the screen if the character is part of the visible portion of
        // the video area.
        if ((r >= 0 && r < srows) && (c >= 0 && c < scols)) {
            screen.screenUpdate(r, c, b);
        }
    }
}
