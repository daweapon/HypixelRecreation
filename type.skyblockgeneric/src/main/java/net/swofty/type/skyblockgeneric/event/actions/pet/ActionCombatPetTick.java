package net.swofty.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.event.player.PlayerTickEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetEquipEffects;

// Per-player pet checks that need a clock: Skeletal Defense's scan and keeping High Stride and Magic Bird in sync
public class ActionCombatPetTick implements HypixelEventClass {
    private static final int SCAN_TICKS = 10;
    private static final int SYNC_TICKS = 40;

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerTickEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        long ticks = player.getAliveTicks();
        if (ticks % SCAN_TICKS != 0) return;
        if (SkyBlockDataHandler.getUser(player) == null) return;
        CombatPetHooks.petTick(player);
        if (ticks % SYNC_TICKS == 0) PetEquipEffects.apply(player);
    }
}
