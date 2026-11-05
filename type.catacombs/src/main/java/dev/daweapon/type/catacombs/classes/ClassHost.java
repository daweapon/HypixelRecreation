package dev.daweapon.type.catacombs.classes;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.UUID;

/** What the class system needs from the run that owns it. */
public interface ClassHost {
    Instance instance();

    List<SkyBlockPlayer> alivePlayers();

    List<SkyBlockPlayer> onlineMembers();

    boolean isGhost(UUID player);

    boolean classesActive();

    void later(int ticks, Runnable action);

    void broadcast(String message);

    boolean reviveAfter(UUID player, int seconds);
}
