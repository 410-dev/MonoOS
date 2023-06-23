package org.mono.userspace;

import org.mono.KernelDirectAPI;

import java.io.*;

public class InitScriptRunner {
    public Integer main(String[] args) {
        // From Environment class, get the environment variable "INIT_SCRIPT"
        String initscriptListLocation = "sys/system/initscripts.list";

        // Read the file in initscriptListLocation
        try {
            Class<?> NVRAM = Class.forName("org.mono.kernel.NVRAM");
            String scriptDir = NVRAM.getDeclaredMethod("get", String.class).invoke(null, "root").toString();
            scriptDir += "/sys/system/init/";

            // Filter .sh files only
            FilenameFilter filter = (dir, name) -> name.endsWith(".sh");
            File[] files = new File(scriptDir).listFiles(filter);

            if (files == null) {
                KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, "No available init scripts found: " + scriptDir + " is empty.");
                return 0;
            }

            // Run the scripts in the alphabetical order, using bash
            // Do print the output of the script
            for (File file : files) {
                KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, "Running init script: " + file.getName());
                String[] command = new String[]{"bash", file.getAbsolutePath()};
                Process process = Runtime.getRuntime().exec(command);

                // Read and print the output of the script
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, line);
                }

                // Wait for the process to complete
                try {
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, "Script execution completed successfully.");
                    } else {
                        KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, "Script execution failed with exit code: " + exitCode);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                reader.close();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        // Then run the scripts in the alphabetical order
        return 0;
    }
}
