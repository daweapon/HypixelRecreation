package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class WiseDragonArmorSet implements ArmorSet {
    public static final double MANA_COST_MULTIPLIER = 2.0 / 3.0;

    public static double manaCostMultiplier(SkyBlockPlayer player) {
        return new WiseDragonArmorSet().isWearingSet(player) ? MANA_COST_MULTIPLIER : 1;
    }

    @Override
    public String getName() {
        return "Wise Blood";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of("<7>Abilities have <a>2/3 <7>of the mana cost."));
    }
}
