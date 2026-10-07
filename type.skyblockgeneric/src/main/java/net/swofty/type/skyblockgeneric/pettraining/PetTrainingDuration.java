package net.swofty.type.skyblockgeneric.pettraining;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minestom.server.item.Material;

@Getter
@RequiredArgsConstructor
public enum PetTrainingDuration {
    FIVE_DAYS("5 Days", Material.CLOCK, 5),
    TEN_DAYS("10 Days", Material.CLOCK, 10),
    TWENTY_DAYS("20 Days", Material.CLOCK, 20),
    MAX_LEVEL("Until Max Level", Material.EXPERIENCE_BOTTLE, 0),
    ;

    private final String displayName;
    private final Material icon;
    private final int fixedDays;

    public boolean isTargetLevel() {
        return this == MAX_LEVEL;
    }
}
