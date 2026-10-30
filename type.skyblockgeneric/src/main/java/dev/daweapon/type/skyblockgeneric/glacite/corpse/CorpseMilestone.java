package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

public record CorpseMilestone(int tier, int lapis, int umber, int tungsten, int vanguard, long powder, long rngXp,
                              int skyBlockXp, @Nullable ItemType rewardItem, double bonusCorpse, double bonusRoll) {

    public int required(CorpseType type) {
        return switch (type) {
            case LAPIS -> lapis;
            case UMBER -> umber;
            case TUNGSTEN -> tungsten;
            case VANGUARD -> vanguard;
        };
    }

    public boolean reached(java.util.function.ToIntFunction<CorpseType> looted) {
        for (CorpseType type : CorpseType.values()) {
            if (looted.applyAsInt(type) < required(type)) return false;
        }
        return true;
    }
}
