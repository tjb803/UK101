/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import uk101.machine.Computer;
import uk101.machine.Configuration;
import uk101.utils.Args;
import uk101.view.ComputerView;
import uk101.view.MachineImage;

/**
 * This is the main entry to the Compukit UK101 simulator, it will start the GUI
 * view of computer and allow it to be operated.
 *
 * Usage:
 *   Main [options] [machine]
 *
 * where:
 *   machine: a previously saved machine image
 *
 * options:
 *   -look lookAndFeel: the Java UI look-and-feel (defaults to system look and feel)
 *   -c, -configuration configFile: a properties file containing the system configuration
 *   -p, -properties systemProps: one or more system configuration properties
 *
 * @author Baldwin
 */
public class Main implements Runnable {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Map<String,String> options = Args.optionMap();
        options.put("look", "lookAndFeel");
        options.put("c", "configuration-");  options.put("configuration", "configFile");
        options.put("p", "properties-");  options.put("properties", "systemProps");
        Args parms = new Args("View", "[machine]", args, options);

        // Set the Swing look and feel
        setLookAndFeel(parms.getOption("look"));

        // Get machine image to restore
        File imageFile = parms.getInputFile(1);
        MachineImage image = null;
        Configuration imageCfg = null;
        if (imageFile != null) {
            image = MachineImage.readImage(imageFile);
            imageCfg = image.imageCfg;
        }    

        // Create a new UK101 simulation and view
        Configuration config = new Configuration(parms, imageCfg);
        Computer computer = new Computer(config);
        ComputerView view = new ComputerView(computer, config);

        // Fire up the GUI
        Main gui = new Main(computer, view, image);
        SwingUtilities.invokeLater(gui);
    }

    // Try to set the Swing look and feel
    private static void setLookAndFeel(String look) throws Exception {
        String lafClass = null;

        // Look for an exact match first, then a likely match
        if (look != null) {
            if (look.equals("LIST")) {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                    System.err.println("look: " + info.getName());
            }

            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals(look)) {
                    lafClass = info.getClassName();
                    break;
                }
            }
            if (lafClass == null) {
                look = look.toUpperCase();
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().toUpperCase().contains(look)) {
                        lafClass = info.getClassName();
                        break;
                    }
                }
            }
        }

        // No match, so use the system default
        if (lafClass == null) {
            lafClass = UIManager.getSystemLookAndFeelClassName();
        }

        UIManager.setLookAndFeel(lafClass);
        JFrame.setDefaultLookAndFeelDecorated(true);
    }

    /*
     * Main code needs to run on the Swing dispatch thread
     */
    JFrame frame;
    Computer computer;
    ComputerView computerView;
    boolean layout;

    public Main(Computer comp, ComputerView view, MachineImage image) {
        this.computer = comp;
        this.computerView = view;

        Image icon = Toolkit.getDefaultToolkit().createImage(Main.class.getResource("icon/uk101-32.png"));

        frame = new JFrame(computer.name + " Simulation (v" + computer.version + ")");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(icon);
        frame.setContentPane(view);

        // Ensure we start-up and shut-down cleanly
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                computer.start();
            }

            public void windowClosed(WindowEvent e) {
                computer.shutdown();
                System.exit(0);
            }
        });

        // Set the default window layout and then apply any saved image
        layout = view.defaultLayout();
        if (image != null) {
            layout = image.apply(computer, view);
        }
    }

    public void run() {
        // If layout is not complete (ie from a restored machine image) we
        // need to pack the frame and position it in the centre of the screen.
        if (!layout) {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            frame.pack();
            frame.setLocation((screen.width - frame.getWidth())/2, (screen.height - frame.getHeight())/2);
        }
        frame.setVisible(true);
        computerView.focusKeyboard();
    }
}
