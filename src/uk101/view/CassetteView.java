/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2017
 */
package uk101.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import uk101.io.AudioDecoder;
import uk101.io.AudioEncoder;
import uk101.io.Tape;
import uk101.machine.Configuration;
import uk101.machine.TapeRecorder;
import uk101.view.component.CassetteButton;
import uk101.view.component.CassetteLight;
import uk101.view.component.DisplayText;
import uk101.view.component.TapeFormat;
import uk101.view.component.ViewFrame;

/**
 * A visual representation of the cassette recorder.
 */
public class CassetteView  extends ViewFrame implements ActionListener, ItemListener {
    private static final long serialVersionUID = 1L;

    private TapeRecorder recorder;

    private JLabel name;
    private DisplayText format;
    private CassetteButton record, play, stop;
    private CassetteLight indicator;
    private JFileChooser select;
    private Timer autoStop;

    private File tapeFile;
    private int inFormat, outFormat;
    private AudioEncoder audioEncoder;
    private AudioDecoder audioDecoder;

    public CassetteView(TapeRecorder recorder, Configuration cfg) {
        super("Cassette Recorder", true);
        this.recorder = recorder;

        recorder.setView(this);
        audioEncoder = cfg.getAudioEncoder();
        audioDecoder = cfg.getAudioDecoder();
 
        // Create an auto-stop timer.  Stops the cassette player if it has
        // not been used for 15 seconds.
        autoStop = new Timer(15000, this);
        autoStop.setRepeats(false);

        name = new JLabel(" ");
        format = new DisplayText(null, TapeFormat.MODE_UNSET, true);
        JPanel tp = new JPanel();
        tp.setLayout(new BoxLayout(tp, BoxLayout.X_AXIS));
        tp.setBorder(BorderFactory.createTitledBorder("Tape file"));
        tp.add(name);
        tp.add(Box.createHorizontalGlue());
        tp.add(format);

        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        bp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        record = new CassetteButton("\u25CF", "Rec", Color.RED, this);
        play = new CassetteButton("\u25BA", "Play", Color.BLACK, this);
        stop = new CassetteButton("\u25A0", "Stop", Color.BLACK, this);
        bp.add(record);
        bp.add(Box.createHorizontalStrut(5));
        bp.add(play);
        bp.add(Box.createHorizontalStrut(5));
        bp.add(stop);

        ButtonGroup group = new ButtonGroup();
        group.add(record.button);
        group.add(play.button);
        group.add(stop.button);
        stop.button.setSelected(true);

        // File selection dialog
        select = new JFileChooser(new File(".").getAbsolutePath());
        select.setAccessory(new TapeFormat());
        select.setDialogTitle(getTitle() + " - Select Tape");
        select.setAcceptAllFileFilterUsed(true);

        JPanel ip = new JPanel();
        ip.setLayout(new BoxLayout(ip, BoxLayout.Y_AXIS));
        indicator = new CassetteLight();
        indicator.setAlignmentX(RIGHT_ALIGNMENT);
        ip.add(indicator);
        ip.add(Box.createVerticalStrut(8));
        JButton open = new JButton("Open...");
        open.setAlignmentX(RIGHT_ALIGNMENT);
        open.addActionListener(this);
        ip.add(open);
        ip.setAlignmentY(BOTTOM_ALIGNMENT);
        bp.add(Box.createHorizontalStrut(25));
        bp.add(ip);

        Container content = getContentPane();
        content.add(tp, BorderLayout.NORTH);
        content.add(bp, BorderLayout.SOUTH);
    }

    /*
     * Open button to select a file or autoStop timer fired
     */

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == autoStop) {
            stop.button.doClick();
        } else {    // Must be the Open... button
            TapeFormat tf = (TapeFormat)select.getAccessory();
            tf.reset();
            if (select.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
                tapeFile = select.getSelectedFile();
                inFormat = tf.getInputFormat();
                outFormat = tf.getOutputFormat();
                loadTape();
            }
        }
    }

    /*
     * Record/Play/Stop buttons to set input/output stream
     */

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getItem() == record.button) {
                play.button.setEnabled(false);
                autoStop.start();
                if (tapeFile != null)
                    recordTape();
            } else if (e.getItem() == play.button) {
                record.button.setEnabled(false);
                autoStop.start();
                if (tapeFile != null)
                    playTape();
            } else if (e.getItem() == stop.button) {
                play.button.setEnabled(true);
                record.button.setEnabled(true);
                autoStop.stop();
                if (tapeFile != null)
                    stopTape();
            }
        }
    }

    // Implementation methods for cassette player function

    private void loadTape() {
        name.setText(tapeFile.getName());
        format.setValue(TapeFormat.MODE_UNSET);
    }

    private void recordTape() {
        OutputStream out = null;
        if (!tapeFile.exists()) {
            out = Tape.getOutputStream(tapeFile, outFormat, 132, audioEncoder);
        } else { 
            String[] msg = {
                "File " + tapeFile.getPath() + " already exists.",
                "Are you sure you want to overwrite it?"
            };
            String[] opts = { "No", "Yes" };
            // I don't think showInternalOptionDialog works properly (at least in some look 
            // and feels) when the initialValue is something other than the first option.
            // So I have reversed YES and NO here - a return value of NO_OPTION means YES!
            if (JOptionPane.showInternalOptionDialog(this, 
                    msg, getTitle(), JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE, null, opts, opts[0]) == JOptionPane.NO_OPTION) {
                out = Tape.getOutputStream(tapeFile, outFormat, 132, audioEncoder);
            } else {
                stop.button.doClick();
            }
        }
        if (out != null) {
            format.setValue(tapeFormat(Tape.getFormat(out)));
            recorder.setOutputTape(out);
        }
    }

    private void playTape() {
        InputStream in = Tape.getInputStream(tapeFile, inFormat, audioDecoder);
        if (in != null) {
            format.setValue(tapeFormat(Tape.getFormat(in)));
            recorder.setInputTape(in);
        }
    }

    private void stopTape() {
        format.setValue(TapeFormat.MODE_UNSET);
        recorder.setInputTape(null);
        recorder.setOutputTape(null);
    }

    private String tapeFormat(int format) {
        return ((format == Tape.STREAM_ASCII) ? TapeFormat.MODE_ASCII : 
                (format == Tape.STREAM_AUDIO) ? TapeFormat.MODE_AUDIO : TapeFormat.MODE_BINARY);
    }

    /*
     * Called when tapes are being actively read or written
     */
    public void setRead() {
        indicator.setOn(Color.GREEN);
        autoStop.restart();
    }

    public void setWrite() {
        indicator.setOn(Color.RED);
        autoStop.restart();
    }
}
