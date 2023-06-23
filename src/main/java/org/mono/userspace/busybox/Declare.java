package org.mono.userspace.busybox;

import org.mono.KernelDirectAPI;
import org.mono.userspace.Shell;

import java.util.HashMap;

public class Declare {
    public Integer main(String[] args) {
        try {
            String name = null;
            String value = null;
            Class<?> environment = Class.forName("org.mono.kernel.Environment");

            if (args.length == 0) {
                Shell.println("Usage: declare <name> <value?>");
                HashMap<String, String> varList = (HashMap<String, String>) environment.getDeclaredField("variables").get(null);
                Shell.println("Variables:");
                for (String varName : varList.keySet()) {
                    Shell.println("  " + varName + " = " + varList.get(varName));
                }
                return 1;
            }

            // Parse arguments
            name = args[0];
            if (args.length >= 2) value = args[1];

            // Set variable
            environment.getDeclaredMethod("set", String.class, String.class).invoke(null, name, value);

            if (value == null) Shell.println("Unset variable '" + name + "'");
            else Shell.println("Set variable '" + name + "' to '" + value + "'");

        }catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
}
