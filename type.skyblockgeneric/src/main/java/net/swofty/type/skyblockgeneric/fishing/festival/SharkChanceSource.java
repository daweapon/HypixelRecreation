package net.swofty.type.skyblockgeneric.fishing.festival;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.fishing.FishingContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToDoubleFunction;

/**
 * Every real modifier on the chance for a Shark to replace a Sea Creature during the Fishing
 * Festival (wiki: Fishing Festival, Mechanics). Sources whose item this server lacks keep their
 * real maximum and the reason, and add nothing until they are wired.
 */
@Getter
public enum SharkChanceSource {
    SHARK_SONAR("Doug's Shark Sonar perk", 5, null,
            context -> Carnivals.perkValue(context.player(), CarnivalPerk.SHARK_SONAR)),
    SHARK_BAIT("Shark Bait", 20, null,
            context -> context.bait() == null ? 0 : context.bait().getTagBonuses().getOrDefault("SHARK", 0D)),
    SHARK_SCALE_ARMOR("Shark Scale Armor (Festival Fisher)", 20,
            "Shark Scale Armor and the Sponge Armor it is crafted from aren't in this server yet.",
            context -> 0),
    MEGALODON_PET("Megalodon Pet (Feeding Frenzy)", 20,
            "The Megalodon Pet isn't in this server yet.",
            context -> 0),
    ;

    private final String displayName;
    private final double maxBonus;
    private final @Nullable String unavailableReason;
    private final ToDoubleFunction<FishingContext> bonus;

    SharkChanceSource(String displayName, double maxBonus, @Nullable String unavailableReason,
                      ToDoubleFunction<FishingContext> bonus) {
        this.displayName = displayName;
        this.maxBonus = maxBonus;
        this.unavailableReason = unavailableReason;
        this.bonus = bonus;
    }

    public boolean isAvailable() {
        return unavailableReason == null;
    }

    public double bonus(FishingContext context) {
        return bonus.applyAsDouble(context);
    }
}
