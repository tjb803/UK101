/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import uk101.machine.Computer;
import uk101.view.component.DisplayText;
import uk101.view.component.FlowPanel;
import uk101.view.component.SmallButton;
import uk101.view.component.SmallToggle;

/**
 * The control and debug panel.
 */
public class MachineView extends JInternalFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    static final String MACHINE_DUMP = "Dump";
    static final String MACHINE_TRACE = "Trace";
    static final String MACHINE_RESET = "Reset";
    static final String MACHINE_NMI = "NMI";
    static final String MACHINE_IRQ = "IRQ";

    Computer computer;
    ComputerView view;
    
    DisplayText speed;
    Timer speedTimer;

    public MachineView(Computer computer, ComputerView computerView) {
        super("Machine", true, false, false, true);
        this.computer = computer;
        this.view = computerView;

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
        speed = new DisplayText("Actual", "0", false);
        int mhz = computer.cpu.getMHz();
        ip.add(new DisplayText("Cpu", (mhz == 0) ? "Max" : mhz + "MHz", false));
        ip.add(speed);
        ip.add(new DisplayText("RAM", computer.ram.kBytes() + "KB", false));
        ip.add(new DisplayText("ROM", computer.monitor.getName(), false));
        
        // Timer to update CPU actual speed
        speedTimer = new Timer(2000, this);
        speedTimer.setRepeats(true);
        speedTimer.start();

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

        FlowPanel panel = new FlowPanel(FlowPanel.VERTICAL);
        panel.add(mp);
        panel.add(ip);
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
}
