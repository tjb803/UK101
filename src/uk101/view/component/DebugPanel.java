/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015
 */
package uk101.view.component;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import uk101.view.ComputerView;

/**
 * Debug button panel for the machine view
 */
public class DebugPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public DebugPanel(AbstractButton[] buttons, ActionListener listener) {
        // Normally use a JToolBar as a way to create a compact row of small
        // buttons.  But this doesn't look good on the Nimbus look-and-feel as
        // it seems to get the size very wrong.  So for Nimbus we just add
        // small button instances to a grid.  Sadly this doesn't work well in
        // anything other than Nimbus.
        if (ComputerView.isNimbus) {
            setLayout(new GridLayout(1, 0));
            for (AbstractButton b : buttons) {
                b.addActionListener(listener);
                b.putClientProperty("JComponent.sizeVariant", "mini");
                b.updateUI();
                add(b);
            }
        } else {
            JToolBar dt = new JToolBar(JToolBar.HORIZONTAL);
            dt.setFloatable(false);
            for (AbstractButton b : buttons) {
                b.setBorder(BorderFactory.createEtchedBorder());
                b.addActionListener(listener);
                dt.add(b);
            }
            add(dt);
        }
    }
}
