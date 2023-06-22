package org.mono.kernel.io;

import org.mono.kernel.ServicesManager;

import java.util.Scanner;

public class KeyboardStdinAdapter {

    // Service constructor
    public KeyboardStdinAdapter() {}
    public Integer main(String[] args) {return 0;}

    public static String readLine() {
        Scanner input = new Scanner(System.in);
        if (input.hasNextLine())
            return input.nextLine();
        return null;
    }

    public static String readLine(String prompt) {
        try {
            ServicesManager.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("print", String.class).invoke(null, prompt);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return readLine();
    }
}
