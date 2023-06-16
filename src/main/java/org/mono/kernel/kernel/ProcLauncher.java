package org.mono.kernel.kernel;

import org.mono.kernel.Service;
import org.mono.kernel.ServicesManager;

import java.io.File;

public class ProcLauncher {

    public static int launch(String processName, String className, String jar, String[] args, boolean syncLaunch) {
        File jarFile = null;
        if (jar != null && !jar.equals("_")) {
            jarFile = new File(jar);
        }
        Service service = new Service(processName, "process", "", className, "main", args, jarFile, false, syncLaunch, false);
        int pid = ServicesManager.registerService(service, false);
        return ServicesManager.startSyncService(pid, false);
    }

}
