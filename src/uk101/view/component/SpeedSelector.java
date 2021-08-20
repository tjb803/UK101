/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015
 */
package uk101.view.component;

import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

/**
 * Slider to select the CPU clock speed.
 */
public class SpeedSelector extends JSlider {
    private static final long serialVersionUID = 1L;
    
    static {    // Improve appearance on GTK look-and-feel
        UIManager.put("Slider.paintValue", Boolean.FALSE);
    }

    int max;

    public SpeedSelector(int max, int value, ChangeListener listener) {
        super(1, max+1, value);
        this.max = max;

        Hashtable<Integer,JComponent> labels = createStandardLabels(1);
        labels.put(max+1, new JLabel(" Max "));

        setLabelTable(labels);
        setMajorTickSpacing(1);
        setPaintLabels(true);
        setSnapToTicks(true);
        addChangeListener(listener);
    }

    public int getSpeed() {
        return (getValue() > max ? 0 : getValue());
    }
}
