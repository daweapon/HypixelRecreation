package dev.daweapon.service.control.integrity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * What a container runs, read from the SERVICE_CMD its entrypoint executes.
 *
 * Every JVM container in the stack is the same image told what to run by this one variable,
 * e.g. {@code java -jar HypixelCore.jar SKYBLOCK_HUB}, so it is the reliable way to learn both
 * which jar a container should hold and which server type it registers as.
 */
public record ServiceCommand(String jar, List<String> args) {

    public static ServiceCommand fromEnv(List<String> env) {
        for (String variable : env) {
            if (variable.startsWith("SERVICE_CMD=")) {
                return parse(variable.substring("SERVICE_CMD=".length()));
            }
        }
        return null;
    }

    public static ServiceCommand parse(String command) {
        if (command == null || command.isBlank()) {
            return null;
        }
        List<String> tokens = Arrays.stream(command.trim().split("\\s+")).toList();
        int jarFlag = tokens.indexOf("-jar");
        if (jarFlag < 0 || jarFlag + 1 >= tokens.size()) {
            return null;
        }
        String jar = tokens.get(jarFlag + 1);
        int slash = jar.lastIndexOf('/');
        if (slash >= 0) {
            jar = jar.substring(slash + 1);
        }
        return new ServiceCommand(jar, new ArrayList<>(tokens.subList(jarFlag + 2, tokens.size())));
    }

    /** The ServerType a game server registers with the proxy as, or null for anything else. */
    public String serverType() {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                return arg;
            }
        }
        return null;
    }
}
