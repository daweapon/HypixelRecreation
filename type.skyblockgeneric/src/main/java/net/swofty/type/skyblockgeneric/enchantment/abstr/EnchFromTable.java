package net.swofty.type.skyblockgeneric.enchantment.abstr;

import lombok.NonNull;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

// Marks an enchantment as obtainable from the vanilla enchanting table, defining which levels
// it can roll there and the bookshelf power required.
public interface EnchFromTable {

    TableLevels getLevelsFromTableToApply(@NonNull SkyBlockPlayer player);

    int getRequiredBookshelfPower();

    record TableLevels(Map<Integer, Integer> levelsFromTableToApply) {
        public int get(int level) {
            return ElectionPerks.experienceLevelCost(levelsFromTableToApply.get(level));
        }

        public int minimumLevel() {
            return levelsFromTableToApply.keySet().stream().min(Integer::compareTo).orElseThrow();
        }

        public int maximumLevel() {
            return levelsFromTableToApply.keySet().stream().max(Integer::compareTo).orElseThrow();
        }
    }
}
