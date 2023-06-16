package org.mono.kernel;

import org.mono.kernel.io.ScreenOutput;
import org.mono.kernel.kernel.KernelInfo;

import java.net.ServerSocket;
import java.net.Socket;

public class KernelAPI {

    // Start listening for commands in port 65530 via socket. Command format:
    // <process info json string> <command json string>
    public static int startListenSystemControls() {
        int exitCode = 2;
        try {
            ServerSocket serverSocket = new ServerSocket(65530);
            Socket socket = serverSocket.accept();
            while (true) {
                byte[] buffer = new byte[1024];
                int read = socket.getInputStream().read(buffer);
                if (read > 0) {
                    String command = new String(buffer, 0, read);
                    if (command.equals("stop")) {
                        exitCode = 0;
                        socket.getOutputStream().write("ok".getBytes());
                        break;
                    } else if (command.equals("restart")) {
                        exitCode = 1;
                        socket.getOutputStream().write("ok".getBytes());
                        break;
                    } else {
                        ScreenOutput.println("Unknown kernel control command: " + command);
                    }
                }
            }
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exitCode;
    }

    // Start listening for commands in port 65531 via socket.
    public static void startListenAPI() {
        try {
            ServerSocket serverSocket = new ServerSocket(65531);
            while (true) {
                Socket socket = serverSocket.accept();
                byte[] buffer = new byte[1024];
                int read = socket.getInputStream().read(buffer);
                if (read > 0) {

                    String command = new String(buffer, 0, read);
                    String[] commandPart = Utils.splitStringBySpaceWithQuotationConsideration(command);
                    if (command.equals("info")) {
                        socket.getOutputStream().write(KernelInfo.main().getBytes());
                    } else if (command.equals("api-close")) {
                        socket.getOutputStream().write("ok".getBytes());
                        socket.close();
                        serverSocket.close();
                        break;

                    } else if (commandPart[0].equals("launch")) {
                        // Launch:
                        //  launch name className jarPath synclaunch args...

                        String name = commandPart[1];
                        String className = commandPart[2];
                        String jar = commandPart[3].equals("_") ? null : commandPart[3];
                        boolean syncLaunch = commandPart[4].equals("true");
                        String[] args = Arrays.copyOfRange(commandPart, 5, commandPart.length);

                        ProcLauncher.launch(name, className, jar, args, syncLaunch);

                    } else if (command.startsWith("IO:")) {
                        // IO Codes:
                        //   0: stdin
                        //   1: stdout
                        //   2: stderr
                        //
                        // This will return the class name of the IO class
                        String id = command.substring("IO:".length());
                        switch (id) {
                            case "0":
                                socket.getOutputStream().write(ServicesManager.getServiceByName("io_stdin").getClassName().getBytes());
                                break;
                            case "1":
                                socket.getOutputStream().write(ServicesManager.getServiceByName("io_stdout").getClassName().getBytes());
                                break;
                            case "2":
                                socket.getOutputStream().write(ServicesManager.getServiceByName("io_stderr").getClassName().getBytes());
                                break;
                            default:
                                socket.getOutputStream().write("".getBytes());
                                break;
                        }
                    } else {
                        ScreenOutput.println("Unknown kernel command: " + command);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
