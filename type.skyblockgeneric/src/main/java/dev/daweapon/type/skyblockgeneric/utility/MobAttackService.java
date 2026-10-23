package dev.daweapon.type.skyblockgeneric.utility;

import net.minestom.server.entity.damage.EntityDamage;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities.CreeperVeil;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalGear;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.runecrafting.RunicMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.PlayerStatistics;

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
        if (CreeperVeil.absorb(player)) return 0;
        ItemStatistics playerStatistics = AttributeEffectService.withLifelineDefense(player, player.getStatistics().allStatistics());

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

        float finalDamage = dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeSlayerGear.burststop(player, (float) valueEvent.getValue());
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        PetEvent.DamagedByMob damageEvent = player.getPetData()
                .dispatch(new PetEvent.DamagedByMob(player, pet, mob, finalDamage));
        finalDamage = (float) damageEvent.damage();

        dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetService.fire(player, dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger.ENTER_COMBAT);
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
        if (damage <= 0 || player.isRemoved() || CreeperVeil.absorb(player)) return;
        damage *= gearMultiplier(mob, player);
        damage = dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeSlayerGear.burststop(player, (float) damage);
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
        return mythological * RunicMobs.damageMultiplier(mob) * ReforgeLoadout.incomingMultiplier(player, mob) * dev.daweapon.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.incomingMultiplier(player, mob)
                * dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.mobDamageMultiplier(mob);
    }
}
