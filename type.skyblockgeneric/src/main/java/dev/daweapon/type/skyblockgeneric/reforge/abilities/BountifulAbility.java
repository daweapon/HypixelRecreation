package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class BountifulAbility implements ReforgeAbility.HarvestCoins {
    @Override
    public String reforgeId() {
        return "bountiful";
    }

    @Override
    public double coinsPerHarvest() {
        return 0.2;
    }
}
