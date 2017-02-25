/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import uk101.hardware.ROM;
import uk101.hardware.Video;

/**
 * Unit test for the video view
 */
public class TestVideoView extends BaseViewTest {

    protected void setupTest() throws Exception {
        // Create the store display component and add to the frame.
        ROM charSet = new ROM(testCfg.getRomCharset());
        video = new Video(16, 64, charSet);
        vidView = new VideoView(video, testCfg);
        testView.add(vidView.display());
    }

    private Video video;
    private VideoView vidView;

    public void testCharset() throws Exception {
        // Fill with spaces
        for (int i = 0; i < video.store.length; i++) {
            video.store[i] = 32;
        }

        // Insert the whole character set
        for (int i = 0; i < 16; i++) {
            int offset = i*64 + 20 + (i%2);
            for (int j = 0; j < 16; j++) {
                video.writeByte(offset, (byte)(i*16+j));
                offset += 2;
            }
        }
        while (true) {
            Thread.yield();
        }
    }
}
