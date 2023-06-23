package org.mono.kernel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hysong.libhyextended.Utils;
import org.mono.kernel.io.ScreenOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


// TODO Handle unexpected service termination
public class ServicesManager {

    protected static ArrayList<Service> services = new ArrayList<>();
    private static int pidCounter = 0;

    public static int registerService(File serviceMetaFile, boolean verbose) {
        if (verbose) ScreenOutput.println("[INFO] Registering service " + serviceMetaFile.getName() + "...");
        if (serviceMetaFile.isFile()) {
            Service service = null;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(serviceMetaFile));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                JsonObject serviceMeta = JsonParser.parseString(builder.toString()).getAsJsonObject();
                service = new Service(
                        serviceMeta.has("name") ? serviceMeta.get("name").getAsString() : "generic-service",
                        serviceMeta.has("type") ? serviceMeta.get("type").getAsString() : "service",
                        serviceMeta.has("description") ? serviceMeta.get("description").getAsString() : "Generic service",
                        serviceMeta.get("className").getAsString(), // This field is the only required field for json data.
                        serviceMeta.has("methodName") ? serviceMeta.get("methodName").getAsString() : "main",
                        Utils.splitStringBySpaceWithQuotationConsideration(serviceMeta.get("arguments").getAsString()),
                        serviceMeta.has("file") ? new File(serviceMetaFile.getParent(), serviceMeta.get("file").getAsString().replace("/", File.separator)) : new File(serviceMetaFile.getParent(), serviceMetaFile.getName().substring(0, serviceMetaFile.getName().lastIndexOf(".")) + ".jar"),
                        serviceMeta.has("isKernelService") && serviceMeta.get("isKernelService").getAsBoolean(),
                        serviceMeta.has("enforceSync") && serviceMeta.get("enforceSync").getAsBoolean(),
                        serviceMeta.has("requireAlive") && serviceMeta.get("requireAlive").getAsBoolean()
                    );

                pidCounter++;
                service.setPID(pidCounter);

                // If kernel service, unload the old one if the same pid exists
                if (service.isKernelService()) {
                    Service oldService = getServiceByType(service.getType());
                    if (oldService != null) {
                        if (verbose) ScreenOutput.println("[WARN] Unloading old kernel service " + oldService.getName() + "...");
                        unloadService(oldService, verbose);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }


            services.add(service);
            if (verbose) ScreenOutput.println("[INFO] Service " + service.getName() + " (" + service.getClassName() + ") registered successfully.");
            return pidCounter;
        }else{
            throw new RuntimeException("[ERR]  Service meta file is not a file!");
        }
    }

    public static int registerService(Service service, boolean verbose){
        if (verbose) ScreenOutput.println("[INFO] Registering service " + service.getName() + "...");
        pidCounter+=1;
        service.setPID(pidCounter);
        services.add(service);
        return pidCounter;
    }

    protected static void stopServices(boolean verbose) {
        for (int i = services.size() - 1; i >= 0; i--) {
            Service service = services.get(i);
            if (verbose) ScreenOutput.println("[INFO] Stopping service " + service.getName() + "...");
            service.kill();
        }
    }

    protected static void startServices(boolean verbose) {
        for (Service service : services) {
            if (verbose) ScreenOutput.println("[INFO] Starting service " + service.getName() + "...");
            if (service.start()) {
                if (verbose) ScreenOutput.println("[INFO] Service " + service.getName() + " started successfully.");
            } else {
                if (verbose) ScreenOutput.println("[ERR]  Service " + service.getName() + " could not be started.");
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

    public static Service getServiceByType(String typeID) {
        for (Service service : services) {
            if (service.getType().equals(typeID)) {
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

    public static boolean unloadServiceByName(String name, boolean verbose){
        return unloadService(getServiceByName(name), verbose);
    }

    public static boolean unloadServiceByType(String type, boolean verbose){
        return unloadService(getServiceByType(type), verbose);
    }

    public static boolean unloadServiceByPID(int pid, boolean verbose){
        return unloadService(getServiceByPID(pid), verbose);
    }

    private static boolean unloadService(Service s, boolean verbose) {
        if(s != null){
            if (s.isRunning()) {
                if (verbose) ScreenOutput.println("[ERR]  Failed to unload service " + s.getName() + ": service is still running.");
                return false;
            }
            services.remove(s);
            return true;
        }
        if (verbose) ScreenOutput.println("[ERR]  Failed to unload service: service not found.");
        return false;
    }

    public static void killService(String name, boolean verbose){
        Service s = getServiceByName(name);
        if(s != null){
            if (verbose) ScreenOutput.println("[INFO] Killing service " + s.getName() + "...");
            s.kill();
        }else {
            if (verbose) ScreenOutput.println("[ERR]  Service not found.");
        }
    }

    public static void killService(int pid, boolean verbose){
        Service s = getServiceByPID(pid);
        if(s != null){
            if (verbose) ScreenOutput.println("[INFO] Killing service " + s.getName() + "...");
            s.kill();
        }else {
            if (verbose) ScreenOutput.println("[ERR]  Service not found.");
        }
    }

    public static void startAsyncService(String name, boolean verbose){
        Service s = getServiceByName(name);
        if(s != null){
            if (verbose) ScreenOutput.println("[INFO] Starting service " + s.getName() + "...");
            if (s.start()) {
                if (verbose) ScreenOutput.println("[INFO] Service " + s.getName() + " started successfully.");
            } else {
                if (verbose) ScreenOutput.println("[ERR]  Service " + s.getName() + " could not be started.");
            }
        }else {
            if (verbose) ScreenOutput.println("[ERR]  Service not found.");
        }
    }

    public static void startAsyncService(int pid, boolean verbose){
        Service s = getServiceByPID(pid);
        if(s != null){
            if (verbose) ScreenOutput.println("[INFO] Starting service " + s.getName() + "...");
            if (s.start()) {
                if (verbose) ScreenOutput.println("[INFO] Service " + s.getName() + " started successfully.");
            } else {
                if (verbose) ScreenOutput.println("[ERR]  Service " + s.getName() + " could not be started.");
            }
        }else {
            if (verbose) ScreenOutput.println("[ERR]  Service not found.");
        }
    }

    public static void startSyncService(String name, boolean verbose){
        Service s = getServiceByName(name);
        if(s != null){
            if (verbose) ScreenOutput.println("[INFO] Starting service " + s.getName() + "...");
            if (s.startSync()) {
                if (verbose) ScreenOutput.println("[INFO] Service task of " + s.getName() + " ended successfully.");
            } else {
                if (verbose) ScreenOutput.println("[ERR]  Service task of " + s.getName() + " ended with an error.");
            }
        }else {
            if (verbose) ScreenOutput.println("[ERR]  Service not found.");
        }
    }

    public static int startSyncService(int pid, boolean verbose){
        Service s = getServiceByPID(pid);
        if(s != null){
            if (verbose) ScreenOutput.println("[INFO] Starting service " + s.getName() + "...");
            if (s.startSync()) {
                if (verbose) ScreenOutput.println("[INFO] Service task of " + s.getName() + " ended successfully.");
                return 0;
            } else {
                if (verbose) ScreenOutput.println("[ERR]  Service task of " + s.getName() + " ended with an error.");
                return 1;
            }
        } else {
            if (verbose) ScreenOutput.println("[ERR]  Service not found.");
        }
        return 2;
    }
}
