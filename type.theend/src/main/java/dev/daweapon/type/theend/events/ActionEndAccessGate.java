package dev.daweapon.type.theend.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import dev.daweapon.type.theend.SepulturePads;

public class ActionEndAccessGate implements HypixelEventClass {
    public static final int ISLAND_COMBAT_LEVEL = 12;
    public static final int SEPULTURE_COMBAT_LEVEL = 25;

    public static boolean meetsIslandLevel(int combatLevel) {
        return combatLevel >= ISLAND_COMBAT_LEVEL;
    }

    public static boolean meetsSepultureLevel(int combatLevel) {
        return combatLevel >= SEPULTURE_COMBAT_LEVEL;
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void island(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (player.getRank().isStaff() || meetsIslandLevel(combatLevel(player))) return;
        player.sendMessage("<c>You must have Combat " + ISLAND_COMBAT_LEVEL + " unlocked to access The End!");
        player.asProxyPlayer().transferToWithIndication(ServerType.SKYBLOCK_HUB);
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void sepulture(PlayerRegionChangeEvent event) {
        if (event.getTo() != RegionType.VOID_SEPULTURE) return;
        SkyBlockPlayer player = event.getPlayer();
        if (player.getRank().isStaff() || meetsSepultureLevel(combatLevel(player))) return;
        SepulturePads.markUsed(player);
        player.teleport(SepulturePads.EXIT);
        player.sendMessage("<c>You must have Combat " + SEPULTURE_COMBAT_LEVEL + " unlocked to enter the Void Sepulture!");
    }

    private static int combatLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT);
    }
}
