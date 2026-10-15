package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import lombok.NonNull;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.item.set.sets.BerserkerArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Health-consuming ability cost. Berserker Armor's tiered bonus reduces it.
 */
public final class HealthAbilityCost extends RegisteredAbility.AbilityCost {
    private final double amount;
    private final boolean percentOfMax;

    private HealthAbilityCost(double amount, boolean percentOfMax) {
        this.amount = amount;
        this.percentOfMax = percentOfMax;
    }

    public static HealthAbilityCost flat(double health) {
        return new HealthAbilityCost(health, false);
    }

    public static HealthAbilityCost percentOfMax(double percent) {
        return new HealthAbilityCost(percent, true);
    }

    public double cost(SkyBlockPlayer player) {
        double base = percentOfMax ? player.getMaxHealth() * amount / 100 : amount;
        return base * BerserkerArmorSet.healthCostMultiplier(player);
    }

    @Override
    public boolean canUse(@NonNull SkyBlockPlayer player) {
        return player.getHealth() > cost(player);
    }

    @Override
    public void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability) {
        player.setHealth((float) (player.getHealth() - cost(player)));
    }

    @Override
    public void onFail(@NonNull SkyBlockPlayer player) {
        player.sendMessage("<c>You don't have enough health to use this ability!");
    }

    @Override
    public @Nullable String getLoreDisplay() {
        return "<8>Health Cost: <c>" + (amount == Math.floor(amount) ? String.valueOf((long) amount) : String.valueOf(amount))
                + (percentOfMax ? "%" : "");
    }
}
