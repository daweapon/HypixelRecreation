package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;

public final class CandiedAbility implements ReforgeAbility.CandyDrop {
    @Override
    public String reforgeId() {
        return "candied";
    }

    @Override
    public double dropChanceBonus() {
        return 0.01;
    }
}
