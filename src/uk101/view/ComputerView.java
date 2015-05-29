/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.filechooser.FileFilter;

import uk101.machine.Computer;
import uk101.machine.Configuration;
import uk101.view.component.ImageFormat;

/**
 * A visual representation of the full computer.  This is a frame that contains the
 * visuals for the various computer elements.
 *
 * @author Baldwin
 */
public class ComputerView extends JDesktopPane implements ActionListener {
    private static final long serialVersionUID = 1L;

    static final String IMAGE_LOAD = "Load...";
    static final String IMAGE_SAVE = "Save...";

    private Computer computer;
    private JFileChooser imageSelect;

    private MachineView machine;
    private VideoView video;
    private KeyboardView keyboard;
    private CassetteView cassette;

    public ComputerView(Computer computer, Configuration cfg) {
        setLayout(null);

        this.computer = computer;

        // Create views for the various machine elements
        video = new VideoView(computer.video, cfg);
        keyboard = new KeyboardView(computer, computer.keyboard, cfg);
        cassette = new CassetteView(computer.recorder, cfg);
        machine = new MachineView(computer, this);
        
        // Attach the keyboard handler to each top level frame
        attachKeyboard(video, keyboard);
        attachKeyboard(keyboard, keyboard);
        attachKeyboard(cassette, keyboard);
        attachKeyboard(machine, keyboard);

        add(machine);
        add(cassette);
        add(video);     // Add video and keyboard last to make sure
        add(keyboard);  // they appear on top.

        // Create a file chooser dialog for the load/save image function
        imageSelect = new JFileChooser(new File("."));
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
        // First some messiness related to the GTK+ look and feel on Linux.  This seems
        // to want to add a TaskBar at the bottom in a layer that overlays our windows
        // (in the default layer).  If we can find something resembling this bar (in the
        // non-default layer) we need to adjust our size to allow for it.
        int extraY = 0;
        for (Component c: getComponents()) {    // Only handle TaskBar at the bottom
            if (getLayer(c) > DEFAULT_LAYER && c.getY() < 0) {
                extraY = Math.max(extraY, c.getHeight()-1);
            }
        }
        
        int maxX1 = video.getWidth() + machine.getWidth() + 5;
        int maxX2 = keyboard.getWidth() + cassette.getWidth() + 10;
        int maxX = Math.max(maxX1, maxX2);
        int maxY1 = keyboard.getHeight() + video.getHeight();
        int maxY2 = keyboard.getHeight() + machine.getHeight();
        int maxY = Math.max(maxY1,  maxY2);
        
        int macX = maxX - machine.getWidth(), macY = 0;
        machine.setLocation(macX, macY);

        int vidX = 0, vidY = 0;
        if (video.getWidth() < keyboard.getWidth()) {
            vidX = (keyboard.getWidth() - video.getWidth())/2;
            if (vidX + video.getWidth() > macX) {
                vidX = 0;
            }
        }
        video.setLocation(vidX, vidY);

        int kybX = 0, kybY = maxY - keyboard.getHeight();
        if (keyboard.getWidth() < video.getWidth()) {
            kybX = (video.getWidth() - keyboard.getWidth())/2;
        }
        keyboard.setLocation(kybX, kybY);

        int casX = kybX + keyboard.getWidth() + 10, casY = maxY - cassette.getHeight();
        cassette.setLocation(casX, casY);

        Dimension size = new Dimension(maxX, maxY + extraY);
        setPreferredSize(size);

        return false;       // Layout is incomplete (frame is unsized)
    }
    
    // Attach a keyListener to a top level window and ensure that no 
    // subcomponents can grab focus.
    private void attachKeyboard(JInternalFrame frame, KeyListener listener) {
        frame.setFocusable(true);
        frame.addKeyListener(listener);
        for (Component c : frame.getComponents()) {
            removeFocus(c);
        }
    }
    
    private void removeFocus(Component c) {
        c.setFocusable(false);
        if (c instanceof Container) {
            for (Component cc : ((Container)c).getComponents()) {
                removeFocus(cc);
            }
        }
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
                File file = imageSelect.getSelectedFile();
                if (!file.getName().contains("."))
                    file = new File(file.getPath() + ".uk101");
                ComputerView saveView = format.savePostions() ? this : null;
                Configuration saveCfg = format.saveProperties() ? computer.config : null;
                MachineImage image = new MachineImage(computer, saveView, saveCfg);
                image.write(file);
            }
        }
    }
}
