package org.mono.userspace;

import org.mono.kernel.io.ScreenOutput;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                    String[] commandParts = commandSplit(command);
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
                    // TODO: Use kernel api to launch process
                    } else {
                        try {
                            String className = commandParts[0].substring(0, 1).toUpperCase() + commandParts[0].substring(1).toLowerCase();
                            Class<?> processClass = Class.forName("org.mono.userspace.busybox." + className);
                            Object process = processClass.getDeclaredConstructor().newInstance();
                            processExitCode = (Integer) processClass.getMethod("main", String[].class).invoke(process, (Object) Arrays.copyOfRange(commandParts, 1, commandParts.length));
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

    private String[] commandSplit(String command) {
        // Command splitting. Treats double quotes or single quotes as a single argument.
        // Example: "echo \"Hello World\"" -> ["echo", "Hello World"]
        // Example: "echo 'Hello World'" -> ["echo", "Hello World"]
        // Example: "echo Hello World" -> ["echo", "Hello", "World"]

        // Regular expression to match quoted or non-quoted parts of the command
        String regex = "\"([^\"]*)\"|'([^']*)'|\\S+";
        List<String> arguments = new ArrayList<>();

        // Use regex pattern to split the command into arguments
        Matcher matcher = Pattern.compile(regex).matcher(command);
        while (matcher.find()) {
            // Add the matched group to the arguments list
            String argument = matcher.group();
            // Remove the surrounding quotes if present
            if (argument.startsWith("\"") && argument.endsWith("\"") ||
                    argument.startsWith("'") && argument.endsWith("'")) {
                argument = argument.substring(1, argument.length() - 1);
            }
            arguments.add(argument);
        }

        // Convert the list to an array and return
        return arguments.toArray(new String[0]);
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

    public static String kernelAPI(String command) {
        try (Socket ksock = new Socket("localhost", 65531)) {
            // Send kernel to stop or restart
            ksock.getOutputStream().write(command.getBytes());
            ksock.getOutputStream().flush();
            byte[] kernbuffer = new byte[1024];
            int kernresp = ksock.getInputStream().read(kernbuffer);
            if (kernresp > 0) {
                return new String(kernbuffer, 0, kernresp);
            }
        } catch (Exception e) {
            println("Failed!");
            println("Error: " + e.getMessage());
        }
        return null;
    }

    private static Class<?> printerClass = null;
    public static void println(String s) {
        if (printerClass == null) {
            try {
                printerClass = Class.forName(kernelAPI("IO:1"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            printerClass.getDeclaredMethod("println", String.class).invoke(null, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void print(String s) {
        if (printerClass == null) {
            try {
                printerClass = Class.forName(kernelAPI("IO:1"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            printerClass.getDeclaredMethod("print", String.class).invoke(null, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
