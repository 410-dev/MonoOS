package org.mono.kernel;

import me.hysong.libhyextended.utils.ObjectIO;
import org.mono.kernel.io.ScreenOutput;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class Boot {

    // Boot arguments:
    // --nvram=<path to NVRAM file>
    // --root=<path to root directory>
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        // Load NVRAM
        // If the NVRAM file does not exist, create a new one in memory
        if (NVRAM.getParameter(args, "--nvram") == null) {
            NVRAM nvram = new NVRAM("00000000");
            NVRAM.setSystemNVRAM(nvram);
        } else {
            NVRAM.setSystemNVRAM((NVRAM) ObjectIO.read(new File(Objects.requireNonNull(NVRAM.getParameter(args, "--nvram")))));
        }

        // Set Bootloader info to NVRAM
        NVRAM.set("BOOTLOADER_NAME", "Built-in STG2 bootloader");
        NVRAM.set("BOOTLOADER_VERS", "0.0.1");
        NVRAM.set("BOOTLOADER_AUTH", "410-dev");

        // Push boot arguments to nvram
        for (String arg : args) {
            NVRAM.set(arg);
        }

        // Add session UID
        NVRAM.set("sys_session", String.valueOf(System.currentTimeMillis()));
        NVRAM.set("--verbose");

        // Load root directory
        if (NVRAM.getParameter(args, "--root") == null) {
            ScreenOutput.println("WARNING: Root is not specified. Current location will be the root.");
        }
        File root = new File(NVRAM.getParameter(args, "--root") == null ? "." : Objects.requireNonNull(NVRAM.getParameter(args, "--root")));  // If root is not specified, use current directory as root.
        if (!root.isDirectory()) {
            throw new RuntimeException("Root directory is not a directory!");
        }
        NVRAM.set("root", root.getAbsolutePath());

        Kernel.init(args, root);
    }

}
