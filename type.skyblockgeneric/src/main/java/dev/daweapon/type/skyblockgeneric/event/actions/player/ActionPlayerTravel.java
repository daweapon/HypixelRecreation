package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEventKeys;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.MissionSet;
import dev.daweapon.type.skyblockgeneric.mission.missions.MissionUseTeleporter;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.island.portal.PlacedPortal;
import dev.daweapon.type.skyblockgeneric.user.island.portal.PortalCatalog;
import dev.daweapon.type.skyblockgeneric.utility.WarpPortal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Handles server-transfer portals (nether/end) and teleporting players through warp portals
public class ActionPlayerTravel implements HypixelEventClass {
    public static List<UUID> delay = new ArrayList<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (delay.contains(player.getUuid())) return;
        delay.add(player.getUuid());

        MinecraftServer.getSchedulerManager().buildTask(() -> delay.remove(player.getUuid()))
                .delay(Duration.ofMillis(500))
                .schedule();

        Key block = player.getInstance().getBlock(player.getPosition()).key();
        if (block == Block.NETHER_PORTAL.key()) {
            MissionData data = player.getMissionData();

            if (!MissionSet.GETTING_STARTED.hasCompleted(player)
                    && !data.isCurrentlyActive(MissionUseTeleporter.class)
            ) {
                player.sendMessage("<c>You must complete your starting missions!");
                return;
            }

            if (HypixelConst.isIslandServer()) {
                travelThroughIslandPortal(player);
                return;
            }

            player.sendTo(HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_HUB ? ServerType.SKYBLOCK_DUNGEON_HUB : ServerType.SKYBLOCK_HUB);
        }

        if (block == Block.END_PORTAL.key()) {
            player.sendTo(ServerType.SKYBLOCK_ISLAND);
        }

        WarpPortal.getWarpPortals().forEach(warp -> {
            // distanceSquared is a micro-optimization
            if (event.getNewPosition().distanceSquared(warp.vector().asPos()) < 2) {
                String closed = warp.closedReason().apply(player);
                if (closed != null) {
                    player.sendMessage(closed);
                    return;
                }
                player.teleport(warp.pos());
                player.playSound(
                        Sound.sound(
                                SoundEventKeys.ENTITY_ENDERMAN_TELEPORT.key(),
                                Sound.Source.PLAYER,
                                1f, 1f
                        )
                );
                player.sendMessage("<d>Warped to <b>{}<d>!", warp.text());
            }
        });
    }

    private void travelThroughIslandPortal(SkyBlockPlayer player) {
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        if (island == null || island.getPortals() == null) return;
        PlacedPortal portal = island.getPortals().activePortalAt(
                player.getPosition().blockX(), player.getPosition().blockY(), player.getPosition().blockZ());
        if (portal == null) return;
        PortalCatalog.get(portal.itemId()).travel(player);
    }
}
