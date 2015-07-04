/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2015
 */
package uk101.view;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk101.machine.Computer;
import uk101.view.component.DisplayText;
import uk101.view.component.SmallButton;
import uk101.view.component.SmallToggle;
import uk101.view.component.SpeedSelector;

/**
 * The control and debug panel.
 */
public class MachineView extends JInternalFrame implements ActionListener, ChangeListener {
    private static final long serialVersionUID = 1L;
    
    static final String MACHINE_DUMP = "Dump";
    static final String MACHINE_TRACE = "Trace";
    static final String MACHINE_RESET = "Reset";
    static final String MACHINE_NMI = " NMI ";
    static final String MACHINE_IRQ = " IRQ ";

    private Computer computer;
    
    private DisplayText speed, baud;
    private SpeedSelector cpuClock;
    private Timer speedTimer;

    public MachineView(Computer computer, ComputerView computerView) {
        super("Machine", false, false, false, true);
        this.computer = computer;

        // Timer to update CPU actual speed
        speedTimer = new Timer(2000, this);
        speedTimer.setInitialDelay(5000);
        speedTimer.setRepeats(true);
        speedTimer.start();

        // Machine image load/save
        JPanel mp = new JPanel();
        mp.setAlignmentY(CENTER_ALIGNMENT);
        mp.setBorder(BorderFactory.createTitledBorder("Machine"));
        JButton load = new JButton(ComputerView.IMAGE_LOAD);
        JButton save = new JButton(ComputerView.IMAGE_SAVE);
        load.addActionListener(computerView);
        save.addActionListener(computerView);
        mp.add(load);  mp.add(Box.createHorizontalStrut(5));
        mp.add(save);

        // Information panel
        JPanel ip = new JPanel(new GridLayout(0, 2, 5, 5));
        ip.setAlignmentY(CENTER_ALIGNMENT);
        ip.setBorder(BorderFactory.createTitledBorder("Configuration"));
        speed = new DisplayText("CPU", computer.cpu.getMHz() + ".00MHz");
        baud = new DisplayText("Baud Rate", computer.acia.getBaudRate() + "");
        ip.add(speed);
        ip.add(baud);
        ip.add(new DisplayText("RAM", computer.ram.kBytes() + "KB"));
        ip.add(new DisplayText("ROM", computer.monitor.getName()));

        // Debug panel
        JPanel dp = new JPanel();
        dp.setAlignmentY(CENTER_ALIGNMENT);
        dp.setBorder(BorderFactory.createTitledBorder("Debug"));
        if (!Computer.nimbusFix1) {
            // Adding small buttons to a JToolBar seems to work and look 
            // better in most look-and-feels ...
            JToolBar db = new JToolBar(JToolBar.HORIZONTAL);
            db.setFloatable(false);
            db.addSeparator();
            db.add(new SmallButton(MACHINE_DUMP, this));  db.addSeparator();
            db.add(new SmallToggle(MACHINE_TRACE, this)); db.addSeparator();
            db.add(new SmallButton(MACHINE_RESET, this)); db.addSeparator();
            db.add(new SmallButton(MACHINE_NMI, this));   db.addSeparator();
            db.add(new SmallButton(MACHINE_IRQ, this));   db.addSeparator(); 
            dp.add(db);
        } else {
            // ... however not in Nimbus as it gets JToolBar sizes wrong.  So
            // we just use a regular flow panel here.  Annoying.
            JPanel db = new JPanel();
            db.add(new SmallButton(MACHINE_DUMP, this));
            db.add(new SmallToggle(MACHINE_TRACE, this)); 
            db.add(new SmallButton(MACHINE_RESET, this));
            db.add(new SmallButton(MACHINE_NMI, this));
            db.add(new SmallButton(MACHINE_IRQ, this)); 
            dp.add(db);
        }    
    
        // CPU Speed control panel
        JPanel sp = new JPanel();
        sp.setAlignmentY(CENTER_ALIGNMENT);
        sp.setBorder(BorderFactory.createTitledBorder("CPU Clock Speed"));
        cpuClock = new SpeedSelector(4, computer.cpu.getMHz(), this);
        sp.add(cpuClock);
   
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(mp);  panel.add(Box.createVerticalStrut(3));
        panel.add(ip);  panel.add(Box.createVerticalStrut(3));
        panel.add(sp);  panel.add(Box.createVerticalStrut(3));
        panel.add(dp);
        
        Container content = getContentPane();
        content.add(panel);
        pack();
        setVisible(true);
    }

    /*
     * Button actions
     */

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == speedTimer) {
            speed.setValue(String.format("%-1.2fMHz", computer.cpu.getSpeed()));
            baud.setValue(Integer.toString(computer.acia.getBaudRate()));
        } else if (e.getActionCommand().equals(MACHINE_DUMP)) {
            computer.dump();
        } else if (e.getActionCommand().equals(MACHINE_TRACE)) {
            computer.trace(((SmallToggle)e.getSource()).isSelected());
        } else if (e.getActionCommand().equals(MACHINE_RESET)) {
            computer.cpu.signalReset();
        } else if (e.getActionCommand().equals(MACHINE_NMI)) {
            computer.cpu.signalNMI();
        } else if (e.getActionCommand().equals(MACHINE_IRQ)) {
            computer.cpu.signalIRQ();
        }
    }

    /*
     * CPU clock speed slider
     */
    
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == cpuClock) {
            if (!cpuClock.getValueIsAdjusting()) {
                computer.setSpeed(cpuClock.getSpeed());
            }    
        }
    }
}
