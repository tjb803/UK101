/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.hardware;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import uk101.Main;

/**
 * This is a system ROM loaded from a resource.
 *
 * @author Baldwin
 */
public class ROM extends Memory {

    private String name;

    /*
     * Attempt to load a ROM from a binary image
     */
    public ROM(String id) throws IOException {
        // Save the name of the ROM
        name = new File(id).getName().toUpperCase();
        if (name.lastIndexOf('.') != -1)
            name = name.substring(0, name.lastIndexOf('.'));
        
        // Try to load the image as a resource from the classpath, if not found try to 
        // load from the file system.  This will throw an exception if nothing can be
        // found.
        InputStream in = Main.class.getResourceAsStream("rom/" + id); 
        if (in == null) {
            in = Main.class.getResourceAsStream("/" + id);
            if (in == null) {
                in = new FileInputStream(id);
            }    
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int b = in.read(); b != -1; b = in.read())
            out.write(b);
        in.close();
        setStore(out.toByteArray());
    }

    // Disable writes to ROMs.
    public void writeByte(int offset, byte b) {
        return;
    }

    // Return ROM name
    public String getName() {
        return name;
    }
    
    /*
     * Mainly for debugging
     */
    public String toString() {
        return "ROM" + super.toString() + ": " + name;
    }
}
