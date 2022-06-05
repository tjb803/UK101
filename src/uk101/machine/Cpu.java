/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2022
 */
package uk101.machine;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import uk101.hardware.CPU6502;

/**
 * Capture and record the CPU state.
 */
public class Cpu implements Serializable {
    private static final long serialVersionUID = 1L;

    public byte A, X, Y;
    public byte S, P;
    public short PC;
    public boolean RST, NMI, IRQ;

    public Cpu(CPU6502 cpu) {
        cpu.getState(this);
    }

    /* 
     * Read and write the CPU state
     */

    public void write(OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(this);
        out.flush();
    }

    public static Cpu readCpu(InputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(stream);
        Cpu cpu = (Cpu)in.readObject();
        return cpu;
    }
}
