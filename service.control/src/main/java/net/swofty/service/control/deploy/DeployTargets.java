package net.swofty.service.control.deploy;

import net.swofty.service.control.fleet.ContainerInfo;
import net.swofty.service.control.fleet.FleetGroup;

import java.util.List;
import java.util.Locale;

/**
 * Works out which containers a given jar belongs in.
 *
 * Deploying to the wrong subset is the version-skew failure this project has already hit
 * (tracker #85), where mismatched jars across a rolling deploy looked exactly like a player's
 * inventory had been wiped. Defaults are therefore derived from the jar name rather than left
 * to whoever is pressing the button.
 */
public final class DeployTargets {

    private DeployTargets() {
    }

    /** The jar is bind-mounted rather than baked into the image, so it is replaced on the host. */
    public static boolean isBindMounted(String jarName) {
        return jarName.equalsIgnoreCase("ServiceControl.jar");
    }

    public static List<String> defaultTargets(String jarName, List<ContainerInfo> fleet) {
        String name = jarName.toLowerCase(Locale.ROOT);

        if (name.equals("hypixelcore.jar")) {
            // Every game server runs the same fat jar; a partial rollout is the skew bug.
            return fleet.stream()
                    .filter(container -> container.group() == FleetGroup.GAME_SERVER)
                    .map(ContainerInfo::name)
                    .sorted()
                    .toList();
        }

        String service = serviceContainerFor(jarName);
        if (service == null) {
            return List.of();
        }
        return fleet.stream()
                .map(ContainerInfo::name)
                .filter(service::equals)
                .toList();
    }

    /**
     * Maps ServiceFoo.jar to its service_foo container.
     *
     * The jar names are camel case and the containers are snake case, and they do not always
     * split on the same boundaries, so the few that differ are listed explicitly rather than
     * guessed at.
     */
    public static String serviceContainerFor(String jarName) {
        if (!jarName.toLowerCase(Locale.ROOT).startsWith("service")
                || !jarName.toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return null;
        }
        String core = jarName.substring("Service".length(), jarName.length() - ".jar".length());
        return switch (core.toLowerCase(Locale.ROOT)) {
            case "api" -> "service_api";
            case "auctionhouse" -> "service_auctionhouse";
            case "bazaar" -> "service_bazaar";
            case "itemtracker" -> "service_itemtracker";
            case "party" -> "service_party";
            case "darkauction" -> "service_darkauction";
            case "orchestrator" -> "service_orchestrator";
            case "friend" -> "service_friend";
            case "store" -> "service_store";
            case "election" -> "service_election";
            case "guild" -> "service_guild";
            case "punishment" -> "service_punishment";
            case "replay" -> "service_replay";
            case "control" -> "service_control";
            default -> null;
        };
    }
}
