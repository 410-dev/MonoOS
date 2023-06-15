package org.mono.userspace;

import org.mono.kernel.io.ScreenOutput;

import java.net.Socket;
import java.util.Scanner;

public class BasicShellUI {

    public BasicShellUI(){}

    public Integer main(String[] args) {
        Scanner input = new Scanner(System.in);
        try (Socket socket = new Socket("localhost", 65532)) {
            Shell.println("Connected to shell...");
            while (true) {
                Shell.print("BasicShellUI >>> ");
                String command = input.nextLine();

                // Send command to localhost:65530 socket without format
                socket.getOutputStream().write(command.getBytes());
                socket.getOutputStream().flush();

                // Receive response from localhost:65530 socket without format
                byte[] buffer = new byte[1024];
                int read = socket.getInputStream().read(buffer);
                if (read > 0) {
                    String response = new String(buffer, 0, read);
                    Shell.println("Process exit: " + response);
                }
            }
        } catch (Exception e) {
            Shell.println("Failed!\n");
            Shell.println("Error: " + e.getMessage() + "\n");
            return 1;
        }
    }

}
