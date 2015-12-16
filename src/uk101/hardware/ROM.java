/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2015
 */
package uk101.hardware;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import uk101.Main;

/**
 * This is a system ROM loaded from a resource.
 */
public class ROM extends Memory {
    
    protected String name;
    private String romid;
    private String patches;

    /*
     * Attempt to load a ROM from a binary image
     */
    public ROM(String id) throws IOException {
        this(id, true);
    }
    
    protected ROM(String id, boolean ro) throws IOException {
        super(load(findName(id)), ro);
        name = id;
        
        // Save the name of the ROM
        romid = new File(findName(id)).getName().toUpperCase();
        if (romid.lastIndexOf('.') != -1)
            romid = romid.substring(0, romid.lastIndexOf('.'));
        
        // Save any patch details for later
        patches = findPatch(id);
    }

    // Return ROM identifier
    public String id() {
        return romid;
    }
    
    // Apply any patches to the ROM image
    public void patch() {
        if (patches != null) {
            StringTokenizer t1 = new StringTokenizer(patches, "[];: ");
            while (t1.hasMoreTokens()) {
                StringTokenizer t2 = new StringTokenizer(t1.nextToken(), "/,");
                if (t2.hasMoreTokens()) {
                    int addr = Integer.parseInt(t2.nextToken(), 16) - base;
                    while (t2.hasMoreTokens())
                        store[addr++] = (byte)Integer.parseInt( t2.nextToken(), 16);
                }
            }
        }
    }

    // Try to load the image as a resource from the classpath, if not found try to 
    // load from the file system.  This will throw an exception if nothing can be
    // found.
    private static byte[] load(String id) throws IOException {
        String rid = id.toUpperCase();
        if (!rid.endsWith(".ROM")) 
            rid += ".ROM";

        InputStream in = Main.class.getResourceAsStream("rom/" + rid); 
        if (in == null) {
            in = Main.class.getResourceAsStream("/" + id.toUpperCase());
            if (in == null) {
                in = Main.class.getResourceAsStream("/" + id);
                if (in == null) {
                    in = new FileInputStream(id);
                }
            }    
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int b = in.read(); b != -1; b = in.read())
            out.write(b);
        in.close();
        
        return out.toByteArray();
    }
    
    // Extract name part of ROM id (excluding any patch information
    private static String findName(String id) {
        String name = id;
        if (id.indexOf('[') != -1)
            name = id.substring(0, id.indexOf('[')).trim();
        return name;
    }
    
    // Extract patch information or null
    private static String findPatch(String id) {
        String patch = null;
        if (id.indexOf('[') != -1)
            patch = id.substring(id.indexOf('[')).trim();
        return patch;
    }
    
    /*
     * Mainly for debugging
     */
    public String toString() {
        return "ROM" + super.toString() + ": " + name;
    }
}
