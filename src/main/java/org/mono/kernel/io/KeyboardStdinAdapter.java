package org.mono.kernel.io;

import org.mono.kernel.ServicesManager;

import java.util.Scanner;

public class KeyboardStdinAdapter {

    // Service constructor
    public KeyboardStdinAdapter() {}
    public Integer main(String[] args) {return 0;}

    public static String readLine() {
        Scanner input = new Scanner(System.in);
        String line = input.nextLine();
        input.close();
        return line;
    }

    public static String readLine(String prompt) {
        try {
            Class.forName(ServicesManager.getServiceByName("io_stdout").getClassName()).getDeclaredMethod("print", String.class).invoke(null, prompt);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return readLine();
    }
}
