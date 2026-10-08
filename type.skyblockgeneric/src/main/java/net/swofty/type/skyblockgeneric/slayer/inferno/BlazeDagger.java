package net.swofty.type.skyblockgeneric.slayer.inferno;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The six Blaze Slayer daggers: the two attunements each swaps between, its damage multipliers and the
 * on-hit ability that fires when a Hellion Shield switches attunement.
 */
public enum BlazeDagger {
    FIREDUST(ItemType.FIREDUST_DAGGER, HellionAttunement.ASHEN, HellionAttunement.AURIC, 1.2, MobType.WITHER, 1.1, 15, Vengeance.BURNING_VENGEANCE),
    TWILIGHT(ItemType.MAWDUST_DAGGER, HellionAttunement.SPIRIT, HellionAttunement.CRYSTAL, 1.5, MobType.SKELETAL, 1.2, 15, Vengeance.WRAITH_DRAIN),
    KINDLEBANE(ItemType.BURSTFIRE_DAGGER, HellionAttunement.ASHEN, HellionAttunement.AURIC, 1.5, MobType.WITHER, 1.2, 30, Vengeance.BURNING_VENGEANCE),
    MAWDREDGE(ItemType.BURSTMAW_DAGGER, HellionAttunement.SPIRIT, HellionAttunement.CRYSTAL, 2.5, MobType.SKELETAL, 1.5, 30, Vengeance.WRAITH_DRAIN),
    PYROCHAOS(ItemType.HEARTFIRE_DAGGER, HellionAttunement.ASHEN, HellionAttunement.AURIC, 2.0, MobType.WITHER, 1.5, 45, Vengeance.BURNING_VENGEANCE),
    DEATHRIPPER(ItemType.HEARTMAW_DAGGER, HellionAttunement.SPIRIT, HellionAttunement.CRYSTAL, 3.5, MobType.SKELETAL, 2.0, 45, Vengeance.WRAITH_DRAIN);

    public enum Vengeance { BURNING_VENGEANCE, WRAITH_DRAIN }

    private final ItemType item;
    private final HellionAttunement first;
    private final HellionAttunement second;
    private final double infernalMultiplier;
    private final MobType secondaryType;
    private final double secondaryMultiplier;
    private final int combatWisdom;
    private final Vengeance vengeance;

    BlazeDagger(ItemType item, HellionAttunement first, HellionAttunement second, double infernalMultiplier,
                MobType secondaryType, double secondaryMultiplier, int combatWisdom, Vengeance vengeance) {
        this.item = item;
        this.first = first;
        this.second = second;
        this.infernalMultiplier = infernalMultiplier;
        this.secondaryType = secondaryType;
        this.secondaryMultiplier = secondaryMultiplier;
        this.combatWisdom = combatWisdom;
        this.vengeance = vengeance;
    }

    public static @Nullable BlazeDagger of(@Nullable SkyBlockItem item) {
        if (item == null) return null;
        ItemType type = item.getAttributeHandler().getPotentialType();
        for (BlazeDagger dagger : values()) {
            if (dagger.item == type) return dagger;
        }
        return null;
    }

    public HellionAttunement attunement(SkyBlockItem item) {
        return item.getAttributeHandler().getDaggerAttunement() == 0 ? first : second;
    }

    public HellionAttunement first() {
        return first;
    }

    public HellionAttunement second() {
        return second;
    }

    public double multiplierAgainst(List<MobType> types) {
        double multiplier = 1;
        if (types.contains(MobType.INFERNAL)) multiplier *= infernalMultiplier;
        if (types.contains(secondaryType)) multiplier *= secondaryMultiplier;
        return multiplier;
    }

    public int combatWisdom() {
        return combatWisdom;
    }

    public Vengeance vengeance() {
        return vengeance;
    }
}
