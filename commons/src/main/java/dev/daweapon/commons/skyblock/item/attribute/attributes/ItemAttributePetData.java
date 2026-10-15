package dev.daweapon.commons.skyblock.item.attribute.attributes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

// Item attribute storing a pet's experience and computing its level from Hypixel's pet XP table.
public class ItemAttributePetData extends ItemAttribute<ItemAttributePetData.PetData> {
    @Override
    public String getKey() {
        return "pet_data";
    }

    @Override
    public PetData getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return new PetData(0);
    }

    @Override
    public PetData loadFromString(String string) {
        if (string.isEmpty()) {
            return new PetData(0);
        }

        return new PetData(Double.parseDouble(string));
    }

    @Override
    public String saveIntoString() {
        return String.valueOf(this.value.experience);
    }

    @AllArgsConstructor
    @Getter
    public static class PetData {
        private double experience;

        public boolean addExperience(double experience, Rarity rarity) {
            return addExperience(experience, rarityOffset(rarity), MAX_LEVEL);
        }

        public boolean addExperience(double experience, int offset, int maxLevel) {
            int currentLevel = getAsLevel(offset, maxLevel);
            this.experience += experience;
            return getAsLevel(offset, maxLevel) > currentLevel;
        }

        public void setExperience(double experience) {
            this.experience = Math.max(0, experience);
        }

        public void setLevel(int level, Rarity rarity) {
            setLevel(level, rarityOffset(rarity), MAX_LEVEL);
        }

        public void setLevel(int level, int offset, int maxLevel) {
            this.experience = totalExperienceForLevel(Math.clamp(level, 1, maxLevel), offset);
        }

        public Integer getAsLevel(Rarity rarity) {
            return getAsLevel(rarityOffset(rarity), MAX_LEVEL);
        }

        public int getAsLevel(int offset, int maxLevel) {
            int level = 1;
            while (level < maxLevel && experience >= totalExperienceForLevel(level + 1, offset)) level++;
            return level;
        }

        public double getExperienceInCurrentLevel(Rarity rarity) {
            return getExperienceInCurrentLevel(rarityOffset(rarity), MAX_LEVEL);
        }

        public double getExperienceInCurrentLevel(int offset, int maxLevel) {
            return experience - totalExperienceForLevel(getAsLevel(offset, maxLevel), offset);
        }

        public long getExperienceForLevel(int level, Rarity rarity) {
            return totalExperienceForLevel(level, rarityOffset(rarity));
        }
    }

    public static final int MAX_LEVEL = 100;
    public static final int HIGHEST_MAX_LEVEL = 200;

    private static final long[] PET_LEVELS = {
            100, 110, 120, 130, 145, 160, 175, 190, 210, 230, 250, 275, 300, 330, 360, 400, 440, 490, 540, 600,
            660, 730, 800, 880, 960, 1050, 1150, 1260, 1380, 1510, 1650, 1800, 1960, 2130, 2310, 2500, 2700, 2920,
            3160, 3420, 3700, 4000, 4350, 4750, 5200, 5700, 6300, 7000, 7800, 8700, 9700, 10800, 12000, 13300,
            14700, 16200, 17800, 19500, 21300, 23200, 25200, 27400, 29800, 32400, 35200, 38200, 41400, 44800,
            48400, 52200, 56200, 60400, 64800, 69400, 74200, 79200, 84700, 90700, 97200, 104200, 111700, 119700,
            128200, 137200, 146700, 156700, 167700, 179700, 192700, 206700, 221700, 237700, 254700, 272700,
            291700, 311700, 333700, 357700, 383700, 411700, 441700, 476700, 516700, 561700, 611700, 666700,
            726700, 791700, 861700, 936700, 1016700, 1101700, 1191700, 1286700, 1386700, 1496700, 1616700,
            1746700, 1886700};
    private static final long LEVEL_200_STEP = 1_886_700;

    public static int rarityOffset(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0;
            case UNCOMMON -> 6;
            case RARE -> 11;
            case EPIC, SPECIAL -> 16;
            default -> 20;
        };
    }

    public static long experienceToLevelUp(int fromLevel, int offset) {
        int index = offset + fromLevel - 1;
        return index < PET_LEVELS.length ? PET_LEVELS[index] : LEVEL_200_STEP;
    }

    public static long totalExperienceForLevel(int level, int offset) {
        if (offset >= 0 && offset < CUMULATIVE.length && level >= 1 && level <= HIGHEST_MAX_LEVEL) {
            return CUMULATIVE[offset][level];
        }
        long total = 0;
        for (int from = 1; from < level; from++) total += experienceToLevelUp(from, offset);
        return total;
    }

    private static final long[][] CUMULATIVE = new long[21][HIGHEST_MAX_LEVEL + 1];

    static {
        for (int offset = 0; offset < CUMULATIVE.length; offset++) {
            for (int level = 2; level <= HIGHEST_MAX_LEVEL; level++) {
                CUMULATIVE[offset][level] = CUMULATIVE[offset][level - 1] + experienceToLevelUp(level - 1, offset);
            }
        }
    }
}
