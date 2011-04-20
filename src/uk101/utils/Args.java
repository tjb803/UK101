/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Utility class to process command line arguments.
 * 
 * General form is a set of positional arguments (accessed by position
 * number) and a set of options (introduced by a '-' and accessed by
 * option name).
 * 
 * Options are defined by entries in a Args.Map option map where
 *   key = option name
 *   value = option value (as displayed in usage summary)
 *   
 * Some special cases for the option value are:
 *   null - option is a flag, either present or absent
 *   ends with a '+' - usage summary shows next option on the same line
 *   ends with a '-' - option is a synonym for another option  
 *
 * @author Baldwin
 */
public class Args {
    
    public static class Map extends LinkedHashMap<String,String> {
        private static final long serialVersionUID = 1L;
    }

    String name, parms;
    Args.Map opts;
    HashMap<String,String> options = new HashMap<String,String>();
    List<String> parameters = new ArrayList<String>();

    public static Args.Map optionMap() {
        return new Args.Map();
    }

    /*
     * Process a set of arguments, creating a list of options and parameters
     */
    public Args(String name, String parms, String[] args, Args.Map opts) {
        this.name = name;
        this.parms = parms;
        this.opts = opts;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                if (opts != null && opts.containsKey(arg)) {
                    String remap = opts.get(arg);
                    if (remap != null && remap.endsWith("-"))
                        arg = remap.substring(0, remap.length()-1);
                }
                if (opts != null && opts.containsKey(arg)) {
                    if (opts.get(arg) != null && i < args.length-1)
                        options.put(arg, args[++i]);
                    else
                        options.put(arg, null);
                } else {
                    error("Incorrect option", arg, true);
                }
            } else {
                parameters.add(arg);
            }
        }
    }

    /*
     * Return options/parameters in various forms
     */
    public String getParameter(int position) {
        String result = null;
        if (position <= parameters.size()) {
            result = parameters.get(position-1);
        }
        return result;
    }

    public List<String> getParameters(int firstPos) {
        return getParameters(firstPos, 0);
    }

    public List<String> getParameters(int firstPos, int lastPos) {
        List<String> results = new ArrayList<String>();
        for (int i = firstPos; lastPos == 0 || i <= lastPos; i++) {
            String result = getParameter(i);
            if (result == null)
                break;
            results.add(result);
        }
        return results;
    }

    public String getOption(String option) {
        return options.get(option);
    }

    public boolean getFlag(String option) {
        return options.containsKey(option);
    }

    public File getInputFile(int position) {
        return getNamedInputFile(getParameter(position));
    }

    public File getInputFile(String option) {
        return getNamedInputFile(getOption(option));
    }

    private File getNamedInputFile(String name) {
        File result = null;
        if (name != null) {
            result = new File(name);
            if (!result.exists() || !result.isFile() || !result.canRead()) {
                error("Cannot open input file", name, false);
            }
        }
        return result;
    }

    public List<File> getInputFiles(int position) {
        return getNamedInputFiles(getParameters(position));
    }

    private List<File> getNamedInputFiles(List<String> names) {
        List<File> results = new ArrayList<File>();
        for (String name : names)
            results.add(getNamedInputFile(name));
        return results;
    }

    public File getOutputFile(int position) {
        return getNamedOutputFile(getParameter(position));
    }

    public File getOutputFile(String option) {
        return getNamedOutputFile(getOption(option));
    }

    private File getNamedOutputFile(String name) {
        File result = null;
        if (name != null) {
            result = new File(name);
            if (result.exists() && (!result.isFile() || !result.canWrite())) {
                error("Cannot open output file", name, false);
            }
        }
        return result;
    }

    public int getInteger(int position) {
        return getIntegerValue(getParameter(position));
    }

    public int getInteger(String option) {
        return getIntegerValue(getOption(option));
    }

    private int getIntegerValue(String value) {
        int result = -1;
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                error("Expected numeric parameter", value, true);
            }
        }
        return result;
    }

    public int getHexInteger(int position) {
        return getHexIntegerValue(getParameter(position));
    }

    public int getHexInteger(String option) {
        return getHexIntegerValue(getOption(option));
    }

    private int getHexIntegerValue(String value) {
        int result = -1;
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                if (value.startsWith("$"))
                    value = value.substring(1);
                else if (value.startsWith("0x") || value.startsWith("0X"))
                    value = value.substring(2);
                try {
                    result = Integer.parseInt(value, 16);
                } catch (NumberFormatException ee) {
                    error("Expected decimal or hex parameter", value, true);
                }
            }
        }
        return result;
    }

    /*
     * Print usage summary
     */
    public void usage() {
        System.err.println("Usage:\n");
        if (opts == null) {
            System.err.println("  " + name + " " + parms + "\n");
        } else {
            System.err.println("  " + name + " [options] " + parms + "\n");
            String hdr = "options:";
            for (String opt : opts.keySet()) {
                System.err.print(hdr + " -" + opt);
                hdr = "        ";
                String value = opts.get(opt);
                if (value != null) {
                    if (value.endsWith("-")) {
                        hdr = ",";
                    } else {
                        if (value.endsWith("+")) {
                            value = value.substring(0, value.length()-1);
                            hdr = "";
                        }
                        System.err.print(" " + value);
                    }
                }
                if (hdr.length() > 1) {
                    System.err.println();
                }
            }
        }
        System.exit(1);
    }

    // Error handling
    private void error(String msg, String parm, boolean usage) {
        System.err.println(msg + ": " + parm);
        if (usage) {
            System.err.println();
            usage();
        }
        System.exit(1);
    }
}
