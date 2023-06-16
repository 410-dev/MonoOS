package org.mono.kernel;

import java.util.ArrayList;
import java.util.HashMap;

public class Environment {
    public static final String KERNEL_EXTENSIONS = "/sys/kernel/extensions";
    public static final String KERNEL_SERVICES = "/sys/kernel/services";


    public static final String SYSTEM_LINKS = "/sys/system/links";


    public static final String USER_LINKS = "$USER_HOME/links";


    public static final HashMap<String, String> variables = new HashMap<>();

    public static String get(String key) {
        return variables.get(key);
    }

    public static void set(String key, String value) {
        String[] invalidCharacters = new String[]{" ", "\t", "\n", "\r", "\f", "\b", "\\", "\"", "'", "`", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "+", "=", "{", "}", "[", "]", "|", "\\", ";", ":", "<", ">", ",", ".", "/", "?"};
        for (String invalidCharacter : invalidCharacters) {
            if (key.contains(invalidCharacter)) {
                throw new RuntimeException("Invalid character in variable name: " + invalidCharacter);
            }
        }
        variables.put(key, value);
    }

    public static String substituteInLineVariable(String string) {
        // Convert hash map keys to array list
        ArrayList<String> keys = new ArrayList<>(variables.keySet());

        // Sort by length. Longer one first.
        keys.sort((o1, o2) -> o2.length() - o1.length());

        // Substitute
        for (String key : keys) {
            string = string.replace("$" + key, variables.get(key));
        }

        return string;
    }
}
