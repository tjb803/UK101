/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010,2014
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
    
    private String name;
    private String patches;

    /*
     * Attempt to load a ROM from a binary image
     */
    public ROM(String id) throws IOException {
        super(load(name(id)));
        
        // Save the name of the ROM
        name = new File(name(id)).getName().toUpperCase();
        if (name.lastIndexOf('.') != -1)
            name = name.substring(0, name.lastIndexOf('.'));
        
        // Save any patch details for later
        patches = patch(id);
    }

    // Disable writes to ROMs.
    public void writeByte(int offset, byte b) {
        return;
    }

    // Return ROM name
    public String getName() {
        return name;
    }
    
    // Apply any patches to the ROM image
    public void patch() {
        if (patches != null) {
            System.out.println(patches);
            StringTokenizer t1 = new StringTokenizer(patches, "[];: ");
            while (t1.hasMoreTokens()) {
                String s1 = t1.nextToken();
                System.out.println(s1);
                StringTokenizer t2 = new StringTokenizer(s1, "/,");
                if (t2.hasMoreTokens()) {
                    String s2 = t2.nextToken();
                    System.out.println(s2);
                    int addr = Integer.parseInt(s2, 16) - base;
                    while (t2.hasMoreTokens()) {
                        s2 = t2.nextToken();
                        System.out.println(s2);
                        store[addr++] = (byte)Integer.parseInt(s2, 16);
                    }    
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
    private static String name(String id) {
        String name = id;
        if (id.indexOf('[') != -1)
            name = id.substring(0, id.indexOf('[')).trim();
        return name;
    }
    
    // Extract patch information or null
    private static String patch(String id) {
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
