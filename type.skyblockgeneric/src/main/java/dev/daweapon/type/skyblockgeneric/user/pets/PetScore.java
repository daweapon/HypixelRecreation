package dev.daweapon.type.skyblockgeneric.user.pets;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.achievement.PetAchievements;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hypixel's Pet Score: each unique pet scores its rarity (Common 1 to Mythic 6) plus 1 at its max
 * level, counting only the best copy of duplicates (the four Wisp forms are one pet). Pets flagged {@code pet_score: false} (Eagle, Bingo,
 * Phoenix) never count. Thresholds grant Magic Find, and every point
 * is worth 3 SkyBlock XP.
 */
public final class PetScore {
    public static final List<Integer> MAGIC_FIND_THRESHOLDS =
            List.of(10, 25, 50, 75, 100, 130, 175, 225, 275, 325, 375, 450, 500);
    public static final int SKYBLOCK_XP_PER_POINT = 3;

    private static final Set<ItemType> WISP_FORMS = Set.of(ItemType.DROPLET_WISP_PET, ItemType.FROST_WISP_PET,
            ItemType.GLACIAL_WISP_PET, ItemType.SUBZERO_WISP_PET);

    private static final Comparator<SkyBlockItem> BEST_COPY = Comparator
            .comparingInt((SkyBlockItem pet) -> pet.getAttributeHandler().getRarity().ordinal())
            .thenComparingInt(PetScore::level);

    private PetScore() {
    }

    public static int of(Collection<SkyBlockItem> pets) {
        Map<ItemType, SkyBlockItem> best = new HashMap<>();
        for (SkyBlockItem pet : pets) {
            if (!pet.hasComponent(PetComponent.class) || !pet.getComponent(PetComponent.class).isCountsForPetScore()) {
                continue;
            }
            best.merge(scoreKey(pet.getAttributeHandler().getPotentialType()), pet,
                    (a, b) -> BEST_COPY.compare(a, b) >= 0 ? a : b);
        }
        return best.values().stream().mapToInt(PetScore::pointsFor).sum();
    }

    public static int of(SkyBlockPlayer player) {
        return of(player.getPetData().getPetsMap().keySet());
    }

    public static int magicFind(int score) {
        return (int) MAGIC_FIND_THRESHOLDS.stream().filter(threshold -> score >= threshold).count();
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        int magicFind = magicFind(of(player));
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, (double) magicFind).build();
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        PetAchievements.refresh(player);
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        double earned = (double) of(player) * SKYBLOCK_XP_PER_POINT;
        if (earned > experience.getSourceXP(SkyBlockXPSource.PET_SCORE)) {
            experience.setSourceXP(SkyBlockXPSource.PET_SCORE, earned);
        }
    }

    private static ItemType scoreKey(ItemType type) {
        return WISP_FORMS.contains(type) ? ItemType.SUBZERO_WISP_PET : type;
    }

    private static int pointsFor(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        int points = Math.min(rarity.ordinal(), Rarity.MYTHIC.ordinal()) + 1;
        return PetLevels.isMaxLevel(pet) ? points + 1 : points;
    }

    private static int level(SkyBlockItem pet) {
        return PetLevels.level(pet);
    }
}
