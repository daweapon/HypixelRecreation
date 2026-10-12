package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;

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
