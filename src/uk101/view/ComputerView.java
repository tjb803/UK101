/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import uk101.machine.Computer;
import uk101.view.component.ImageFormat;

/**
 * A visual representation of the full computer.  This is a frame that contains the
 * visuals for the various computer elements.
 */
public class ComputerView extends JDesktopPane implements ActionListener {
    private static final long serialVersionUID = 1L;

    public static boolean isMac, isNimbus;

    // Improve appearance on GTK look-and-feel and detect native Mac and Nimbus
    // as some things need special handling for these looks and feels :-(
    static {
        UIManager.put("InternalFrame.useTaskBar", Boolean.FALSE);
        LookAndFeel lf = UIManager.getLookAndFeel();
        isMac = (lf != null && (lf.getID().equals("Mac") || lf.getID().equals("Aqua")));
        isNimbus = (lf != null && (lf.getID().equals("Nimbus")));
    }

    static final String IMAGE_LOAD = "Load...";
    static final String IMAGE_SAVE = "Save...";

    private Computer computer;
    private JFileChooser imageSelect;

    private MachineView machine;
    private VideoView video;
    private KeyboardView keyboard;
    private CassetteView cassette;

    public ComputerView(Computer computer) {
        this.computer = computer;

        // Pure black background (if used) is a bit too dark
        if (getBackground().equals(Color.BLACK)) {
            setBackground(Color.DARK_GRAY);
        }

        // Create views for the various machine elements
        video = new VideoView(computer.video, computer.config);
        keyboard = new KeyboardView(computer, computer.keyboard, computer.config);
        cassette = new CassetteView(computer.recorder, computer.config);
        machine = new MachineView(computer, this);

        // Attach the keyboard handler to each top level frame
        video.attachKeyboard(keyboard);
        keyboard.attachKeyboard(keyboard);
        cassette.attachKeyboard(keyboard);
        machine.attachKeyboard(keyboard);

        add(machine.display());
        add(cassette.display());
        add(video.display());     // Add video and keyboard last to make sure
        add(keyboard.display());  // they appear on top.

        // Create a file chooser dialog for the load/save image function
        imageSelect = new JFileChooser(new File(".").getAbsolutePath());
        imageSelect.setDialogTitle("UK101 Machine Image");
        imageSelect.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".uk101"));
            }
            public String getDescription() {
                return "UK101 Machine Images";
            }
        });
    }

    // Layout all the windows in their default positions
    public boolean defaultLayout() {
        int maxX1 = Math.max(video.getWidth(), keyboard.getWidth());
        int maxX2 = Math.max(machine.getWidth(), cassette.getWidth());
        int maxX = maxX1 + maxX2 + 5;

        int maxY1 = video.getHeight() + keyboard.getHeight() + 2;
        int maxY2 = machine.getHeight() + cassette.getHeight() + 5;
        int maxY = Math.max(maxY1, maxY2);

        int vidX = 0, vidY = 0;
        int kybX = 0, kybY = maxY - keyboard.getHeight();
        if (video.getWidth() > keyboard.getWidth()) {
            kybX = (video.getWidth() - keyboard.getWidth())/2;
        } else {
            vidX = (keyboard.getWidth() - video.getWidth())/2;
        }
        int macX = maxX - machine.getWidth(), macY = 0;
        int casX = maxX - cassette.getWidth(), casY = maxY - cassette.getHeight();

        video.setLocation(vidX, vidY);
        keyboard.setLocation(kybX, kybY);
        machine.setLocation(macX, macY);
        cassette.setLocation(casX, casY);

        setPreferredSize(new Dimension(maxX, maxY));

        return false;       // Layout is incomplete (frame is unsized)
    }

    // Set focus to the keyboard
    public void focusKeyboard() {
        try {
            keyboard.setSelected(true);
        } catch (PropertyVetoException e) {
        }
    }

    /*
     * Prompt to load and save machine images
     */

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(IMAGE_LOAD)) {
            ImageFormat format = new ImageFormat(false);
            imageSelect.setAccessory(format);
            if (imageSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = imageSelect.getSelectedFile();
                MachineImage image = MachineImage.readImage(file);
                image.apply(computer, null);
            }
        } else if (e.getActionCommand().equals(IMAGE_SAVE)) {
            ImageFormat format = new ImageFormat(true);
            imageSelect.setAccessory(format);
            if (imageSelect.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                MachineImage image = new MachineImage(computer, this,
                        format.saveSnapshot(), format.saveProperties(), format.savePostions());
                File file = imageSelect.getSelectedFile();
                if (!file.getName().contains("."))
                    file = new File(file.getPath() + ".uk101");
                image.write(file);
            }
        }
    }
}
