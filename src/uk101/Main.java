/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2012
 */
package uk101;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

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
 *   uk101.Main [options] [machine]
 *
 * where:
 *   machine: a previously saved machine image
 *
 * options:
 *   -look <lookAndFeel>: the Java UI look-and-feel (defaults to system look and feel)
 *   -c, -configuration <configFile>: a properties file containing the system configuration
 *   -p, -properties <systemProps>: one or more system configuration properties
 *   -debug: print out debug information
 */
public class Main implements Runnable {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("look", "lookAndFeel");
        options.put("c", "=configuration");  
        options.put("configuration", "configFile");
        options.put("p", "=properties");  
        options.put("properties", "systemProps");
        options.put("debug");
        Args parms = new Args(Main.class, "[machine]", args, options);
        
        Computer.debug = parms.getFlag("debug");

        // Set the Swing look and feel
        String look = parms.getOption("look");
        if (look == null) {
            setDefaultLookAndFeel();
        } else {
            setLookAndFeel(look);
        }

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
        ComputerView view = new ComputerView(computer);

        // Fire up the GUI
        Main gui = new Main(computer, view, image);
        SwingUtilities.invokeLater(gui);
    }

    // Set the default look and feel.  Try to force the platform look unless
    // the user has overridden it with something specific.
    private static void setDefaultLookAndFeel() throws Exception {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf == null || laf.getID().equals("Metal")) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
        }
    }
    
    // Try to set a specific look and feel from the command line parameter name.
    private static void setLookAndFeel(String look) throws Exception {
        String lafClass = null;
        String metalTheme = null;
        
        if (Computer.debug) {
            System.out.println("Look:");
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                String name = info.getName();
                if (name.equals("Metal"))
                    name += "/Steel/Ocean";
                System.out.println("  " + name);
            }    
        }

        // Look for an exact match first, then a likely match

        // "Steel" and "Ocean" are themes for "Metal"
        if (look.equalsIgnoreCase("Steel") || look.equalsIgnoreCase("Ocean")) {
            metalTheme = look;
            look = "Metal";
        }
            
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equalsIgnoreCase(look)) {
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

        // Set requested look and feel.  Leave as default if no match found.
        if (lafClass != null) {
            // Set Metal theme if needed
            if (metalTheme != null) {
                if (metalTheme.equalsIgnoreCase("Steel"))
                    MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                else if (metalTheme.equalsIgnoreCase("Ocean")) 
                    MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            }
            
            // Nimbus look-and-feel has some bugs it looks better to workaround
            if (lafClass.contains("nimbus"))
                Computer.nimbusFix1 = true;
            
            UIManager.setLookAndFeel(lafClass);
            JFrame.setDefaultLookAndFeelDecorated(true);
        }
    }

    /*
     * Main code needs to run on the Swing dispatch thread
     */
    private JFrame frame;
    private Computer computer;
    private ComputerView computerView;
    private boolean layout;

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
