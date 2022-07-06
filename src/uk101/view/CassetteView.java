/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
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
import uk101.view.component.TapeMode;
import uk101.view.component.ViewFrame;

/**
 * A visual representation of the cassette recorder.
 */
public class CassetteView  extends ViewFrame implements ActionListener, ItemListener {
    private static final long serialVersionUID = 1L;

    private TapeRecorder recorder;

    private JLabel name;
    private DisplayText format;
    private CassetteButton record, play, stop, eject;
    private CassetteLight indicator;
    private JFileChooser select;
    private Timer autoStop;
    private File tapeFile;
    private InputStream tapeIn;
    private OutputStream tapeOut;
    private int formatIn, formatOut;
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
        format = new DisplayText(null, TapeMode.asMode(Tape.STREAM_UNKNOWN), true);
        JPanel tp = new JPanel();
        tp.setLayout(new BoxLayout(tp, BoxLayout.X_AXIS));
        tp.setBorder(BorderFactory.createTitledBorder("Tape file"));
        tp.add(name);
        tp.add(Box.createHorizontalGlue());
        tp.add(format);

        // On the Mac the buttons are wide and I'm not sure how to change them, so they
        // are best arranged in a 2x2 grid rather than the 1x4 used elsewhere.
        int gx = 1, gy = 4;
        if (ComputerView.isMac) {
            gx = 2; gy = 2;
        }

        JPanel bg = new JPanel();
        bg.setLayout(new GridLayout(gx, gy, 5, 0));
        bg.setAlignmentY(BOTTOM_ALIGNMENT);
        record = new CassetteButton("\u25CF", "Rec", Color.RED, this);
        play = new CassetteButton("\u25BA", "Play", Color.BLACK, this);
        stop = new CassetteButton("\u25A0", "Stop", Color.BLACK, this);
        eject = new CassetteButton("\u25B2", "Eject", Color.BLUE, this);
        bg.add(record);
        bg.add(play);
        bg.add(stop);
        bg.add(eject);

        ButtonGroup group = new ButtonGroup();
        group.add(record.button);
        group.add(play.button);
        group.add(stop.button);
        group.add(eject.button);
        stop.button.setSelected(true);

        // File selection dialog
        select = new JFileChooser(new File(".").getAbsolutePath());
        select.setAccessory(new TapeMode());
        select.setDialogTitle(getTitle() + " - Select Tape");
        select.setAcceptAllFileFilterUsed(true);

        JPanel ip = new JPanel();
        ip.setLayout(new BoxLayout(ip, BoxLayout.Y_AXIS));
        indicator = new CassetteLight();
        indicator.setAlignmentX(RIGHT_ALIGNMENT);
        ip.add(Box.createVerticalStrut(3));
        ip.add(indicator);
        ip.add(Box.createVerticalGlue());
        JButton open = new JButton("Open...");
        open.setAlignmentX(RIGHT_ALIGNMENT);
        open.addActionListener(this);
        ip.add(open);
        ip.setAlignmentY(BOTTOM_ALIGNMENT);

        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        bp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bp.add(bg);
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
        } else {    // Must be the "Open" button
            TapeMode tm = (TapeMode)select.getAccessory();
            tm.reset();
            if (select.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
                eject();
                tapeFile = select.getSelectedFile();
                formatIn = tm.getInputFormat();
                formatOut = tm.getOutputFormat();
                load();
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
                record();
            } else if (e.getItem() == play.button) {
                record.button.setEnabled(false);
                autoStop.start();
                play();
            } else if (e.getItem() == stop.button || e.getItem() == eject.button) {
                play.button.setEnabled(true);
                record.button.setEnabled(true);
                autoStop.stop();
                stop();
                if (e.getItem() == eject.button)
                    eject();
            }
        }
    }

    // Implementation methods for cassette player function

    private void load() {
        name.setText(tapeFile.getName());
        format.setValue(TapeMode.asMode(Tape.STREAM_UNKNOWN));
    }

    private void record() {
        if (tapeOut == null && tapeFile != null) {
            if (!tapeFile.exists()) {
                tapeOut = Tape.getOutputStream(tapeFile, formatOut, 132, audioEncoder);
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
                    tapeOut = Tape.getOutputStream(tapeFile, formatOut, 132, audioEncoder);
                } else {
                    stop.button.doClick();
                }
            }
            if (tapeOut != null) {
                format.setValue(TapeMode.asMode(Tape.getFormat(tapeOut)));
                recorder.setOutputTape(tapeOut);
            }
        }
        if (tapeOut != null)
            recorder.startTape();
    }

    private void play() {
        if (tapeIn == null && tapeFile != null) {
            tapeIn = Tape.getInputStream(tapeFile, formatIn, audioDecoder);
            if (tapeIn != null) {
                format.setValue(TapeMode.asMode(Tape.getFormat(tapeIn)));
                recorder.setInputTape(tapeIn);
            }
        }
        if (tapeIn != null)
            recorder.startTape();
    }

    private void stop() {
        recorder.stopTape();
    }

    private void eject() {
        name.setText(null);
        format.setValue(TapeMode.asMode(Tape.STREAM_UNKNOWN));
        tapeFile = null;
        tapeIn = null;
        tapeOut = null;
        recorder.ejectTape();
    }

    /*
     * Called when tapes are being actively read or written
     */
    public void setActive(boolean write) {
        indicator.setOn((write) ? Color.RED : Color.GREEN);
        autoStop.restart();
    }
}
