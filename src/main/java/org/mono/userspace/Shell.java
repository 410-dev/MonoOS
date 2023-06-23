package org.mono.userspace;

import me.hysong.libhyextended.Utils;
import org.mono.kernel.ServicesManager;
import org.mono.kernel.kernel.ProcLauncher;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Shell {

    public Shell(){}

    // Start listening for commands in port 65532 via socket. Usage is exactly same as linux shells.
    public int main(String[] args) {
        int exitCode = 2;
        try {
            ServerSocket serverSocket = new ServerSocket(65532);
            Socket socket = serverSocket.accept();
            while (true) {
                byte[] buffer = new byte[1024];
                int read = socket.getInputStream().read(buffer);
                if (read > 0) {
                    String command = new String(buffer, 0, read);

                    String[] commandParts = Utils.splitStringBySpaceWithQuotationConsideration(command);
                    int processExitCode = 1;

                    // Send kernel to stop or restart
                    if (commandParts[0].equals("stop") || commandParts[0].equals("restart")) {
                        if (machineCtl(command)) {
                            socket.getOutputStream().write("0".getBytes());
                            socket.close();
                            serverSocket.close();
                            break;
                        }

                    // Find command in userspace.busybox package and execute it
                    // TODO: If not found, try finding from disk drive (.jar file) and execute it
                    } else {
                        try {
                            String className = commandParts[0].substring(0, 1).toUpperCase() + commandParts[0].substring(1).toLowerCase();
                            Class<?> processClass = Class.forName("org.mono.userspace.busybox." + className);

                            processExitCode = ProcLauncher.launch(commandParts[0], processClass.getName(), "_", Arrays.copyOfRange(commandParts, 1, commandParts.length), true);

                        } catch (ClassNotFoundException e) {
                            println("Unknown command: " + commandParts[0]);
                        }
                    }
                    socket.getOutputStream().write(String.valueOf(processExitCode).getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitCode;
    }


    private boolean machineCtl(String command) {
        try (Socket ksock = new Socket("localhost", 65530)) {
            // Send kernel to stop or restart
            ksock.getOutputStream().write(command.getBytes());
            ksock.getOutputStream().flush();
            byte[] kernbuffer = new byte[1024];
            int kernresp = ksock.getInputStream().read(kernbuffer);
            if (kernresp > 0) {
                String response = new String(kernbuffer, 0, kernresp);
                if (response.startsWith("ok")) {
                    ksock.close();
                    return true;
                }else{
                    println("Kernel response: " + response);
                }
            }
        } catch (Exception e) {
            println("Failed!");
            println("Error: " + e.getMessage());
        }
        return false;
    }

    private static Object printerObject = null;
    private static Object inputObject = null;
    public static void println(String s) {
        if (printerObject == null) {
            printerObject = ServicesManager.getServiceByType("stdout").loadObject();
        }
        try {
            printerObject.getClass().getDeclaredMethod("println", String.class).invoke(null, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void print(String s) {
        if (printerObject == null) {
            printerObject = ServicesManager.getServiceByType("stdout").loadObject();
        }
        try {
            printerObject.getClass().getDeclaredMethod("print", String.class).invoke(null, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readLine(String prompt) {
        if (inputObject == null) {
            inputObject = ServicesManager.getServiceByType("stdin").loadObject();
        }
        try {
            return (String) inputObject.getClass().getDeclaredMethod("readLine", String.class).invoke(null, prompt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String substituteEnvVar(String toSubstitute) {
        try {
            Class<?> environment = Class.forName("org.mono.kernel.Environment");
            return (String) environment.getDeclaredMethod("substituteInLineVariable", String.class).invoke(null, toSubstitute);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
