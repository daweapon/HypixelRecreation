package dev.daweapon.type.skyblockgeneric.event.actions.custom.loot;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsOldWolf;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobRuinsWolf;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.hub.MobSewerRat;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;

import java.util.ArrayList;
import java.util.List;

// Rolls the Combat pets that drop from existing mobs: Hound from the Ruins wolves, Rat from Hub rats and
// Phoenix from any mob
public class ActionCombatPetDrops implements HypixelEventClass {
    public static final double JERRY_CANDY_CHANCE_PERCENT = 0.5;

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
        if (CombatPetHooks.findsJerryCandy(event.getPlayer(), JERRY_CANDY_CHANCE_PERCENT)) {
            event.getPlayer().addAndUpdateItem(ItemType.JERRY_CANDY, 1);
        }
    }

    private static List<MobDrop> dropsFor(SkyBlockMob mob) {
        if (mob instanceof MobRuinsOldWolf) return OLD_WOLF;
        if (mob instanceof MobRuinsWolf) return RUINS_WOLF;
        if (mob instanceof MobSewerRat) return RAT;
        return List.of();
    }
}
