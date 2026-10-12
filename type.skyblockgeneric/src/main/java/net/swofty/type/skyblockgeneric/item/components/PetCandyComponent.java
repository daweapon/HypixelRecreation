package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.item.SafeConfig;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetCandy;

/**
 * Pet food such as Carrot Candy: feeding it to the summoned pet grants a fixed amount of Pet Exp.
 * Candy that counts towards the lifetime cap can only be eaten {@value #LIFETIME_CAP} times per pet.
 */
@Getter
public class PetCandyComponent extends SkyBlockItemComponent {
    public static final int LIFETIME_CAP = 10;

    private final double experience;
    private final boolean countsTowardsCap;
    private final int levels;
    private final boolean toMaxLevel;

    private PetCandyComponent(double experience, boolean countsTowardsCap, int levels, boolean toMaxLevel) {
        this.experience = experience;
        this.countsTowardsCap = countsTowardsCap;
        this.levels = levels;
        this.toMaxLevel = toMaxLevel;
        addInheritedComponent(new InteractableComponent(this::feed, null, null));
    }

    public static PetCandyComponent parse(SafeConfig config) {
        return new PetCandyComponent(config.getDouble("experience", 0.0), config.getBoolean("counts_towards_cap", true),
                config.getInt("levels", 0), config.getBoolean("to_max_level", false));
    }

    public boolean grantsLevels() {
        return levels > 0 || toMaxLevel;
    }

    private void feed(SkyBlockPlayer player, SkyBlockItem candy) {
        PetCandy.feed(player, candy, this);
    }
}
