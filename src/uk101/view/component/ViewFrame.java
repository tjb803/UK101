/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015
 */
package uk101.view.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;

import uk101.view.ComputerView;

/**
 * Base class for the internal window elements
 */
public class ViewFrame extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    
    public ViewFrame(String title, boolean minimize) {
        super(title, false, false, false, minimize);

        // Only affects Mac: removes attempts to draw shadows under the windows
        // which doesn't work properly on many OS X versions and/or JDK levels.
        if (ComputerView.isMac) {
            putClientProperty("JInternalFrame.frameType", "normal");
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1,1,1,1), getBorder()));
        }    
    }
    
    // Attach a keyListener to a top level window and ensure that no 
    // subcomponents can grab focus.
    public void attachKeyboard(KeyListener listener) {
        setFocusable(true);
        addKeyListener(listener);
        for (Component c : getComponents()) {
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

    // Pack and display the window
    public ViewFrame display() {
        pack();
        setVisible(true);
        return this;
    }
}
