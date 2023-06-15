package org.mono.userspace.busybox;

import org.mono.userspace.Shell;

public class Info {
    public Integer main(String[] args) {
        String output = Shell.kernelAPI("info");
        Shell.println(output);
        return 0;
    }
}
