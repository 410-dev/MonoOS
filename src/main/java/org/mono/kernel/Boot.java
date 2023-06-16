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

        // Start Kernel API
        ScreenOutput.println("Starting Kernel API...");
        Thread kernelAPIThread = new Thread(KernelAPI::startListenAPI);
        kernelAPIThread.start();

        // Load service files from specified directories
        // TODO

        // Load all services in root/sys/services
        // The file name shouldn't start with dot, and the file name should end with .service
        ScreenOutput.println("Loading services...");
        FilenameFilter serviceFilter = (dir, name) -> !name.startsWith(".") && name.endsWith(".service");
        File[] serviceFiles = new File(root.getAbsolutePath() + Environment.KERNEL_SERVICES.replace("/", File.separator)).listFiles(serviceFilter);
        if (serviceFiles != null) {
            for (File serviceFile : serviceFiles) {
                ServicesManager.registerService(serviceFile);
            }
        }

        // Register basic services
        Service shell = new Service("shell", "Internal linux-like shell", "org.mono.userspace.Shell", "main", null, null, true, false);
        Service basicUI = new Service("ui", "Internal fallback UI with direct kernel communication", "org.mono.userspace.BasicShellUI", "main", null, null, true, false);
        Service io_stdout = new Service("io_stdout", "Standard output", "org.mono.kernel.io.ScreenOutput", "main", null, null, true, false);
        Service io_stdin = new Service("io_stdin", "Standard input", "org.mono.kernel.io.KeyboardStdinAdapter", "main", null, null, true, false);
        Service io_stderr = new Service("io_stderr", "Standard error", "org.mono.kernel.io.ScreenOutput", "main", null, null, true, false);
        ServicesManager.registerService(shell);
        ServicesManager.registerService(basicUI);
        ServicesManager.registerService(io_stdout);
        ServicesManager.registerService(io_stdin);
        ServicesManager.registerService(io_stderr);

        // Start all services
        ServicesManager.startServices();

        // Start kernel console
        // Exit codes:
        //   0: Shutdown
        //   1: Reboot
        //   2: Halt
        //   3: Panic
        ScreenOutput.println("Starting kernel...");
        int exitCode = KernelAPI.startListenSystemControls();

        switch (exitCode) {
            case 0:
                // Shutdown
                ScreenOutput.println("Shutting down...");
                stopSequence(kernelAPIThread);
                System.exit(0);
            case 1:
                // Reboot
                ScreenOutput.println("Rebooting...");
                stopSequence(kernelAPIThread);
                main(args);
            case 2:
                // Halt
                ScreenOutput.println("Halting...");
                stopSequence(kernelAPIThread);
                System.exit(0);
            case 3:
                // Panic
                ScreenOutput.println("Panic!");
                stopSequence(kernelAPIThread);
                System.exit(1);

            default:
                // Unknown exit code
                ScreenOutput.println("Unknown exit code: " + exitCode);
                stopSequence(kernelAPIThread);
                System.exit(1);
        }
    }

    private static void stopSequence(Thread kernelAPIThread) {
        ServicesManager.stopServices();
        ScreenOutput.println("Stopping kernel API...");
        kernelAPIThread.interrupt();
    }
}
