package dev.daweapon.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.event.player.PlayerTickEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetEquipEffects;

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
        MiscPetHooks.petTick(player);
        if (ticks % SYNC_TICKS == 0) PetEquipEffects.apply(player);
    }
}
