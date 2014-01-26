/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
 */
package uk101.machine;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import uk101.Main;
import uk101.io.AudioDecoder;
import uk101.io.AudioEncoder;
import uk101.io.KansasCityDecoder;
import uk101.io.KansasCityEncoder;
import uk101.utils.Args;

/**
 * Contains the machine configuration.
 * 
 * @author Baldwin
 */
public class Configuration extends Properties {
    private static final long serialVersionUID = 2L;
    
    public static final String CPU_SPEED = "cpu.speed";
    public static final String BAUD_RATE = "baud.rate";
    public static final String RAM_SIZE = "ram.size";
    public static final String ROM_MONITOR = "rom.monitor";
    public static final String ROM_BASIC = "rom.basic";
    public static final String ROM_CHARSET = "rom.charset";
    public static final String KEYBOARD = "keyboard";
    public static final String VIDEO_ROWS = "video.rows";
    public static final String VIDEO_COLS = "video.cols";
    public static final String SCREEN_SIZE = "screen.size";
    public static final String SCREEN_WIDTH = "screen.width";
    public static final String SCREEN_OFFSET = "screen.offset";
    public static final String SCREEN_COLOUR = "screen.colour";
    public static final String SCREEN_UPDATE = "screen.update";
    public static final String KCS_RATE = "kcs.rate";
    public static final String KCS_BITS = "kcs.bits";
    public static final String KCS_LEAD = "kcs.lead";
    public static final String ROM = "rom.";
    
    public static final String WHITE = "white";
    public static final String GREEN = "green";
    public static final String AMBER = "amber";
    public static final String SYNC = "sync";
    public static final String ASYNC = "async";
    public static final String UK = "uk";
    public static final String US = "us";
    
    // Additional ROMs have an address and a filename
    public static class ROM {
        public int address;
        public String name;
        private ROM(String addr, String value) {
            address = Integer.parseInt(addr, 16);
            name = value;
        }
    }
    
    public Configuration(Args parms, Configuration initial) throws IOException {
        // Load a properties file, if specified, and apply any command line
        // overrides.  Then update the default configuration from the property
        // values.
        Properties props = new Properties();
        InputStream in = Main.class.getResourceAsStream("/uk101.properties");
        if (in != null) {
            props.load(in);
            in.close();
        }
        File f = parms.getInputFile("configuration");
        if (f != null) {
            in = new FileInputStream(f);
            props.load(in);
            in.close();
        }
        String s = parms.getOption("properties");
        if (s != null) {
            s = s.replace("\\", "\\\\").replace(";", "\n");
            in = new ByteArrayInputStream(s.getBytes("ISO8859_1"));
            props.load(in);
            in.close();
        }

        // Load the default property set and apply any user defined changes
        load(Main.class.getResourceAsStream("rom/uk101.properties"));
        if (initial != null)
            applyProperties(initial);
        applyProperties(props);
    }
    
    private void applyProperties(Properties props) {
        applyInt(props, CPU_SPEED, 0, 2);
        applyStr(props, BAUD_RATE, "110", "300", "600", "1200", "2400", "4800", "9600");
        applyInt(props, RAM_SIZE, 4, 40);
        applyStr(props, ROM_MONITOR);
        applyStr(props, ROM_BASIC);
        applyStr(props, ROM_CHARSET);
        applyStr(props, KEYBOARD, UK, US);
        applyInt(props, VIDEO_ROWS, 16, 32);
        applyInt(props, VIDEO_COLS, 32, 64);
        applyInt(props, SCREEN_SIZE, 1, 2);
        applyInt(props, SCREEN_WIDTH, 16, 64);
        applyInt(props, SCREEN_OFFSET, 0, 63);
        applyStr(props, SCREEN_COLOUR, WHITE, GREEN, AMBER);
        apply(props, SCREEN_COLOUR, "screen.color", 0, 0, WHITE, GREEN, AMBER);
        applyStr(props, SCREEN_UPDATE, SYNC, ASYNC);
        applyInt(props, KCS_RATE, 8000, 96000);
        applyStr(props, KCS_BITS, "8", "16");
        applyInt(props, KCS_LEAD, 0, 10);
        applyROM(props);
    }
    
    private void applyInt(Properties props, String key, int min, int max) {
        try {
            apply(props, key, key, min, max);
        } catch (NumberFormatException e) { // Ignore bad numeric values
        }
    }
    
    private void applyStr(Properties props, String key, String... range) {
        apply(props, key, key, 0, 0, range);
    }
    
    private void apply(Properties props, String key, String ukey, int min, int max, String... range) {
        String value = props.getProperty(ukey);
        if (value != null) {
            value = value.trim();
            if (max > 0) {
                int i = Integer.parseInt(value);
                if (i >= min && i <= max) 
                    setProperty(key, Integer.toString(i));
            } else if (range.length > 0) {
                String s = value.toLowerCase();
                for (int i = 0; i < range.length; i++) {
                    if (s.equals(range[i]))
                        setProperty(key, s);
                }    
            } else {
                setProperty(key, value);
            }
        }
    }
    
    private void applyROM(Properties props) {
        for (Enumeration<?> k = props.propertyNames(); k.hasMoreElements(); ) {
            String key = (String)k.nextElement();
            String addr = hasAddr(key);
            if (addr != null) {
                setProperty(ROM+addr, props.getProperty(key).trim());
            }
        }
    }
    
    private String hasAddr(String key) {
        String addr = null;
        if (key.startsWith(ROM)) {
            String hex = key.substring(ROM.length()).toUpperCase();
            if (hex.matches("[0-9A-F]{4}")) {
                addr = hex;
            }
        }
        return addr;
    }
  
    /*
     * Return config values either as integers or strings
     */
    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }
    
    public String getValue(String key) {
        return getProperty(key);
    }
    
    /*
     * Return the KCS audio encoder/decoder based on configuration settings 
     */
    public AudioEncoder getAudioEncoder() {
        KansasCityEncoder kcs = new KansasCityEncoder(getInt(KCS_RATE), getInt(KCS_BITS), getInt(BAUD_RATE));
        kcs.setLeader(getInt(KCS_LEAD)*1000, getInt(KCS_LEAD)*1000); 
        return kcs;        
    }
    
    public AudioDecoder getAudioDecoder() {
        KansasCityDecoder kcs = new KansasCityDecoder(getInt(BAUD_RATE));
        return kcs;        
    }
    
    /*
     * Return any additional ROMs listed
     */
    public Collection<ROM> getROMs() {
        Collection<ROM> roms = new ArrayList<ROM>();
        for (Enumeration<?> k = propertyNames(); k.hasMoreElements(); ) {
            String key = (String)k.nextElement();
            String addr = hasAddr(key);
            if (addr != null) {
                roms.add(new ROM(addr, getProperty(key)));
            }
        }
        return roms;
    }
    
    /*
     * Write configuration.
     */
    public void write(OutputStream stream) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(this);
        out.flush();
    }
    
    /*
     * Read a configuration. 
     */
    public static Configuration readImage(InputStream stream) throws IOException, ClassNotFoundException {
         ObjectInputStream in = new ObjectInputStream(stream);
         Configuration cfg = (Configuration)in.readObject();
         return cfg;
    }
    
    /*
     * Print configuration
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Enumeration<?> k = propertyNames(); k.hasMoreElements(); ) {
            String key = (String)k.nextElement();
            s.append(key).append("=").append(getProperty(key)).append("\n");
        }
        return s.toString();
    }
}
