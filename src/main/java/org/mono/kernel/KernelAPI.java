package org.mono.kernel;

import me.hysong.libhyextended.Utils;
import org.mono.kernel.io.ScreenOutput;
import org.mono.kernel.kernel.KernelInfo;
import org.mono.kernel.kernel.ProcLauncher;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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
}
