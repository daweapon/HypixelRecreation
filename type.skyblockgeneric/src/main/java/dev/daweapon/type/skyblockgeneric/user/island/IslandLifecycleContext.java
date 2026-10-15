package dev.daweapon.type.skyblockgeneric.user.island;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

public record IslandLifecycleContext(
        SkyBlockIsland island,
        boolean coop,
        List<SkyBlockPlayer> onlineMembers,
        List<UUID> memberProfiles) {
}
