/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
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
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import uk101.Main;
import uk101.utils.Args;

/**
 * Contains the machine configuration.
 * 
 * @author Baldwin
 */
public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String WHITE = "white";
    public static final String GREEN = "green";
    public static final String AMBER = "amber";
    
    // Property processing
    static Collection<String> colours = Arrays.asList(new String[] {WHITE, GREEN, AMBER});
    static Item[] items = {
        new Item("cpuSpeed", "cpu.speed", 0, 2),
        new Item("ramSize", "ram.size", 4, 40),
        new Item("videoRows", "video.rows", 16, 32),
        new Item("videoCols", "video.cols", 32, 64),
        new Item("screenSize", "screen.size", 1, 2),
        new Item("screenWidth", "screen.width", 16, 64),
        new Item("screenOffset", "screen.offset", 0, 63),
        new Item("screenColour", "screen.colour", colours),
        new Item("screenColour", "screen.color", colours),
        new Item("romBASIC", "rom.basic", null),
        new Item("romMonitor", "rom.monitor", null),
        new Item("romCharset", "rom.charset", null),
    };
    
    // Default hardware configuration
    public int cpuSpeed = 1;
    public int ramSize = 8;
    public int videoRows = 16, videoCols = 64;
    public String romBASIC = "BASUK101.ROM";
    public String romMonitor = "MONUK02.ROM";
    public String romCharset = "CHGUK101.ROM";
   
    // Default view configuration
    public int screenSize = 1;
    public int screenWidth = 48, screenOffset = 13;
    public String screenColour = WHITE;
 
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
            in = new ByteArrayInputStream(s.replace(';', '\n').getBytes("ISO8859_1"));
            props.load(in);
            in.close();
        }

        // Apply any settings found in the initial config or the properties
        if (initial != null || !props.isEmpty()) {
            for (Item item : items) {
                item.init(initial, this);
                item.parse(props, this);
            }    
        }    
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

    // Configuration item
    private static class Item {
        static final int INT = 0;
        static final int STR = 1;

        int type;
        String var, key;
        int min, max;
        Collection<String> range;
        
        Item(String var, String key, int min, int max) {
            this.type = INT;
            this.var = var;  this.key = key;
            this.min = min;  this.max = max;
        }
        
        Item(String var, String key, Collection<String> range) {
            this.type = STR;
            this.var = var;  this.key = key;
            this.range = range;
        }
        
        void init(Configuration initial, Configuration cfg) {
            if (initial != null) {
                try {
                    Field f = Configuration.class.getField(var);
                    f.set(cfg, f.get(initial));
                } catch (Exception e) {     // Not supposed to happen!
                }
            }    
        }
        
        void parse(Properties props, Configuration cfg) {
            if (props.containsKey(key)) {
                try {
                    if (type == INT) {
                        int i = Integer.parseInt(props.getProperty(key).trim());
                        if (i >= min && i <= max) {
                            Configuration.class.getField(var).setInt(cfg, i);
                        }
                    } else if (type == STR) {
                        String s = props.getProperty(key).trim();
                        if (range == null) {
                            Configuration.class.getField(var).set(cfg, s);
                        } else {
                            s = s.toLowerCase();
                            if (range.contains(s)) {
                                Configuration.class.getField(var).set(cfg, s);   
                            }    
                        }
                    }
                } catch (Exception e) {     // Not supposed to happen!
                }
            }
        }
    }
}
