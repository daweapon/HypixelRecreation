package net.swofty.type.skyblockgeneric.utility;

import net.minestom.server.entity.damage.EntityDamage;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalGear;
import net.swofty.type.skyblockgeneric.reforge.ReforgeLoadout;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;

import java.util.Map;

public final class MobAttackService {
    private MobAttackService() {
    }

    public static float strike(SkyBlockMob mob, SkyBlockPlayer player) {
        return strike(mob, player, mob.getStatistics());
    }

    public static float strike(SkyBlockMob mob, SkyBlockPlayer player, double damage) {
        return strike(mob, player, ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damage).build());
    }

    public static float strike(SkyBlockMob mob, SkyBlockPlayer player, ItemStatistics mobStatistics) {
        ItemStatistics playerStatistics = player.getStatistics().allStatistics();

        Map.Entry<Double, Boolean> damageDealt =
                PlayerStatistics.runPrimaryDamageFormula(mobStatistics, playerStatistics);

        double baseDefense = playerStatistics.getOverall(ItemStatistic.DEFENSE);
        double resistance = AttributeEffectService.resistanceDefense(player.getHuntingData(), mob);
        double resistanceMultiplier = (100D + Math.max(0, baseDefense))
                / (100D + Math.max(0, baseDefense + resistance));
        PlayerDamagedByMobValueUpdateEvent valueEvent = new PlayerDamagedByMobValueUpdateEvent(
                player, (float) (damageDealt.getKey() * resistanceMultiplier * gearMultiplier(mob, player)
                        * ReforgeLoadout.defenseMultiplier(player, mob, baseDefense)), mob);
        SkyBlockValueEvent.callValueUpdateEvent(valueEvent);

        float finalDamage = net.swofty.type.skyblockgeneric.slayer.inferno.BlazeSlayerGear.burststop(player, (float) valueEvent.getValue());
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        PetEvent.DamagedByMob damageEvent = player.getPetData()
                .dispatch(new PetEvent.DamagedByMob(player, pet, mob, finalDamage));
        finalDamage = (float) damageEvent.damage();

        CombatPetHooks.mobAttacked(player, mob);
        player.damage(new EntityDamage(mob, finalDamage));

        new DamageIndicator()
                .damage(finalDamage)
                .pos(player.getPosition())
                .critical(damageDealt.getValue())
                .display(player.getInstance());
        return finalDamage;
    }

    public static void trueStrike(SkyBlockMob mob, SkyBlockPlayer player, double damage) {
        if (damage <= 0 || player.isRemoved()) return;
        damage *= gearMultiplier(mob, player);
        damage = net.swofty.type.skyblockgeneric.slayer.inferno.BlazeSlayerGear.burststop(player, (float) damage);
        CombatPetHooks.mobAttacked(player, mob);
        player.damage(new EntityDamage(mob, (float) damage));
        new DamageIndicator()
                .damage((float) damage)
                .pos(player.getPosition())
                .critical(false)
                .display(player.getInstance());
    }

    private static double gearMultiplier(SkyBlockMob mob, SkyBlockPlayer player) {
        double mythological = mob instanceof MythologicalCreatureMob ? MythologicalGear.incomingDamageMultiplier(player) : 1;
        return mythological * ReforgeLoadout.incomingMultiplier(player, mob) * net.swofty.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.incomingMultiplier(player, mob)
                * net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.mobDamageMultiplier(mob);
    }
}
