/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2011
 */
package uk101.machine;

import java.io.IOException;

import uk101.hardware.ACIA6850;
import uk101.hardware.CPU6502;
import uk101.hardware.Keyboard;
import uk101.hardware.Memory;
import uk101.hardware.RAM;
import uk101.hardware.ROM;
import uk101.hardware.Video;
import uk101.hardware.bus.DataBus;

/**
 * The complete UK101 computer.
 *
 * @author Baldwin
 */
public class Computer extends Thread implements DataBus {

    // Version
    public String name;
    public String version;

    public CPU6502 cpu;
    public Memory[] memory;

    public RAM ram;
    public ROM basic;
    public ROM monitor;

    public Keyboard keyboard;
    public Video video;
    public ACIA6850 acia;

    public TapeRecorder recorder;
    
    public Configuration config;

    /*
     * Create the collection of hardware that forms the complete computer.
     */
    public Computer(Configuration cfg) throws IOException {
        // Get name and version information from manifest, or set default
        name = getClass().getPackage().getImplementationTitle();
        version = getClass().getPackage().getImplementationVersion();
        if (name == null || version == null) {
            name = "Compukit UK101";
            version = "0.0.0";
        }
        
        // Save the configuration
        config = cfg;
        
        // A 6502 CPU
        cpu = new CPU6502(cfg.cpuSpeed, this);

        // Address space is 64K.  Assumption here is that any ROM/RAM or any
        // memory-mapped devices are mapped in BLKSIZE sections.
        memory = new Memory[Memory.toBlocks(Memory.K64)];

        // Install the system ROMs and RAM
        ram = new RAM(cfg.ramSize);
        basic = new ROM(cfg.romBASIC);
        monitor = new ROM(cfg.romMonitor);
        addMemory(0, ram);
        addMemory(0xA000, basic);
        addMemory(0xF800, monitor);

        // Keyboard, screen and ACIA are memory mapped.
        keyboard = new Keyboard(cfg.keyboard);
        video = new Video(cfg.videoRows, cfg.videoCols, new ROM(cfg.romCharset));
        acia = new ACIA6850();
        addMemory(0xDF00, keyboard);
        addMemory(0xD000, video);
        addMemory(0xF000, acia);

        // Create a tape recorder to load and save programs and plug it into the ACIA.
        recorder = new TapeRecorder();
        acia.setDevice(recorder);
    }

    // Add some memory into the address space, removing anything previously
    // mapped at the same starting address.
    private void addMemory(int base, Memory mb) {
        Memory m = memory[base/Memory.BLKSIZE];
        if (m != null) {
            for (int i = 0; i < m.blocks; i++) {
                memory[base/Memory.BLKSIZE + i] = null;
            }
        }

        mb.base = base;
        for (int i = 0; i < mb.blocks; i++) {
            memory[base/Memory.BLKSIZE + i] = mb;
        }
    }

    /*
     * Reset the computer
     */
    public void reset() {
        cpu.signalReset();
    }

    /*
     * Implement the DataBus interface used by the processor to access
     * memory in the address space.
     *
     * It shouldn't really matter what value is returned if we try to read 
     * non-existent memory - I suspect the real machine would return 0 or 
     * -1.  But I'm returning 32 (the space character) as when the monitor
     * ROM scrolls the screen it sometimes seems to read beyond the end of
     * the video RAM.  I guess on the real machine this either didn't
     * happen or the video logic didn't show anything, but I was seeing 
     * garbage characters on the screen very briefly.  By returning space
     * characters this is avoided.
     */
    public byte readByte(int addr) {
        byte b = 32;
        Memory m = memory[addr/Memory.BLKSIZE];
        if (m != null) {
            b = m.readByte(addr-m.base);
        }
        return b;
    }

    public short readWord(int addr) {
        byte bl = 32, bh = 32;
        Memory m = memory[addr/Memory.BLKSIZE];
        if (m != null) {
            bl = m.readByte(addr-m.base);
            bh = m.readByte(addr-m.base+1);
        }
        return Data.getWord(bh, bl);
    }

    public void writeByte(int addr, byte value) {
        Memory m = memory[addr/Memory.BLKSIZE];
        if (m != null) {
            m.writeByte(addr-m.base, value);
        }
    }

    public void writeWord(int addr, short value) {
        Memory m = memory[addr/Memory.BLKSIZE];
        if (m != null) {
            m.writeByte(addr-m.base, Data.getLoByte(value));
            m.writeByte(addr-m.base+1, Data.getHiByte(value));
        }
    }

    /*
     * Run the simulation thread
     */
    public void run() {
        // Lower the simulation thread priority a little to ensure the GUI
        // remains responsive.
        setPriority(Math.max(Thread.MIN_PRIORITY, getPriority()/2));
        setName(name);

        // Run the CPU
        cpu.signalReset();
        cpu.run();
    }

    public void shutdown() {
        trace(false);
        recorder.shutdown();
    }

    /*
     * Debugging functions
     */

    Trace trace;

    public void trace(boolean enable) {
        if (enable) {
            if (trace == null) {
                trace = new Trace(this);
                cpu.trace(trace);
            }    
        } else {
            if (trace != null) {
                cpu.trace(null);
                trace.write();
                trace = null;
            }    
        }
    }

    public void dump() {
        Dump dump = new Dump(this);
        dump.write();
    }

    public void restore(Dump dump) {
        ram.restore(dump.store);
    }

    /*
     * Mainly for debugging
     */
    public String toString() {
        return "UK101: " + ram.kBytes() + "K RAM";
    }

    /*
     * Dummy constructor for unit tests
     */
    public Computer(boolean test) throws IOException {
        ram = new RAM(4);
        monitor = new ROM("MONUK02.ROM");
        cpu = new CPU6502(1, null);
    }
}
