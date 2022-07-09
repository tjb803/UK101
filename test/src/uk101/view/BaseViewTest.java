/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import junit.framework.TestCase;
import uk101.machine.Computer;
import uk101.machine.Configuration;
import uk101.utils.Args;

/**
 * General Swing test frame
 */
public abstract class BaseViewTest extends TestCase {

    protected abstract void setupTest() throws Exception;

    protected void setUp() throws Exception {
        // Test Computer instance
        testCfg = new Configuration(new Args("", "", new String[0], null), null);
        testComputer = new Computer(testCfg);

        // Create a multi-document frame for machine view tests
        testFrame = new JFrame("Machine View Tester");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(new Dimension(800, 600));
        testFrame.setPreferredSize(testFrame.getSize());
        testFrame.setLocation(new Point(100, 100));

        testView = new JDesktopPane();
        testView.setLayout(null);
        setupTest();

        // And display for testing
        testFrame.setContentPane(testView);
        testFrame.pack();
        testFrame.setVisible(true);
    }

    protected JFrame testFrame;
    protected JDesktopPane testView;
    protected Computer testComputer;
    protected Configuration testCfg;
}
