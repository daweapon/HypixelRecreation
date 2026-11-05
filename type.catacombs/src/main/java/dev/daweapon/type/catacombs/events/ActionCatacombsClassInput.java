package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.classes.ClassCombat;
import dev.daweapon.type.catacombs.classes.ClassItems;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUIDungeonClasses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;

/**
 * Class controls in a run, as on Hypixel: right-clicking the Dungeon Orb (or the drop-stack key) uses the class ability,
 * left-clicking it (or the drop key) the ultimate; ghosts right-click Haunt and their two ghost items; the waiting
 * room's Nether Star opens Dungeon Classes. Drop keys never drop items while the run is active.
 */
public class ActionCatacombsClassInput implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onUse(PlayerUseItemEvent event) {
        String kind = ClassItems.kind(event.getItemStack());
        if (kind == null) return;
        event.setCancelled(true);
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null) return;
        switch (kind) {
            case ClassItems.SELECTOR -> {
                if (!run.started()) player.openView(new GUIDungeonClasses((who, type) -> run.classes().select(who, type)));
            }
            case ClassItems.ORB -> run.classes().abilities().orb(player, false);
            case ClassItems.HAUNT -> run.classes().abilities().haunt(player);
            case ClassItems.GHOST_FIRST -> run.classes().abilities().ghost(player, 0);
            case ClassItems.GHOST_SECOND -> run.classes().abilities().ghost(player, 1);
            default -> {
            }
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onBeginUse(PlayerBeginItemUseEvent event) {
        if (ClassItems.isClassItem(event.getItemStack())) event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onSwing(PlayerHandAnimationEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (ClassItems.ORB.equals(ClassItems.kind(player.getItemInMainHand()))) {
            CatacombsRun run = CatacombsRuns.of(player.getUuid());
            if (run != null) run.classes().abilities().orb(player, true);
            return;
        }
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> ClassCombat.mageStaff(player));
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onDropKey(PlayerPacketEvent event) {
        if (!(event.getPacket() instanceof ClientPlayerActionPacket packet)) return;
        boolean single = packet.status() == ClientPlayerActionPacket.Status.DROP_ITEM;
        boolean stack = packet.status() == ClientPlayerActionPacket.Status.DROP_ITEM_STACK;
        if (!single && !stack) return;
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null || !run.classes().host().classesActive()) return;
        event.setCancelled(true);
        player.getInventory().update();
        if (run.isGhost(player.getUuid())) return;
        run.classes().abilities().orb(player, single);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onDrop(ItemDropEvent event) {
        if (ClassItems.isClassItem(event.getItemStack())) event.setCancelled(true);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        for (CatacombsRun run : CatacombsRuns.all()) {
            if (run.isMember(player.getUuid())) run.classes().onDisconnect(player);
        }
    }
}
