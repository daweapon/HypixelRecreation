package dev.daweapon.type.skyblockgeneric.levels;

import lombok.NonNull;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AccessoryComponent;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.levels.causes.*;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.museum.MuseumCatalogue;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.museum.MuseumMilestones;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoulExchangeLevels;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SkyBlockLevelCause {
    /**
     * Highest level a {@link LevelCause} is registered for. Keep in step with the last entry in
     * {@code configuration/skyblock/levels/skyblock_level_rewards.yml}.
     */
    public static final int HIGHEST_LEVEL_CAUSE = 700;

    private static final Map<String, SkyBlockLevelCauseAbstr> CAUSES = new HashMap<>();
    /**
     * Reverse lookup for {@link #getKey}. Causes are compared by identity, matching the
     * original linear scan, but without rescanning every registered cause on each call — this
     * runs once per cause on every profile save, so a linear scan made saving quadratic.
     */
    private static final Map<SkyBlockLevelCauseAbstr, String> KEYS_BY_CAUSE = new IdentityHashMap<>();

    public static void initializeCauses() {
        // Register all Skill causes
        for (SkillCategories category : SkillCategories.values()) {
            for (int i = 1; i <= category.asCategory().getHighestLevel(); i++) {
                CAUSES.put("skill-" + category.name() + "-" + i, new SkillLevelCause(category, i));
            }
        }

        // Register all accessory causes
        for (ItemType itemTypeLinker : ItemType.values()) {
            if (new SkyBlockItem(itemTypeLinker).hasComponent(AccessoryComponent.class)) {
                CAUSES.put("accessory-" + itemTypeLinker.name(), new NewAccessoryLevelCause(itemTypeLinker));
            }
        }

        // Register all level causes. Must cover every level defined in
        // skyblock_level_rewards.yml, otherwise levelling past the ceiling hands
        // ActionChangeHypixelXP a null cause.
        for (int i = 0; i <= HIGHEST_LEVEL_CAUSE; i++) {
            CAUSES.put("level-" + i, new LevelCause(i));
        }

        // Register all collection causes
        for (ItemType itemTypeLinker : ItemType.values()) {
            CollectionCategory category = CollectionCategories.getCategory(itemTypeLinker);
            if (category == null) continue;

            for (CollectionCategory.ItemCollection collection : category.getCollections()) {
                for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
                    CAUSES.put("collection-" + itemTypeLinker.name() + "-" + collection.getPlacementOf(reward),
                            new CollectionLevelCause(itemTypeLinker, collection.getPlacementOf(reward)));
                }
            }
        }

        // Register all Fairy Exchange rewards
        for (int i = 0; i <= FairySoulExchangeLevels.values().length; i++) {
            CAUSES.put("fairy-soul-exchange-" + i, new FairySoulExchangeLevelCause(i));
        }

        // Register all collected mob type rewards
        for (MobType mobType : MobType.values()) {
            CAUSES.put("mob-type-" + mobType.name(), new CollectedMobTypesCause(mobType.name()));
        }
        CAUSES.put("mob-type-" + CollectedMobTypesCause.ALL_TYPES, new CollectedMobTypesCause(CollectedMobTypesCause.ALL_TYPES));

        // Register all Mission rewards
        for (String missionID : MissionData.getAllMissionIDs()) {
            CAUSES.put("mission-" + missionID, new MissionLevelCause(missionID));
        }

        for (MuseumEntry entry : MuseumCatalogue.get().progressEntries()) {
            CAUSES.put("museum-" + entry.id(), new MuseumLevelCause(entry));
        }
        for (int milestone = 1; milestone <= MuseumMilestones.MAX_MILESTONE; milestone++) {
            CAUSES.put("museum-milestone-" + milestone, new MuseumMilestoneCause(milestone));
        }
    }

    public static Map<String, SkyBlockLevelCauseAbstr> getCauses() {
        return CAUSES;
    }

    public static Double getTotalXP() {
        double total = 0;
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            total += cause.xpReward();
        }
        return total;
    }

    public static FairySoulExchangeLevelCause getFairySoulExchangeCause(int exchangeCount) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof FairySoulExchangeLevelCause exchangeCause) {
                if (exchangeCause.getExchangeCount() == exchangeCount) {
                    return exchangeCause;
                }
            }
        }
        return null;
    }

    public static MissionLevelCause getMissionCause(String missionKey) {
        for (String key : CAUSES.keySet()) {
            if (CAUSES.get(key) instanceof MissionLevelCause missionCause) {
                if (missionCause.getMissionKey().equals(missionKey)) {
                    return missionCause;
                }
            }
        }
        return null;
    }

    public static @Nullable MuseumLevelCause getMuseumCause(MuseumEntry entry) {
        return CAUSES.get("museum-" + entry.id()) instanceof MuseumLevelCause cause ? cause : null;
    }

    public static @Nullable MuseumMilestoneCause getMuseumMilestoneCause(int milestone) {
        return CAUSES.get("museum-milestone-" + milestone) instanceof MuseumMilestoneCause cause ? cause : null;
    }

    public static CollectionLevelCause getCollectionCause(ItemType itemTypeLinker, int level) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof CollectionLevelCause collectionCause) {
                if (collectionCause.getType() == itemTypeLinker && collectionCause.getLevel() == level) {
                    return collectionCause;
                }
            }
        }
        return null;
    }

    public static int getAmountOfCauses() {
        return CAUSES.size();
    }

    public static LevelCause getLevelCause(int level) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof LevelCause levelCause) {
                if (levelCause.getLevel() == level) {
                    return levelCause;
                }
            }
        }
        return null;
    }

    public static NewAccessoryLevelCause getAccessoryCause(ItemType itemTypeLinker) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof NewAccessoryLevelCause accessoryCause) {
                if (accessoryCause.itemTypeLinker == itemTypeLinker) {
                    return accessoryCause;
                }
            }
        }
        return null;
    }

    public static SkillLevelCause getSkillCause(SkillCategories category, int level) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof SkillLevelCause skillCause) {
                if (skillCause.getCategory() == category && skillCause.getLevel() == level) {
                    return skillCause;
                }
            }
        }
        return null;
    }

    public static @Nullable SkyBlockLevelCauseAbstr getCause(String key) {
        return CAUSES.get(key);
    }

    public static String getKey(@NonNull SkyBlockLevelCauseAbstr cause) {
        if (KEYS_BY_CAUSE.isEmpty() && !CAUSES.isEmpty()) {
            CAUSES.forEach((key, value) -> KEYS_BY_CAUSE.put(value, key));
        }

        String key = KEYS_BY_CAUSE.get(cause);
        if (key == null) {
            throw new IllegalArgumentException("Cause not found " + cause.getClass().getSimpleName());
        }
        return key;
    }

    public static List<SkyBlockLevelCauseAbstr> getSkillCauses(SkillCategories category, int levelUpToInclusive) {
        List<SkyBlockLevelCauseAbstr> causes = new ArrayList<>();
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof SkillLevelCause skillCause) {
                if (skillCause.getCategory() == category && skillCause.getLevel() <= levelUpToInclusive) {
                    causes.add(skillCause);
                }
            }
        }
        return causes;
    }
}
