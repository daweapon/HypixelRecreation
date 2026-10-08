package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.impl.TieredArmorSet;

import java.util.List;

public class RekindledEmberSet implements TieredArmorSet {
    private static final String[] BURNING_BONUS = {"200%", "400%", "600%"};
    private static final String[] PER_SECOND = {"1.2%", "2.5%", "5%"};
    private static final String[] CAP = {"50%", "100%", "200%"};

    @Override
    public String getName() {
        return "Rekindle";
    }

    @Override
    public int minimumPieces() {
        return 2;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        int tier = TieredArmorSet.tierIndex(wornPieces, minimumPieces());
        return List.of(
                "<7>Increase all of your outgoing",
                "<7>burning damage by <c>" + BURNING_BONUS[tier] + "<7>. For each",
                "<7>second you are on fire additionally",
                "<7>increase all burning damage by <c>" + PER_SECOND[tier],
                "<7>up to <c>" + CAP[tier] + "<7>. When wearing 2 or more",
                "<7>armor pieces of this set, melee",
                "<7>attacks from ignited mobs will set you",
                "<7>on fire!");
    }
}
