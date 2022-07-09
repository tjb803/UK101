/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2022
 */
package uk101.view;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import uk101.hardware.ROM;
import uk101.hardware.SEK;
import uk101.view.component.ViewFrame;

/**
 * Unit test for the video view
 */
public class TestSEKVideoView extends BaseViewTest {

    protected void setupTest() throws Exception {
        // Create the SEK display component and add to the frame.
        ROM charSet = new ROM(testCfg.getRomCharset());
        video = new SEK(0, charSet, testComputer);
        vidView = new VideoView(video, testCfg);
        testView.add(vidView.display());

        // Add a controller window
        controlView = new ControlView();
        testView.add(controlView.display());
    }

    private SEK video;
    private VideoView vidView;
    private ControlView controlView;

    public void testFormats() throws Exception {
        // Fill first 1K with uppercase letters
        int i = 0, ch = 0;
        while (i < video.store.length/2) {
            video.store[i++] = (byte)('A' + ch++ % 26);
        }
        // And second half with lower case
        ch = 0;
        while (i < video.store.length) {
            video.store[i++] = (byte)('a' + ch++ % 26);
        }

        while (true) {
            Thread.yield();
        }
    }

    private class ControlView extends ViewFrame implements ActionListener, ItemListener {
        private static final long serialVersionUID = 1L;

        private int format = 0, inverted = 0;

        private ControlView() {
            super("SEK Control", false);

            Container content = getContentPane();
            content.setLayout(new FlowLayout());

            ButtonGroup group = new ButtonGroup();
            for (int i = 0; i < 12; i++) {
                JRadioButton b = new JRadioButton(Integer.toString(i));
                b.addActionListener(this);
                group.add(b);
                content.add(b);
            }
            JToggleButton inv = new JToggleButton("Inverted");
            inv.addItemListener(this);
            content.add(inv);
        }

        public void actionPerformed(ActionEvent event) {
            format = Integer.parseInt(event.getActionCommand());
            video.control.writeByte(0, (byte)(format + inverted));
        }

        public void itemStateChanged(ItemEvent event) {
            inverted = event.getStateChange() == ItemEvent.SELECTED ? 128 : 0;
            video.control.writeByte(0, (byte)(format + inverted));
        }
    }
}
