/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2017
 */
package uk101.view.component;

import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import uk101.io.Tape;

/**
 * Tape file selection dialog accessory to select tape mode.
 */
public class TapeFormat extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String MODE_AUTO = "Automatic";
    public static final String MODE_ASCII = "ASCII";
    public static final String MODE_BINARY = "Binary";
    public static final String MODE_AUDIO = "Audio";
    public static final String MODE_UNSET = " ";

    private JRadioButton iAuto, oAscii;
    private ButtonGroup inputGroup, outputGroup;

    public TapeFormat() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        iAuto = new JRadioButton((String)null, true);
        JRadioButton ia = new JRadioButton();
        JRadioButton ib = new JRadioButton();
        JRadioButton iw = new JRadioButton();
        iAuto.setActionCommand(Integer.toString(Tape.STREAM_SELECT));
        ia.setActionCommand(Integer.toString(Tape.STREAM_ASCII));
        ib.setActionCommand(Integer.toString(Tape.STREAM_BINARY));
        iw.setActionCommand(Integer.toString(Tape.STREAM_AUDIO));
        inputGroup = new ButtonGroup();
        inputGroup.add(iAuto);  inputGroup.add(ia);  inputGroup.add(ib);  inputGroup.add(iw);

        oAscii = new JRadioButton((String)null, true);
        JRadioButton ob = new JRadioButton();
        JRadioButton ow = new JRadioButton();
        oAscii.setActionCommand(Integer.toString(Tape.STREAM_ASCII));
        ob.setActionCommand(Integer.toString(Tape.STREAM_BINARY));
        ow.setActionCommand(Integer.toString(Tape.STREAM_AUDIO));
        outputGroup = new ButtonGroup();
        outputGroup.add(oAscii);  outputGroup.add(ob);  outputGroup.add(ow);

        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.Y_AXIS));
        TitledBorder hb = BorderFactory.createTitledBorder("Tape Format");
        hb.setTitleJustification(TitledBorder.CENTER);
        hb.setTitlePosition(TitledBorder.ABOVE_TOP);
        bp.setBorder(hb);
        
        JPanel heads = new JPanel();
        heads.setLayout(new BoxLayout(heads, BoxLayout.X_AXIS));
        heads.add(new JLabel("Read"));
        heads.add(Box.createHorizontalGlue());
        heads.add(new JLabel("Write"));
        bp.add(heads);
        bp.add(Box.createVerticalStrut(5));
        bp.add(buttonPair(MODE_AUTO, iAuto, null));
        bp.add(buttonPair(MODE_ASCII, ia, oAscii));
        bp.add(buttonPair(MODE_BINARY, ib, ob));
        bp.add(buttonPair(MODE_AUDIO, iw, ow));
        
        add(bp);
    }
    
    private JPanel buttonPair(String bt, JRadioButton bl, JRadioButton br) {
        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        bl.setIconTextGap(0);
        bl.setMargin(new Insets(2, 0, 2, 10));
        bp.add(bl);
        bp.add(Box.createHorizontalGlue()); 
        bp.add(new JLabel(bt));
        bp.add(Box.createHorizontalGlue());
        if (br != null) {
            br.setIconTextGap(0);
            br.setMargin(new Insets(2, 10, 2, 0));
            bp.add(br);
        } else {
            JLabel fill = new JLabel();   
            fill.setMaximumSize(bl.getPreferredSize());
            fill.setPreferredSize(bl.getPreferredSize());
            bp.add(fill);
        }
        return bp;
    }
    
    public void reset() {
      iAuto.setSelected(true);
      oAscii.setSelected(true);
    }

    public int getInputFormat() {
        return Integer.parseInt(inputGroup.getSelection().getActionCommand());
    }

    public int getOutputFormat() {
        return Integer.parseInt(outputGroup.getSelection().getActionCommand());
    }
}
