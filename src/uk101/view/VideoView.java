/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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

    private Video video;
    private VideoScreen screen;
    private int srows, scols, sstart, sstep;
    private int ssize;
    private Color scolour;
    private boolean ssync;
    private Container content;

    public VideoView(Video video, Configuration cfg) {
        super("Video", false);
        this.video = video;

        content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(VideoScreen.SCREEN_BLACK);

        // Fixed details about the video view
        ssize = cfg.getScreenSize() < 2 ? 1 : 2;
        scolour = getScreenColour(cfg.getScreenColour());
        ssync = cfg.getScreenUpdate().equals(Configuration.SYNC);

        // For standard video set the default screen format up-front
        if (video.standard) {
            setFormat(video.rows, cfg.getScreenWidth(), cfg.getScreenOffset(), video.cols, false);
        }

        video.setView(this);
    }

    /*
     * Set/reset the screen format based on supplied parameters:
     *   rows, cols: number of rows and columns to be _displayed_
     *   start:      offset into video RAM of top-left character
     *   step:       size to step in video RAM to begin a new row
     *   invert:     display as inverted (black-on-white) characters
     *
     * This is not particularly optimised as it is assumed it is not called
     * very often.
     */
    public void setFormat(int rows, int cols, int start, int step, boolean invert) {
        int prevWidth = (screen != null) ? screen.getWidth() : 0;

        screen = new VideoScreen(video, rows, cols, start, step, ssize, scolour, invert, ssync);
        srows = rows;  scols = cols;
        sstart = start;  sstep = step;

        // Surround the screen by a small border (about a character cell wide).
        int bh = video.charHeight*ssize;
        int bw = video.charWidth*ssize;
        JPanel tv = new JPanel();
        tv.setLayout(new BoxLayout(tv, BoxLayout.Y_AXIS));
        tv.setOpaque(true);
        tv.setBackground(VideoScreen.SCREEN_BLACK);
        tv.setBorder(BorderFactory.createEmptyBorder(bh, bw, bh, bw));
        tv.add(screen);

        // The video size seems too small the first time this is displayed when using
        // the Nimbus look-and-feel - so this is a hack that makes it about right.  Why?
        if (prevWidth == 0 && ComputerView.isNimbus) {
            Dimension ds = screen.getPreferredSize();
            tv.setPreferredSize(new Dimension(ds.width + 2*bw, ds.height + 2*bh + 5+ssize));
        }

        content.removeAll();
        content.add(tv);

        // Redisplay and reposition the main view to keep the new screen centralized
        if (prevWidth != 0) {
            revalidate();
            repaint();
            pack();
            setLocation(getX() + (prevWidth-screen.getWidth())/2, getY());
        }
    }

    // Map configuration screen colour
    private Color getScreenColour(String value) {
        Color colour = VideoScreen.SCREEN_WHITE;
        if (value.equals(Configuration.GREEN))
            colour = VideoScreen.SCREEN_GREEN;
        else if (value.equals(Configuration.AMBER))
            colour = VideoScreen.SCREEN_AMBER;
        return colour;
    }

    /*
     * Called when the video memory is updated
     */
    public void updateVideo(int addr, byte b) {
        // Calculate the row and column position on the screen for the character
        // that has been updated.  This needs to account for the 'offset' of the
        // first visible character on each row.
        int r = addr / sstep;
        int c = addr % sstep - sstart;

        // Update the screen if the character is part of the visible portion of
        // the video area.
        if ((r >= 0 && r < srows) && (c >= 0 && c < scols)) {
            screen.screenUpdate(r, c, b);
        }
    }
}
