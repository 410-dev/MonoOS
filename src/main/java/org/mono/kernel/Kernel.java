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

        // Start Kernel API
        ScreenOutput.println("Starting Kernel API...");
        Thread kernelAPIThread = new Thread(KernelAPI::startListenAPI);
        kernelAPIThread.start();

        // Register basic services
        Service shell = new Service("Basic Shell", "shell", "Internal linux-like shell", "org.mono.userspace.Shell", "main", null, null, true, false, true);
        Service basicUI = new Service("BasicShellUI", "ui","Internal fallback UI with direct kernel communication", "org.mono.userspace.BasicShellUI", "main", null, null, true, false, true);
        Service io_stdout = new Service("BasicIO_StandardOut", "io_stdout","Standard output", "org.mono.kernel.io.ScreenOutput", "main", null, null, true, false, false);
        Service io_stdin = new Service("BasicIO_StandardIn", "io_stdin","Standard input", "org.mono.kernel.io.KeyboardStdinAdapter", "main", null, null, true, false, false);
        Service io_stderr = new Service("BasicIO_StandardErr", "io_stderr","Standard error", "org.mono.kernel.io.ScreenOutput", "main", null, null, true, false, false);
        ServicesManager.registerService(shell, verbose);
        ServicesManager.registerService(basicUI, verbose);
        ServicesManager.registerService(io_stdout, verbose);
        ServicesManager.registerService(io_stdin, verbose);
        ServicesManager.registerService(io_stderr, verbose);

        // Load all services in root/sys/services
        // The file name shouldn't start with dot, and the file name should end with .service
        ScreenOutput.println("Loading services...");
        FilenameFilter serviceFilter = (dir, name) -> !name.startsWith(".") && name.endsWith(".service");
        File[] serviceFiles = new File(root.getAbsolutePath() + Environment.KERNEL_SERVICES.replace("/", File.separator)).listFiles(serviceFilter);
        if (serviceFiles != null) {
            for (File serviceFile : serviceFiles) {
                ServicesManager.registerService(serviceFile, verbose);
            }
        }

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
        Method out = ServicesManager.getServiceByType("io_stdout").loadObject().getClass().getDeclaredMethod("println", String.class);

        // Start all services
        ServicesManager.startServices(verbose);

        // Start kernel console
        // Exit codes:
        //   0: Shutdown
        //   1: Reboot
        //   2: Halt
        //   3: Panic
        out.invoke(null,"Starting kernel...");
        int exitCode = KernelAPI.startListenSystemControls();

        switch (exitCode) {
            case 0:
                // Shutdown
                out.invoke(null,"Shutting down...");
                stopSequence(kernelAPIThread);
                System.exit(0);
            case 1:
                // Reboot
                out.invoke(null,"Rebooting...");
                stopSequence(kernelAPIThread);
                Boot.main(args);
            case 2:
                // Halt
                out.invoke(null,"Halting...");
                stopSequence(kernelAPIThread);
                System.exit(0);
            case 3:
                // Panic
                out.invoke(null,"Panic!");
                stopSequence(kernelAPIThread);
                System.exit(1);

            default:
                // Unknown exit code
                out.invoke(null,"Unknown exit code: " + exitCode);
                stopSequence(kernelAPIThread);
                System.exit(1);
        }
    }


    private static void stopSequence(Thread kernelAPIThread) {
        ServicesManager.stopServices(NVRAM.get("--verbose") != null);
        ScreenOutput.println("Stopping kernel API...");
        try (Socket ksock = new Socket("localhost", 65531)) {
            // Send kernel to stop or restart
            ksock.getOutputStream().write("api-close".getBytes());
            ksock.getOutputStream().flush();
            byte[] kernbuffer = new byte[1024];
            int kernresp = ksock.getInputStream().read(kernbuffer);
            if (kernresp > 0) {
                String output = new String(kernbuffer, 0, kernresp);
                if (output.equals("ok")) {
                    ScreenOutput.println("Kernel API successfully closed.");
                    kernelAPIThread.interrupt();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(9);
        }
    }
}
