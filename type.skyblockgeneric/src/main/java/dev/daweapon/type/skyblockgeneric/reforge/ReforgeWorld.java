package dev.daweapon.type.skyblockgeneric.reforge;

import dev.daweapon.commons.ServerType;

import java.util.Set;

public interface ReforgeWorld {
    ServerType server();

    double cold();

    int armorThornsTiers();

    default boolean isOn(Set<ServerType> servers) {
        return server() != null && servers.contains(server());
    }

    record Fixed(ServerType server, double cold, int armorThornsTiers) implements ReforgeWorld {
        public static Fixed on(ServerType server) {
            return new Fixed(server, 0, 0);
        }
    }
}
