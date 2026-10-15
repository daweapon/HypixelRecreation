package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilities;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorWorn;
import dev.daweapon.type.skyblockgeneric.item.set.sets.ArmorPieceEffects;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ArmorDamageTakenEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number number)) return;
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockMob mob = ((PlayerDamagedByMobValueUpdateEvent) event).getMob();
        double damage = number.doubleValue();
        if (damage <= 0) return;

        if (ArmorWorn.helmetIs(player, "SKELETON_HELMET") && ArmorPieceEffects.consumeBone(player)) {
            set(event, number, 0);
            return;
        }

        double adjusted = ArmorAbilities.mistAura(damage, isGhost(mob), ArmorWorn.countPrefix(player, "SORROW"));
        double defense = player.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
        boolean undead = mob.getMobTypes().contains(MobType.UNDEAD);
        if (undead && trollingTheReaper(player)) {
            adjusted = ArmorAbilities.extraDefenseTaken(adjusted, defense, ArmorAbilities.UNDEAD_DEFENSE);
        }
        if (mob.getMobTypes().contains(MobType.ANIMAL) && "BOOTS_OF_THE_PACK".equals(ArmorWorn.idOf(player.getBoots()))) {
            adjusted = ArmorAbilities.extraDefenseTaken(adjusted, defense, ArmorAbilities.ANIMAL_DEFENSE);
        }
        if (adjusted != damage) set(event, number, adjusted);
    }

    public static boolean trollingTheReaper(SkyBlockPlayer player) {
        return ArmorWorn.countPrefix(player, "REVENANT") >= ArmorAbilities.TROLLING_PIECES
                || ArmorWorn.countPrefix(player, "REAPER") >= ArmorAbilities.TROLLING_PIECES;
    }

    private static boolean isGhost(SkyBlockMob mob) {
        String name = mob.getDisplayName();
        return name != null && name.toLowerCase().contains("ghost");
    }

    private static void set(ValueUpdateEvent event, Number original, double value) {
        event.setValue(original instanceof Float ? (Object) (float) value : (Object) value);
    }
}
