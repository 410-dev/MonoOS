package org.mono.kernel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.hysong.libhyextended.utils.StackTraceStringifier;
import org.mono.kernel.io.ScreenOutput;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

@Getter(AccessLevel.PROTECTED)
public class Service implements Serializable {

    private int pid;
    private final String name;
    private final String description;
    @Getter private final String className;
    private final String methodName;
    private final String[] arguments;
    private final File serviceFile;
    private final boolean isKernelService;
    private final boolean enforceJoin;
    private String session;
    private Thread thread;
    private String stackTrace;

    public Service(String name, String description, String className, String methodName, String[] arguments, File serviceFile, boolean isKernelService, boolean enforceJoin) {
        this.name = name;
        this.description = description;
        this.className = className;
        this.methodName = methodName == null ? "main" : methodName;
        this.arguments = arguments == null ? new String[]{} : arguments;
        this.serviceFile = serviceFile;
        this.isKernelService = isKernelService;
        this.enforceJoin = enforceJoin;
        this.session = NVRAM.get("sys_session");
    }

    public void setPID(int pid) {
        // If this method is not called by ServicesManager, it will not be called at all.
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String classFullName = e.getClass().getPackageName() + "." + e.getClassName();
        if (classFullName.equals("org.mono.kernel.ServicesManager")) {
            this.pid = pid;
        }
    }

    public boolean isRunning() {
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
                    e.printStackTrace();
                }
            });
            thread.start();

            if (enforceJoin) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stackTrace = StackTraceStringifier.stringify(e);
            return false;
        }
        return true;
    }

    public boolean startJoin() {
        boolean success = start();
        if (!success) return false;
        try {
            thread.join();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            stackTrace = StackTraceStringifier.stringify(e);
            return false;
        }
    }
}
