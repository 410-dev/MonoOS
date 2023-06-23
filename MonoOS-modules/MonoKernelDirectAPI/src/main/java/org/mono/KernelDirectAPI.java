package org.mono;

import org.mono.kernel.Service;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KernelDirectAPI {
    public static int registerService(File serviceMetaFile, boolean verbose) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method realMethod = Class.forName("org.mono.kernel.ServicesManager").getMethod("registerService", File.class, boolean.class);
        return (int) realMethod.invoke(null, serviceMetaFile, verbose);
    }

    public static Service getServiceByName(String name) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method realMethod = Class.forName("org.mono.kernel.ServicesManager").getMethod("getServiceByName", String.class);
        return (Service) realMethod.invoke(null, name);
    }

    public static Service getServiceByType(String typeID) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method realMethod = Class.forName("org.mono.kernel.ServicesManager").getMethod("getServiceByType", String.class);
        return (Service) realMethod.invoke(null, typeID);
    }

    public static Service getServiceByPID(int pid) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method realMethod = Class.forName("org.mono.kernel.ServicesManager").getMethod("getServiceByPID", int.class);
        return (Service) realMethod.invoke(null, pid);
    }
}
