/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.view.component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import uk101.io.Stream;

/**
 * Tape file selection dialog accessory to select tape mode.
 *
 * @author Baldwin
 */
public class TapeFormat extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String MODE_AUTO = "Automatic";
    public static final String MODE_ASCII = "ASCII";
    public static final String MODE_BINARY = "Binary";
    public static final String MODE_AUDIO = "Audio";
    public static final String MODE_UNSET = " ";

    ButtonGroup inputGroup, outputGroup;

    public TapeFormat() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Tape Format"));

        JPanel ip = new JPanel();
        ip.setLayout(new BoxLayout(ip, BoxLayout.Y_AXIS));
        ip.setBorder(BorderFactory.createTitledBorder("Input"));
        JRadioButton ix = new JRadioButton(MODE_AUTO, true);
        JRadioButton ia = new JRadioButton(MODE_ASCII);
        JRadioButton ib = new JRadioButton(MODE_BINARY);
        ix.setActionCommand(Integer.toString(Stream.STREAM_SELECT));
        ia.setActionCommand(Integer.toString(Stream.STREAM_ASCII));
        ib.setActionCommand(Integer.toString(Stream.STREAM_BINARY));
        inputGroup = new ButtonGroup();
        inputGroup.add(ix);  inputGroup.add(ia);  inputGroup.add(ib);
        ip.add(ix);  ip.add(Box.createVerticalStrut(2));
        ip.add(ia);  ip.add(Box.createVerticalStrut(2));
        ip.add(ib);

        JPanel op = new JPanel();
        op.setLayout(new BoxLayout(op, BoxLayout.Y_AXIS));
        op.setBorder(BorderFactory.createTitledBorder("Output"));
        JRadioButton oa = new JRadioButton(MODE_ASCII, true);
        JRadioButton ob = new JRadioButton(MODE_BINARY);
        JRadioButton ow = new JRadioButton(MODE_AUDIO);
        oa.setActionCommand(Integer.toString(Stream.STREAM_ASCII));
        ob.setActionCommand(Integer.toString(Stream.STREAM_BINARY));
        ow.setActionCommand(Integer.toString(Stream.STREAM_AUDIO));
        outputGroup = new ButtonGroup();
        outputGroup.add(oa);  outputGroup.add(ob);  outputGroup.add(ow);
        op.add(oa);  op.add(Box.createVerticalStrut(2));
        op.add(ob);  op.add(Box.createVerticalStrut(2));
        op.add(ow);

        add(ip);
        add(Box.createVerticalStrut(5));
        add(op);
    }

    public int getInputFormat() {
        return Integer.parseInt(inputGroup.getSelection().getActionCommand());
    }

    public int getOutputFormat() {
        return Integer.parseInt(outputGroup.getSelection().getActionCommand());
    }
}
