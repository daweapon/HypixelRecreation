package net.swofty.type.skyblockgeneric.event.actions.custom.loot;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.crimson.mob.MobDrop;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsOldWolf;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsWolf;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.MobSewerRat;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;

import java.util.ArrayList;
import java.util.List;

// Rolls the Combat pets that drop from existing mobs: Hound from the Ruins wolves, Rat from Hub rats and
// Phoenix from any mob
public class ActionCombatPetDrops implements HypixelEventClass {
    private static final List<MobDrop> EVERY_MOB = List.of(
            MobDrop.pet("PHOENIX_PET", Rarity.VERY_SPECIAL, 0.000003),
            MobDrop.pet("PHOENIX_PET", Rarity.SPECIAL, 0.000017));
    private static final List<MobDrop> RUINS_WOLF = List.of(
            MobDrop.pet("HOUND_PET", Rarity.LEGENDARY, 0.0003),
            MobDrop.pet("HOUND_PET", Rarity.EPIC, 0.001));
    private static final List<MobDrop> OLD_WOLF = List.of(
            MobDrop.pet("HOUND_PET", Rarity.LEGENDARY, 0.001),
            MobDrop.pet("HOUND_PET", Rarity.EPIC, 0.003));
    private static final List<MobDrop> RAT = List.of(MobDrop.pet("RAT_PET", Rarity.LEGENDARY, 0.2));

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockMob mob = event.getKilledMob();
        if (mob == null) return;
        List<MobDrop> drops = new ArrayList<>(EVERY_MOB);
        drops.addAll(dropsFor(mob));
        CrimsonMobLoot.roll(event.getPlayer(), mob, drops);
    }

    private static List<MobDrop> dropsFor(SkyBlockMob mob) {
        if (mob instanceof MobRuinsOldWolf) return OLD_WOLF;
        if (mob instanceof MobRuinsWolf) return RUINS_WOLF;
        if (mob instanceof MobSewerRat) return RAT;
        return List.of();
    }
}
