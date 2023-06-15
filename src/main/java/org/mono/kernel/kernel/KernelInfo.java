package org.mono.kernel.kernel;

import com.google.gson.JsonObject;
import me.hysong.libhyextended.utils.JsonBeautifier;

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

        kernelModules.addProperty("bootloader", "Built-in STG2 bootloader");
        kernelModules.addProperty("init", "ServicesManager");
        kernelModules.addProperty("frameworks", "ServicesManager");
        kernelModules.addProperty("shell", "Shell");
        kernelModules.addProperty("ui", "BasicShellUI");
        kernelModules.addProperty("nvram", "On-FS NVRAM");
        kernelModules.addProperty("environment", "Environment");
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
