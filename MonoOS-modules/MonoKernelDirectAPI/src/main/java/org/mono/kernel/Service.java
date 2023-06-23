package org.mono.kernel;

import lombok.AccessLevel;
import lombok.Getter;
import me.hysong.libhyextended.utils.StackTraceStringifier;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@Getter(AccessLevel.PROTECTED)
public class Service implements Serializable {

    private int pid;
    @Getter private final String name;
    private final String type;
    @Getter private final String description;
    @Getter private final String className;
    private final String methodName;
    @Getter private final String[] arguments;
    @Getter private final File serviceFile;
    private final boolean isKernelService;
    private       boolean enforceSync;
    private final boolean requireAlive;
    private Thread thread;
    private String stackTrace;

    public Service(String name, String type, String description, String className, String methodName, String[] arguments, File serviceFile, boolean isKernelService, boolean enforceSync, boolean requireAlive) {
        this.name = name;
        this.type = type == null ? "service" : type;
        this.description = description;
        this.className = className;
        this.methodName = methodName == null ? "main" : methodName;
        this.arguments = arguments == null ? new String[]{} : arguments;
        this.serviceFile = serviceFile;
        this.isKernelService = isKernelService;
        this.enforceSync = enforceSync;
        this.requireAlive = requireAlive;
    }

    public void setPID(int pid) {
        // If this method is not called by ServicesManager, it will not be called at all.
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String classFullName = e.getClass().getPackageName() + "." + e.getClassName();
        if (classFullName.equals("java.lang.org.mono.kernel.ServicesManager")) {
            this.pid = pid;
        }
    }

    public boolean isRunning() {
        if (type.startsWith("dummy")) return false;

        if (thread == null) {
            return false;
        }
        return thread.isAlive();
    }

    public void kill() {
        if (isRunning()) {
            thread.interrupt();
        }
    }

    public Object loadObject() {
        if (type.startsWith("dummy")) return null;
        try {
            Class<?> loadedClass = null;
            if (serviceFile != null) {
                String jarFilePath = serviceFile.getAbsolutePath();
                URL jarUrl = new URL("file://" + jarFilePath);
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});
                loadedClass = classLoader.loadClass(className);
                classLoader.close();
            }else{
                loadedClass = Class.forName(className);
            }
            Class<?> finalLoadedClass = loadedClass;
            return finalLoadedClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean start() {

        if (type.startsWith("dummy")) return true;

        // Kill process first
        kill();

        try {
            Object instance = loadObject();
            Method method = instance.getClass().getMethod(methodName, String[].class);
            thread = new Thread(() -> {
                try {
                    Object result = method.invoke(instance, (Object) arguments);
                    if (result instanceof String) {
                        stackTrace = (String) result;
                    }
                } catch (Exception e) {
                    stackTrace += StackTraceStringifier.stringify(e);
                    e.printStackTrace();
                }
            });

            if (enforceSync) {
                thread.run();
            } else {
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stackTrace += StackTraceStringifier.stringify(e);
            return false;
        }
        return true;
    }

    public boolean startSync() {

        if (type.startsWith("dummy")) return true;

        boolean originalEnforceSync = enforceSync;
        enforceSync = true;
        boolean success = start();
        enforceSync = originalEnforceSync;
        return success;
    }

    public void suspend() throws InterruptedException {
        if (isRunning()) {
            thread.wait();
        }
    }

    public void suspend(long millis) throws InterruptedException {
        if (isRunning()) {
            thread.wait(millis);
        }
    }

    public void resume() {
        if (isRunning()) {
            thread.notify();
        }
    }
}
