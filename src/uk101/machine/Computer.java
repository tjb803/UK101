/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2015
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
        cpu = new CPU6502(cfg.getInt(Configuration.CPU_SPEED), cfg.getValue(Configuration.CPU_CONTROL), this);

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
        keyboard = new Keyboard(cfg.getValue(Configuration.KBD_LAYOUT));
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
     * Set the CPU clock speed
     */
    public void setSpeed(int mhz) {
        cpu.setMHz(mhz);
    }

    /*
     * Implement the DataBus interface used by the processor to access
     * memory in the address space.
     *
     * If we read from non-existent memory the UK101 hardware would return
     * the high byte of the memory address.
     */
    public byte readByte(int addr) {
        byte b;
        Memory m = memory[Memory.asBlock(addr)];
        if (m != null) {
            b = m.readByte(addr-m.base);
        } else {
            b = Data.getHiByte((short)addr);
            // Small hack to avoid nasty video artifacts when MONUK02 scrolls the
            // screen.  This monitor will read past the end of the video RAM when it
            // scrolls and very briefly display garbage on the screen.  It is very 
            // brief and presumably isn't visible on a real machine but can sometimes
            // be seen in this emulation.  So if this looks like a read by the video 
            // scroll routine we return 0x20 to keep the screen clean!
            if (cpu.getPC() == 0xFB72) 
                b = 0x20;
        }
        return b;
    }

    public void writeByte(int addr, byte value) {
        Memory m = memory[Memory.asBlock(addr)];
        if (m != null) {
            m.writeByte(addr-m.base, value);
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

    private Trace trace;

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
        cpu = new CPU6502(1, "sleep", null);
        acia = new ACIA6850(300, Thread.NORM_PRIORITY);
    }
}
