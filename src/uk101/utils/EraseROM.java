/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015
 */
package uk101.utils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This class will erase ROM contents or create a new empty ROM.
 *
 * Usage:
 *    EraseROM [options] romfile
 *
 * where:
 *    romfile: is the name of a the ROM image file
 *
 * options:
 *    -fill byte: value to use to erase ROM, defaults to 0
 *    -new size: generate a new ROM of specific size
 *
 * ROM file size will be rounded up to a 256 byte multiple and filled with 
 * the specified byte value. 
 */
public class EraseROM {
    
    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("fill", "byte");
        options.put("new", "size");
        Args parms = new Args(EraseROM.class, "romfile", args, options);
        File romFile = parms.getOutputFile(1);
        int fill = parms.getHexInteger("fill", 0);
        int size = parms.getHexInteger("new", 0);

        // Check parameters
        if (romFile == null || (!romFile.exists() && size == 0)) {
            parms.usage();
        }
        
        // Calculate size - rounded up to a 256 byte multiple with a maximum of 64K
        if (size == 0) 
            size = (int)Math.min(65536, romFile.length());
        size = ((size+255)/256)*256;
    
        // Write ROM image
        FileOutputStream out = new FileOutputStream(romFile);
        for (int i = 0; i < size; i++) 
            out.write(fill);
        out.close();
            
        System.out.println("Erased ROM:");
        System.out.println("  " + romFile.getCanonicalPath());
        System.out.println("  Size=" + ((size%1024 == 0) ? size/1024+"KB" : size+" bytes"));

    }
}
