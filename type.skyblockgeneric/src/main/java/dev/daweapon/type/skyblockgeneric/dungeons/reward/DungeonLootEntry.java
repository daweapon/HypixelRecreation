package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * One row of a floor's chest loot table (wiki "Catacombs Chest Loot Table Entry"): an item, enchanted book,
 * pet or essence with its quality cost, weight and added coin cost. Rows with neither quality nor weight are
 * guaranteed (or, with a {@code chance}, a separate rare roll such as a Dye); a weightless row with quality
 * is the essence filler that spends the leftover quality.
 */
public record DungeonLootEntry(@Nullable String itemId, @Nullable String rarity, @Nullable String enchantment, int level,
                               @Nullable String essence, int amount, int quality, double weight, long cost,
                               double chance) {

    public static DungeonLootEntry parse(JSONObject json) {
        String book = json.optString("book", null);
        return new DungeonLootEntry(
                book != null ? ItemType.ENCHANTED_BOOK.name() : json.optString("item", null),
                json.optString("rarity", null),
                book,
                json.optInt("level", 0),
                json.optString("essence", null),
                json.optInt("amount", 1),
                json.optInt("quality", 0),
                json.optDouble("weight", 0),
                json.optLong("cost", 0),
                json.optDouble("chance", 0));
    }

    public boolean isEssence() {
        return essence != null;
    }

    public boolean isRolled() {
        return weight > 0;
    }

    public boolean isFiller() {
        return weight <= 0 && quality > 0;
    }

    public boolean isGuaranteed() {
        return weight <= 0 && quality <= 0 && chance <= 0;
    }

    public boolean isChanceRoll() {
        return weight <= 0 && quality <= 0 && chance > 0;
    }

    public @Nullable ItemType itemType() {
        return itemId == null || isEssence() ? null : ItemType.get(itemId);
    }

    public DungeonReward reward(int times) {
        if (isEssence()) return DungeonReward.essence(essence, (long) amount * times);
        return new DungeonReward(itemId, amount, enchantment, level, rarity, null, 0);
    }
}
