/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2015
 */
package uk101.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk101.machine.Computer;
import uk101.view.component.SpeedSelector;
import uk101.view.component.DisplayText;
import uk101.view.component.FlowPanel;
import uk101.view.component.SmallButton;
import uk101.view.component.SmallToggle;

/**
 * The control and debug panel.
 */
public class MachineView extends JInternalFrame implements ActionListener, ChangeListener {
    private static final long serialVersionUID = 1L;
    
    static final String MACHINE_DUMP = "Dump";
    static final String MACHINE_TRACE = "Trace";
    static final String MACHINE_RESET = "Reset";
    static final String MACHINE_NMI = "NMI";
    static final String MACHINE_IRQ = "IRQ";

    private Computer computer;
    
    private DisplayText speed, baud;
    private SpeedSelector cpuClock;
    private Timer speedTimer;

    public MachineView(Computer computer, ComputerView computerView) {
        super("Machine", true, false, false, true);
        this.computer = computer;

        // Timer to update CPU actual speed
        speedTimer = new Timer(2000, this);
        speedTimer.setInitialDelay(5000);
        speedTimer.setRepeats(true);
        speedTimer.start();

        // Machine image load/save
        JPanel mp = new JPanel(new GridLayout(1, 0, 5, 5));
        mp.setBorder(BorderFactory.createTitledBorder("Machine"));
        JButton load = new JButton(ComputerView.IMAGE_LOAD);
        JButton save = new JButton(ComputerView.IMAGE_SAVE);
        load.addActionListener(computerView);
        save.addActionListener(computerView);
        mp.add(load);
        mp.add(save);

        // Information panel
        JPanel ip = new JPanel(new GridLayout(0, 2, 5, 5));
        ip.setBorder(BorderFactory.createTitledBorder("Configuration"));
        speed = new DisplayText("CPU", computer.cpu.getMHz() + ".00MHz", false);
        baud = new DisplayText("Baud Rate", computer.acia.getBaudRate() + "", false);
        ip.add(speed);
        ip.add(baud);
        ip.add(new DisplayText("RAM", computer.ram.kBytes() + "KB", false));
        ip.add(new DisplayText("ROM", computer.monitor.getName(), false));

        // Debug panel
        JPanel db = new JPanel(new GridLayout(1, 0, 3, 3));
        db.setBorder(BorderFactory.createTitledBorder("Debug"));
        SmallButton dump = new SmallButton(MACHINE_DUMP, this);
        SmallToggle trace = new SmallToggle(MACHINE_TRACE, this);
        SmallButton reset = new SmallButton(MACHINE_RESET, this);
        SmallButton nmi = new SmallButton(MACHINE_NMI, this);
        SmallButton irq = new SmallButton(MACHINE_IRQ, this);
        db.add(dump);
        db.add(trace);
        db.add(reset);
        db.add(nmi);
        db.add(irq);
        
        // CPU Speed control panel
        JPanel sp = new JPanel(new GridLayout(1, 0, 0, 0));
        sp.setBorder(BorderFactory.createTitledBorder("CPU Clock Speed"));
        cpuClock = new SpeedSelector(4, computer.cpu.getMHz());
        cpuClock.addChangeListener(this);
        sp.add(cpuClock);
        
         // Size the speed panel to be no wider than debug panel
        Dimension d = sp.getPreferredSize();
        d.setSize(Math.min(d.width, db.getPreferredSize().width), d.height);
        sp.setPreferredSize(d);
        
        JPanel panel = new FlowPanel(FlowPanel.VERTICAL);
        panel.add(mp);
        panel.add(ip);
        panel.add(sp);
        panel.add(db);
        
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
                computer.setSpeed(cpuClock.getMhz());
            }    
        }
    }
}
