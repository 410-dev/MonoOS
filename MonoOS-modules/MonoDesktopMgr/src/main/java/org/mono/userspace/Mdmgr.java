package org.mono.userspace;

import org.mono.KernelDirectAPI;
import org.mono.kernel.Service;

import javax.swing.*;

public class Mdmgr {

    private JFrame frame;

    public Integer main(String[] args) {
        try {
            frame = new JFrame("Desktop Manager");
            int width = 800;
            int height = 600;
            try {
                Service gpuDriver = KernelDirectAPI.getServiceByType("driver-gpu");
                width = (int) gpuDriver.loadObject().getClass().getDeclaredMethod("getDisplayWidth").invoke(gpuDriver.loadObject());
                height = (int) gpuDriver.loadObject().getClass().getDeclaredMethod("getDisplayHeight").invoke(gpuDriver.loadObject());
            }catch (Exception e) {
                e.printStackTrace();
                KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, "Failed to get display size, using default size (800x600)");
                KernelDirectAPI.getServiceByType("stdout").loadObject().getClass().getDeclaredMethod("println", String.class).invoke(null, "Error: " + e.getMessage());
            }
            frame.setSize(width, height);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
