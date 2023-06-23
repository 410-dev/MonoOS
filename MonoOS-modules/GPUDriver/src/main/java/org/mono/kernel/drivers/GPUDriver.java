package org.mono.kernel.drivers;

import java.awt.*;

public class GPUDriver {
    public Integer main(String[] args) {
        return 0;
    }

    // Hard coded!
    public static String getGPUName() {
        return "Generic Internal GPU";
    }

    // Hard coded!
    public static int getGPUMemoryMB() {
        return 1024;
    }

    // Hard coded!
    public static String getGPUVendor() {
        return "Generic";
    }

    // Hard coded!
    public static String getGPUDriverVersion() {
        return "1.0";
    }

    public static int[] getResolution() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int[] resolution = new int[2];
        resolution[0] = screenSize.width;
        resolution[1] = screenSize.height;
        return resolution;
    }

    public static int getDisplayWidth() {
        return getResolution()[0];
    }

    public static int getDisplayHeight() {
        return getResolution()[1];
    }
}
