/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2017
 */
package uk101.view.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * An indicator light. Displays a colour to indicate activity and goes
 * out if not refreshed for 1 second.
 */
public class CassetteLight extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    static final Dimension SIZE = new Dimension(20, 9);
    
    Timer busyTimer;

    public CassetteLight() {
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setMaximumSize(SIZE);
        setPreferredSize(SIZE);
        busyTimer = new Timer(1000, this);
        busyTimer.setRepeats(false);
    }
    
    public void setOn(Color colour) {
        busyTimer.restart();
        setBackground(colour);
        setOpaque(true);
        repaint();
    }
    
    public void actionPerformed(ActionEvent e) {
        setOpaque(false);
        repaint();
    }
}
