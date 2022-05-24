/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2015,2022
 */
package uk101.utils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This class will erase NVRAM contents or create a new empty RAM.
 *
 * Usage:
 *    EraseRAM [options] ramfile
 *
 * where:
 *    ramfile: is the name of a the NVRAM image file
 *
 * options:
 *    -fill byte: value to use to erase RAM, defaults to 0
 *    -new size: generate a new NVRAM of specific size
 *
 * RAM file size will be rounded up to a 256 byte multiple and filled with 
 * the specified byte value. 
 */
public class EraseRAM {

    public static void main(String[] args) throws Exception {
        // Handle parameters
        Args.Map options = Args.optionMap();
        options.put("fill", "byte");
        options.put("new", "size");
        Args parms = new Args(EraseRAM.class, "ramfile", args, options);
        File ramFile = parms.getOutputFile(1);
        int fill = parms.getHexInteger("fill", 0);
        int size = parms.getHexInteger("new", 0);

        // Check parameters
        if (ramFile == null || (!ramFile.exists() && size == 0)) {
            parms.usage();
        }

        // Calculate size - rounded up to a 256 byte multiple with a maximum of 64K
        if (size == 0) 
            size = (int)Math.min(65536, ramFile.length());
        size = ((size+255)/256)*256;

        // Write ROM image
        FileOutputStream out = new FileOutputStream(ramFile);
        for (int i = 0; i < size; i++) 
            out.write(fill);
        out.close();

        System.out.println("Erased RAM:");
        System.out.println("  " + ramFile.getCanonicalPath());
        System.out.println("  Size=" + ((size%1024 == 0) ? size/1024+"KB" : size+" bytes"));

    }
}
