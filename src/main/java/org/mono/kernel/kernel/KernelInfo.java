package org.mono.kernel.kernel;

import com.google.gson.JsonObject;
import me.hysong.libhyextended.utils.JsonBeautifier;
import org.mono.kernel.NVRAM;
import org.mono.kernel.ServicesManager;

public class KernelInfo {
    public static String main() {
        JsonObject response = new JsonObject();
        JsonObject kernel = new JsonObject();
        JsonObject kernelModules = new JsonObject();
        JsonObject system = new JsonObject();
        kernel.addProperty("name", "Mono Basis Framework");
        kernel.addProperty("version", "0.1a");
        kernel.addProperty("author", "410-dev");
        kernel.addProperty("license", "Undefined");

        kernelModules.addProperty("bootloader", NVRAM.get("BOOTLOADER_NAME") + " " + NVRAM.get("BOOTLOADER_VERS"));
        kernelModules.addProperty("init", ServicesManager.getServiceByType("dummy-init").getName());
        kernelModules.addProperty("framework", ServicesManager.getServiceByType("dummy-framework").getName());
        kernelModules.addProperty("shell", ServicesManager.getServiceByType("shell").getName());
        kernelModules.addProperty("ui", ServicesManager.getServiceByType("ui").getName());
        kernelModules.addProperty("nvram", ServicesManager.getServiceByType("dummy-nvram").getName());
        kernelModules.addProperty("environment", ServicesManager.getServiceByType("dummy-env").getName());
        kernel.add("modules", kernelModules);

        system.addProperty("name", "MonoOS");
        system.addProperty("version", "0.1a");
        system.addProperty("author", "410-dev");
        system.addProperty("license", "Undefined");

        response.add("kernel", kernel);
        response.add("system", system);

        return JsonBeautifier.beautify(response);
    }
}
