package org.mono.kernel;

import me.hysong.libhyextended.utils.ObjectIO;
import org.mono.kernel.io.ScreenOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ServicesManager {

    protected static ArrayList<Service> services = new ArrayList<>();
    private static int pidCounter = 0;

    public static int registerService(File serviceMetaFile) throws IOException, ClassNotFoundException {
        if (serviceMetaFile.isFile()) {
            ScreenOutput.println("Registering service " + serviceMetaFile.getName() + "...");
            Service service = (Service) ObjectIO.read(serviceMetaFile);
            pidCounter++;
            service.setPID(pidCounter);
            services.add(service);
            return pidCounter;
        }else{
            throw new RuntimeException("Service meta file is not a file!");
        }
    }

    public static int registerService(Service service){
        ScreenOutput.println("Registering service " + service.getName() + "...");
        pidCounter++;
        service.setPID(pidCounter);
        services.add(service);
        return pidCounter;
    }

    protected static void stopServices() {
        for (Service service : services) {
            ScreenOutput.println("Stopping service " + service.getName() + "...");
            service.kill();
        }
    }

    protected static void startServices() {
        for (Service service : services) {
            ScreenOutput.println("Starting service " + service.getName() + "...");
            if (service.start()) {
                ScreenOutput.println("Service " + service.getName() + " started successfully.");
            } else {
                ScreenOutput.println("Service " + service.getName() + " could not be started.");
            }
        }
    }

    public static Service getServiceByName(String name){
        for (Service service : services) {
            if(service.getName().equals(name)){
                return service;
            }
        }
        return null;
    }

    public static Service getServiceByPID(int pid) {
        for (Service service : services) {
            if (service.getPid() == pid) {
                return service;
            }
        }
        return null;
    }

    public static boolean unloadService(String name){
        return unloadService(getServiceByName(name));
    }

    public static boolean unloadService(int pid){
        return unloadService(getServiceByPID(pid));
    }

    private static boolean unloadService(Service s) {
        if(s != null){
            ScreenOutput.println("Unloading service " + s.getName() + "...");
            if (s.isRunning()) {
                ScreenOutput.println("Failed to unload service " + s.getName() + ": service is still running.");
                return false;
            }
            services.remove(s);
            return true;
        }
        ScreenOutput.println("Failed to unload service: service not found.");
        return false;
    }

    public static void killService(String name){
        Service s = getServiceByName(name);
        if(s != null){
            ScreenOutput.println("Killing service " + s.getName() + "...");
            s.kill();
        }
    }

    public static void killService(int pid){
        Service s = getServiceByPID(pid);
        if(s != null){
            ScreenOutput.println("Killing service " + s.getName() + "...");
            s.kill();
        }
    }

    public static void startAsyncService(String name){
        Service s = getServiceByName(name);
        if(s != null){
            ScreenOutput.println("Starting service " + s.getName() + "...");
            if (s.start()) {
                ScreenOutput.println("Service " + s.getName() + " started successfully.");
            } else {
                ScreenOutput.println("Service " + s.getName() + " could not be started.");
            }
        }
    }

    public static void startAsyncService(int pid){
        Service s = getServiceByPID(pid);
        if(s != null){
            ScreenOutput.println("Starting service " + s.getName() + "...");
            if (s.start()) {
                ScreenOutput.println("Service " + s.getName() + " started successfully.");
            } else {
                ScreenOutput.println("Service " + s.getName() + " could not be started.");
            }
        }
    }

    public static void startSyncService(String name){
        Service s = getServiceByName(name);
        if(s != null){
            ScreenOutput.println("Starting service " + s.getName() + "...");
            if (s.startJoin()) {
                ScreenOutput.println("Service task of " + s.getName() + " ended successfully.");
            } else {
                ScreenOutput.println("Service task of " + s.getName() + " ended with an error.");
            }
        }
    }

    public static void startSyncService(int pid){
        Service s = getServiceByPID(pid);
        if(s != null){
            ScreenOutput.println("Starting service " + s.getName() + "...");
            if (s.startJoin()) {
                ScreenOutput.println("Service task of " + s.getName() + " ended successfully.");
            } else {
                ScreenOutput.println("Service task of " + s.getName() + " ended with an error.");
            }
        }
    }
}
