package org.mono.userspace;

import me.hysong.libhyextended.utils.StackTraceStringifier;
import org.mono.kernel.ServicesManager;
import org.mono.kernel.io.ScreenOutput;

import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class BasicShellUI {

    public BasicShellUI(){}

    public Integer main(String[] args) {
        try (Socket socket = new Socket("localhost", 65532)) {
            Shell.println("Connected to shell...");
            String response = "0";
            while (true) {
                String command = Shell.readLine("BasicShellUI [" + response + "] >>> ");

                if (command == null || command.isEmpty()) continue;

                // Send command to localhost:65530 socket without format
                socket.getOutputStream().write(command.getBytes());
                socket.getOutputStream().flush();

                // Receive response from localhost:65530 socket without format
                byte[] buffer = new byte[1024];
                int read = socket.getInputStream().read(buffer);
                if (read > 0) {
                    response = new String(buffer, 0, read);
                }
            }
        } catch (NoSuchElementException e) {
            try {
                Shell.println("No such element exception, waiting...");
                Thread.sleep(1000);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            Shell.println("Failed!");
            Shell.println("Error: " + StackTraceStringifier.stringify(e) + "\n");
            return 1;
        }
        return 0;
    }

}
