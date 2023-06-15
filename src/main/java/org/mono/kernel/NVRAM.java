package org.mono.kernel;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;

public class NVRAM implements Serializable {

    @Getter private final String serial;
    private final HashMap<String, String> data;

    public NVRAM(String serial) {
        this.serial = serial;
        this.data = new HashMap<>();
    }

    private String getData(String key) {
        return data.get(key);
    }

    private void setData(String key, String value) {
        data.put(key, value);
    }


    private static NVRAM systemNVRAM;
    protected static NVRAM getSystemNVRAM() {
        return systemNVRAM;
    }

    protected static void setSystemNVRAM(NVRAM nvram) {
        systemNVRAM = nvram;
    }

    public static String get(String key) {
        return getSystemNVRAM().getData(key);
    }

    protected static void set(String key, String value) {
        getSystemNVRAM().setData(key, value);
    }

    protected static String getParameter(String[] args, String key) {
        boolean found = false;
        for (String arg : args) {
            if (arg.startsWith(key + "=")) {
                return arg.substring(key.length() + 1);
            } else if (arg.equals(key)) {
                found = true;
            }
        }

        if (found) {
            return "true";
        }
        return null;
    }
}
