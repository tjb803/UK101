/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2013
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
    
    // General debug flag
    public static boolean debug = false;

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
        
        // Lower the simulation thread priority a little to ensure the GUI
        // remains responsive.
        setName(name);
        setPriority(Math.max(Thread.MIN_PRIORITY, getPriority()/2));
        
        // Save the configuration
        config = cfg;
        
        // A 6502 CPU
        cpu = new CPU6502(cfg.getInt(Configuration.CPU_SPEED), this);

        // Address space is 64K.  Assumption here is that any ROM/RAM or any
        // memory-mapped devices are mapped in BLKSIZE sections.
        memory = new Memory[Memory.toBlocks(Memory.K64)];

        // Install the system ROMs and RAM
        ram = new RAM(cfg.getInt(Configuration.RAM_SIZE));
        basic = new ROM(cfg.getValue(Configuration.ROM_BASIC));
        monitor = new ROM(cfg.getValue(Configuration.ROM_MONITOR));
        addMemory(0, ram);
        addMemory(0xA000, basic);
        addMemory(0xF800, monitor);
        
        // Install any additional ROMs
        for (Configuration.ROM rom : cfg.getROMs()) {
            addMemory(rom.address, new ROM(rom.name));
        }    

        // Keyboard, screen and ACIA are memory mapped.
        ROM charset = new ROM(cfg.getValue(Configuration.ROM_CHARSET));
        keyboard = new Keyboard(cfg.getValue(Configuration.KEYBOARD));
        video = new Video(cfg.getInt(Configuration.VIDEO_ROWS), cfg.getInt(Configuration.VIDEO_COLS), charset);
        acia = new ACIA6850(cfg.getInt(Configuration.BAUD_RATE), getPriority());
        addMemory(0xDF00, keyboard);
        addMemory(0xD000, video);
        addMemory(0xF000, acia);

        // Create a tape recorder to load and save programs and plug it into the ACIA.
        recorder = new TapeRecorder(acia);
    }

    // Add some memory into the address space
    private void addMemory(int base, Memory m) {
        m.base = base;
        for (int i = 0; i < m.blocks; i++) {
            memory[Memory.asBlock(base) + i] = m;
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
        Memory m = memory[Memory.asBlock(addr)];
        if (m != null) {
            b = m.readByte(addr-m.base);
        }
        return b;
    }

    public short readWord(int addr) {
        byte bl = 32, bh = 32;
        Memory m = memory[Memory.asBlock(addr)];
        if (m != null) {
            bl = m.readByte(addr-m.base);
            bh = m.readByte(addr-m.base+1);
        }
        return Data.getWord(bh, bl);
    }

    public void writeByte(int addr, byte value) {
        Memory m = memory[Memory.asBlock(addr)];
        if (m != null) {
            m.writeByte(addr-m.base, value);
        }
    }

    public void writeWord(int addr, short value) {
        Memory m = memory[Memory.asBlock(addr)];
        if (m != null) {
            m.writeByte(addr-m.base, Data.getLoByte(value));
            m.writeByte(addr-m.base+1, Data.getHiByte(value));
        }
    }

    /*
     * Run the simulation thread
     */
    public void run() {
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
        acia = new ACIA6850(300, Thread.NORM_PRIORITY);
    }
}
