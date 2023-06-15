package org.mono.userspace.busybox;

import org.mono.userspace.Shell;

public class Echo {
    public Integer main(String[] args) {
        String result = "";
        for (String arg : args) {
            result += arg + " ";
        }
        Shell.println(result);
        return 0;
    }
}
