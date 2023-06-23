package org.mono.userspace.busybox;

import org.mono.userspace.Shell;

public class Info {
    public Integer main(String[] args) {
        try {
            String output = Class.forName("org.mono.kernel.kernel.KernelInfo").getDeclaredMethod("main").invoke(null).toString();
            Shell.println(output);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
