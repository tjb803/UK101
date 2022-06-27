/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view.component;

import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import uk101.io.Tape;

/**
 * Tape file selection dialog accessory to select tape mode.
 */
public class TapeMode extends JPanel {
    private static final long serialVersionUID = 1L;

    static final String MODE_AUTO = "Automatic";
    static final String MODE_ASCII = "ASCII";
    static final String MODE_BINARY = "Binary";
    static final String MODE_AUDIO = "Audio";
    static final String MODE_UNSET = " ";

    private ButtonGroup inputGroup, outputGroup;
    private ButtonModel inputDef, outputDef;

    public TapeMode() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBorder(BorderFactory.createTitledBorder("Tape Format"));
        add(buttons);

        JRadioButton is = new JRadioButton();
        JRadioButton ia = new JRadioButton();
        JRadioButton ib = new JRadioButton();
        JRadioButton iw = new JRadioButton();
        inputGroup = new ButtonGroup();
        inputDef = is.getModel();
        inputGroup.add(is);
        inputGroup.add(ia);
        inputGroup.add(ib);
        inputGroup.add(iw);

        JRadioButton oa = new JRadioButton();
        JRadioButton ob = new JRadioButton();
        JRadioButton ow = new JRadioButton();
        outputGroup = new ButtonGroup();
        outputDef = oa.getModel();
        outputGroup.add(oa);
        outputGroup.add(ob);
        outputGroup.add(ow);

        JPanel heads = new JPanel();
        heads.setLayout(new BoxLayout(heads, BoxLayout.X_AXIS));
        heads.setBorder(BorderFactory.createEmptyBorder(2, 1, 5, 1));
        heads.add(new JLabel("Read"));
        heads.add(Box.createHorizontalGlue());
        heads.add(new JLabel("Write"));

        buttons.add(heads);
        buttons.add(buttonPair(MODE_AUTO, is, null));
        buttons.add(buttonPair(MODE_ASCII, ia, oa));
        buttons.add(buttonPair(MODE_BINARY, ib, ob));
        buttons.add(buttonPair(MODE_AUDIO, iw, ow));
    }

    private JPanel buttonPair(String bt, JRadioButton bl, JRadioButton br) {
        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        bl.setActionCommand(bt);
        bl.setMargin(new Insets(2, 0, 0, 2));
        bp.add(bl);
        bp.add(Box.createHorizontalGlue());
        bp.add(new JLabel(bt));
        bp.add(Box.createHorizontalGlue());
        if (br != null) {
            br.setActionCommand(bt);
            br.setMargin(new Insets(2, 2, 0, 0));
            bp.add(br);
        } else {
            bp.add(Box.createRigidArea(bl.getPreferredSize()));
        }
        return bp;
    }

    public void reset() {
        inputGroup.setSelected(inputDef, true);
        outputGroup.setSelected(outputDef, true);
    }

    public int getInputFormat() {
        return asFormat(inputGroup.getSelection().getActionCommand());
    }

    public int getOutputFormat() {
        return asFormat(outputGroup.getSelection().getActionCommand());
    }

    /*
     * Convert mode string values to and from Tape stream formats
     */

    public static String asMode(int format) {
        return ((format == Tape.STREAM_UNKNOWN) ? MODE_UNSET :
                (format == Tape.STREAM_ASCII) ? MODE_ASCII :
                (format == Tape.STREAM_AUDIO) ? MODE_AUDIO : MODE_BINARY);
    }

    public static int asFormat(String mode) {
        return ((mode.equals(MODE_AUTO)) ? Tape.STREAM_SELECT :
                (mode.equals(MODE_ASCII)) ? Tape.STREAM_ASCII :
                (mode.equals(MODE_AUDIO)) ? Tape.STREAM_AUDIO : Tape.STREAM_BINARY);
    }
}
