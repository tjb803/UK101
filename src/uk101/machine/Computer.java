/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2016
 */
package uk101.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import uk101.hardware.ACIA6850;
import uk101.hardware.CPU6502;
import uk101.hardware.EPROM;
import uk101.hardware.Keyboard;
import uk101.hardware.Memory;
import uk101.hardware.RAM;
import uk101.hardware.ROM;
import uk101.hardware.Video;
import uk101.hardware.bus.DataBus;

/**
 * The complete UK101 computer.
 */
public class Computer extends Thread implements DataBus {
    
    // General debug flag
    public static boolean debug = false;
    
    // Some flags to control a few special hacks 
    public static boolean videoFix1 = false;
    public static boolean aciaFix1 = false;

    // Version
    public String name;
    public String version;

    public CPU6502 cpu;
    public Memory[] memory;

    public RAM ram;
    public ROM basic;
    public ROM monitor;
    
    public Collection<ROM> roms;
    public Collection<RAM> rams;
    public Collection<EPROM> eproms;

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
        cpu = new CPU6502(cfg.getCpuSpeed(), cfg.getCpuControl(), this);

        // Address space is 64K.  Assumption here is that any ROM/RAM or any
        // memory-mapped devices are mapped in BLKSIZE sections.
        memory = new Memory[Memory.toBlocks(Memory.K64)];
        
        // Install additional RAM blocks first so they do not overwrite ROMs
        rams = new ArrayList<RAM>();
        for (Configuration.Mem ram : cfg.getRAMs()) {
            RAM r = new RAM(ram.size);
            addMemory(ram.address, r);
            rams.add(r);
        } 

        // Install the system ROMs and RAM
        ram = new RAM(cfg.getRamSize());
        basic = new ROM(cfg.getRomBasic());
        monitor = new ROM(cfg.getRomMonitor());
        addMemory(cfg.getRamAddr(), ram);
        addMemory(cfg.getBasicAddr(), basic);
        addMemory(cfg.getMonitorAddr(), monitor);
        
        // Install any additional ROMs and EPROMS
        roms = new ArrayList<ROM>();
        for (Configuration.Mem rom : cfg.getROMs()) {
            ROM r = new ROM(rom.name);
            addMemory(rom.address, r);
            roms.add(r);
        }
        eproms = new ArrayList<EPROM>();
        for (Configuration.Mem eeprom : cfg.getEPROMs()) {
            EPROM e = new EPROM(eeprom.name);
            addMemory(eeprom.address, e);
            eproms.add(e);
        } 

        // Keyboard, screen and ACIA are memory mapped.
        ROM charset = new ROM(cfg.getRomCharset());
        keyboard = new Keyboard(cfg.getKbdLayout());
        video = new Video(cfg.getVideoRows(), cfg.getVideoCols(), charset);
        acia = new ACIA6850(cfg.getAciaRate(), getPriority());
        addMemory(cfg.getKbdAddr(), keyboard);
        addMemory(cfg.getVideoAddr(), video);
        addMemory(cfg.getAciaAddr(), acia);

        // Create a tape recorder to load and save programs and plug it into the ACIA.
        recorder = new TapeRecorder(acia);
        
        // Set special flags for some emulation hacks.
        String ms = new String(monitor.store, "US-ASCII");
        boolean cegmon = ms.contains("CEGMON");         // Looks like CEGMON rom
        boolean newmon = ms.contains("(C)old Start");   // Looks like New Monitor rom
        boolean synmon = ms.contains("D/C/W/M ?");      // Looks like original/OSI rom
        boolean wemon = ms.contains("WEMON");           // Looks like WEMON rom
        aciaFix1 = newmon | synmon;
        videoFix1 = newmon;
    }    

    // Add some memory into the address space, applying any patches if 
    // we are installing a ROM.
    private void addMemory(int base, Memory m) {
        m.base = base;
        int bb = Memory.asBlock(base);
        for (int i = 0; i < m.blocks; i++) {
            if (bb+i < memory.length)
               memory[bb+i] = m;
        }
    }
    
    private void addMemory(int base, ROM r) {
        addMemory(base, (Memory)r);
        r.patch();
    }
    
    /*
     * Reset the computer
     */
    public void reset() {
        cpu.signalReset();
    }
    
    /*
     * Set the CPU clock speed and store back in config so it will be saved in 
     * any machine image.
     */
    public void setSpeed(int mhz) {
        cpu.setMHz(mhz);
        config.setCpuSpeed(mhz);
    }
    
    /*
     * Store the keyboard game mode back in config so it will be saved in any
     * machine image.
     */
    public void setGameMode(boolean gameMode) {
        config.setKbdMode((gameMode) ? Configuration.GAME : Configuration.NORMAL);
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
            // TODO: When the standard monitor scrolls the screen it ends up reading
            // past the end of video memory and very briefly writes garbage characters
            // to the screen buffer.  It is very brief and doesn't seem to show on a
            // real machine but can show on my simulation.  So if this looks like the
            // monitor scroll routine we'll return a space character to keep the screen
            // clean.
            if (Computer.videoFix1 && cpu.getPC() == 0xFB72) 
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
        for (EPROM e : eproms) {
            e.close();
        }    
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
