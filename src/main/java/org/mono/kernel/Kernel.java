package org.mono.kernel;

import org.mono.kernel.io.ScreenOutput;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class Kernel {
    public static void init(String[] args, File root) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        boolean verbose = NVRAM.get("--verbose") != null;

        // Register basic services
        Service kernel = new Service("Mono Basis Framework", "dummy-kern", "Kernel that drives MonoOS", "", "", null, null, true, true, true);
        Service nvram = new Service("On-FS NVRAM", "dummy-nvram", "NVRAM manager", "", "", null, null, true, true, true);
        Service init = new Service("ServicesManager", "dummy-init", "Process launching utility", "", "", null, null, true, true, true);
        Service framework = new Service("ServicesManager", "dummy-framework", "Process management utility", "", "", null, null, true, true, true);
        Service stdout = new Service("BasicIO_StandardOut", "stdout","Standard output", "org.mono.kernel.io.ScreenOutput", "main", null, null, true, false, false);
        Service stderr = new Service("BasicIO_StandardErr", "stderr","Standard error", "org.mono.kernel.io.ScreenOutput", "main", null, null, true, false, false);
        Service stdin = new Service("BasicIO_StandardIn", "stdin","Standard input", "org.mono.kernel.io.KeyboardStdinAdapter", "main", null, null, true, false, false);
        Service environment = new Service("Environment", "dummy-env", "Environment variables and designated paths", "", "", null, null, true, true, true);
        Service shell = new Service("Shell", "shell", "Internal linux-like shell", "org.mono.userspace.Shell", "main", null, null, true, false, true);
        Service basicUI = new Service("BasicShellUI", "ui","Internal fallback UI with direct kernel communication", "org.mono.userspace.BasicShellUI", "main", null, null, true, false, true);
        ServicesManager.registerService(kernel, verbose);
        ServicesManager.registerService(nvram, verbose);
        ServicesManager.registerService(init, verbose);
        ServicesManager.registerService(framework, verbose);
        ServicesManager.registerService(stdout, verbose);
        ServicesManager.registerService(stderr, verbose);
        ServicesManager.registerService(stdin, verbose);
        ServicesManager.registerService(environment, verbose);


        // Load drivers if exists
        ScreenOutput.println("Loading hardware extensions...");
        FilenameFilter extFilters = (dir, name) -> !name.startsWith(".") && name.endsWith(".ext");
        File[] extensionFiles = new File(root.getAbsolutePath() + Environment.KERNEL_EXTENSIONS.replace("/", File.separator)).listFiles(extFilters);
        if (extensionFiles != null) {
            for (File serviceFile : extensionFiles) {
                ServicesManager.registerService(serviceFile, verbose);
            }
        }else{
            ScreenOutput.println("No hardware extensions found.");
        }

        // Cache output driver
        Method out = ServicesManager.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class);

        // Load all services in root/sys/services
        // The file name shouldn't start with dot, and the file name should end with .service
        ScreenOutput.println("Loading services...");
        FilenameFilter serviceFilter = (dir, name) -> !name.startsWith(".") && name.endsWith(".service");
        File[] serviceFiles = new File(root.getAbsolutePath() + Environment.KERNEL_SERVICES.replace("/", File.separator)).listFiles(serviceFilter);
        if (serviceFiles != null) {
            for (File serviceFile : serviceFiles) {
                ServicesManager.registerService(serviceFile, verbose);
            }
        }else{
            ScreenOutput.println("No services found.");
        }

        // Start all services
        ServicesManager.startServices(verbose);

        // Start UI services
        ServicesManager.startAsyncService(ServicesManager.registerService(shell, verbose), verbose);
        ServicesManager.startAsyncService(ServicesManager.registerService(basicUI, verbose), verbose);

        // Start kernel console
        // Exit codes:
        //   0: Shutdown
        //   1: Reboot
        //   2: Halt
        //   3: Panic
        out.invoke(null,"Starting kernel controls...");
        int exitCode = KernelAPI.startListenSystemControls();

        switch (exitCode) {
            case 0:
                // Shutdown
                out.invoke(null,"Shutting down...");
                stopSequence();
                System.exit(0);
            case 1:
                // Reboot
                out.invoke(null,"Rebooting...");
                stopSequence();
                Boot.main(args);
            case 2:
                // Halt
                out.invoke(null,"Halting...");
                stopSequence();
                System.exit(0);
            case 3:
                // Panic
                out.invoke(null,"Panic!");
                stopSequence();
                System.exit(1);

            default:
                // Unknown exit code
                out.invoke(null,"Unknown exit code: " + exitCode);
                stopSequence();
                System.exit(1);
        }
    }


    private static void stopSequence() {
        ServicesManager.stopServices(NVRAM.get("--verbose") != null);
    }
}
