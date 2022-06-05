/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2022
 */
package uk101.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import uk101.machine.Computer;
import uk101.machine.Configuration;
import uk101.machine.Cpu;
import uk101.machine.Dump;

/**
 * A saved machine image.  Currently this consists of a memory dump,
 * the CPU registers, the system configuration and the size and position 
 * of all the windows on the screen.
 */
public class MachineImage {
    static final byte HAS_CONFIG = 0x01;
    static final byte HAS_VIEW = 0x02;
    static final byte HAS_SNAP = 0x04;

    public Cpu imageCpu;
    public Dump imageDump, imageVid;
    public Configuration imageCfg;
    public ViewImage imageView;

    public MachineImage() {
    }

    public MachineImage(Computer computer, ComputerView view, boolean saveSnap, boolean saveCfg, boolean saveView) {
        imageDump = new Dump(computer);
        if (saveSnap) {
            imageCpu = new Cpu(computer.cpu);
            imageVid = new Dump(computer.video);
        }
        if (saveCfg) {
            imageCfg = computer.config;
        }
        if (saveView) {
            imageView = new ViewImage(view);
        }
    }

    /*
     * Apply the machine image to the current simulation
     */
    public boolean apply(Computer computer, ComputerView view) {
        boolean layout = false;
        computer.restore(imageDump, imageCpu, imageVid);
        if (view != null && imageView != null) {
            layout = imageView.layout(view);
        }
        return layout;
    }

    /*
     * Write this machine image to a file
     */
    public void write(File file) {
        try {
            // Write memory dump
            OutputStream stream = new DeflaterOutputStream(new FileOutputStream(file));
            imageDump.write(stream);

            // Write optional items
            if (imageCfg != null || imageView != null) {
                // Start with optional items flag to show what is present
                int flags = 0;
                if (imageCpu != null) flags |= HAS_SNAP;
                if (imageCfg != null) flags |= HAS_CONFIG;
                if (imageView != null) flags |= HAS_VIEW;
                stream.write(flags);

                if ((flags & HAS_SNAP) != 0) {
                    imageCpu.write(stream);
                    imageVid.write(stream);
                }
                if ((flags & HAS_CONFIG) != 0) {
                    imageCfg.write(stream);
                }
                if ((flags & HAS_VIEW) != 0) {
                    imageView.write(stream);
                }
            }
            stream.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /*
     * Read a previously saved image
     */
    public static MachineImage readImage(File file) {
        // Machine image contains a RAM dump followed optional components:
        // CPU state, video memory, system configuration and UI view layout.
        MachineImage machine = new MachineImage();
        try {
            // Memory image is always present
            InputStream stream = new InflaterInputStream(new FileInputStream(file));
            machine.imageDump = Dump.readDump(stream);

            // Read optional components
            int flags = stream.read();
            if (flags != -1) {
                if ((flags & HAS_SNAP) != 0) {
                    machine.imageCpu = Cpu.readCpu(stream);
                    machine.imageVid = Dump.readDump(stream);
                }
                if ((flags & HAS_CONFIG) != 0) {
                    machine.imageCfg = Configuration.readImage(stream);
                }
                if ((flags & HAS_VIEW) != 0) {
                    machine.imageView = ViewImage.readImage(stream);
                }
            }
            stream.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return machine;
    }
}
