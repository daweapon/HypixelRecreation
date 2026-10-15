package dev.daweapon.type.skyblockgeneric.staff;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryPower;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowers;
import dev.daweapon.type.skyblockgeneric.accessory.power.PowerScaling;
import dev.daweapon.type.skyblockgeneric.accessory.power.TuningStat;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * {@code /maxprofile}'s Maxwell step: every stone power learned (with its SkyBlock XP), every tuning
 * template slot unlocked, the power with the most total stats at the profile's Accessory Power
 * selected, and tuning left valid. Runs after the Accessory Bag is filled; a rerun changes nothing.
 */
public final class MaxPowers {
    public static final TuningStat DEFAULT_TUNING = TuningStat.STRENGTH;

    private MaxPowers() {
    }

    public static void apply(PowerData data, int accessoryPower, int combatLevel) {
        data.getLearned().addAll(AccessoryPowerType.stonePowers());
        data.setTemplatesUnlocked(AccessoryPowers.TEMPLATE_SLOTS);
        AccessoryPowers.select(data, AccessoryPowers.best(data, accessoryPower, combatLevel));

        int budget = PowerScaling.tuningPoints(accessoryPower);
        if (data.assignedPoints() > budget) data.getTuning().clear();
        if (data.assignedPoints() == 0 && budget > 0) data.getTuning().put(DEFAULT_TUNING, budget);
    }

    public static void maxOut(SkyBlockPlayer player) {
        AccessoryOwnership.invalidate(player);
        PowerData data = AccessoryPowers.data(player);
        apply(data, AccessoryPower.of(player), AccessoryPowers.combatLevel(player));
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.UNLOCKING_POWERS,
                data.getLearned().size() * AccessoryPowers.SKYBLOCK_XP_PER_POWER);
        AccessoryPowers.save(player, data);
    }
}
