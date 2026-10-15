package dev.daweapon.type.skyblockgeneric.crimson.quest;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.faction.ReputationTier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The five Faction Quest tiers with their guaranteed rewards and the two item pools a claim picks one entry
 * from (Fishing quests use their own pool). Rewards follow the current wiki, including its x1 amounts for the
 * A and S pools where the archived official wiki listed x2.
 */
@Getter
public enum FactionQuestTier {
    D("Ⓓ", "<f>", "Common", 2_000, 30, 20,
            List.of(reward("BLAZE_ASHES", 5), reward("BURNING_EYE", 5), reward("DIGESTED_MUSHROOMS", 5),
                    reward("KADA_LEAD", 5), reward("MAGMAG", 5), reward("SPECTRE_DUST", 5), reward("WITHER_SOUL", 5)),
            List.of(reward("LUMP_OF_MAGMA", 2), reward("MOOGMA_PELT", 1), reward("PYROCLASTIC_SCALE", 1))),
    C("Ⓒ", "<a>", "Uncommon", 5_000, 50, 30,
            List.of(reward("BEZOS", 1), reward("BLAZE_ASHES", 10), reward("BURNING_EYE", 10),
                    reward("DIGESTED_MUSHROOMS", 10), reward("GAZING_PEARL", 1), reward("KADA_LEAD", 10),
                    reward("MAGMAG", 10), reward("MUTATED_BLAZE_ASHES", 2), reward("SPECTRE_DUST", 10),
                    reward("TENTACLE_MEAT", 3), reward("WITHER_SOUL", 10)),
            List.of(reward("CUP_OF_BLOOD", 1), reward("FLAMING_HEART", 1), reward("LUMP_OF_MAGMA", 4),
                    reward("MOOGMA_PELT", 2), reward("ORB_OF_ENERGY", 1), reward("PYROCLASTIC_SCALE", 2))),
    B("Ⓑ", "<9>", "Rare", 10_000, 75, 40,
            List.of(reward("BEZOS", 2), reward("GAZING_PEARL", 2), reward("MUTATED_BLAZE_ASHES", 3),
                    reward("TENTACLE_MEAT", 5)),
            List.of(reward("CUP_OF_BLOOD", 1), reward("FLAMING_HEART", 1), reward("ORB_OF_ENERGY", 1))),
    A("Ⓐ", "<5>", "Epic", 15_000, 100, 50,
            List.of(reward("BEZOS", 3), reward("COMPACT_OOZE", 1), reward("CORRUPTED_FRAGMENT", 5),
                    reward("GAZING_PEARL", 3), reward("HALLOWED_SKULL", 1), reward("LEATHER_CLOTH", 1),
                    reward("LUMINO_FIBER", 1), reward("MUTATED_BLAZE_ASHES", 4), reward("SPELL_POWDER", 1),
                    reward("TENTACLE_MEAT", 7)),
            List.of(reward("HORN_OF_TAURUS", 1), reward("CUP_OF_BLOOD", 1), reward("FLAMING_HEART", 1),
                    reward("ORB_OF_ENERGY", 1))),
    S("Ⓢ", "<6>", "Legendary", 20_000, 125, 60,
            List.of(reward("COMPACT_OOZE", 2), reward("CORRUPTED_FRAGMENT", 10), reward("HALLOWED_SKULL", 1),
                    reward("LEATHER_CLOTH", 1), reward("LUMINO_FIBER", 1), reward("SPELL_POWDER", 1)),
            List.of(reward("HORN_OF_TAURUS", 1), reward("ORB_OF_ENERGY", 1))),
    ;

    private final String glyph;
    private final String colour;
    private final String rarityName;
    private final int combatXP;
    private final int reputation;
    private final int bits;
    private final List<RewardItem> itemPool;
    private final List<RewardItem> fishingPool;

    FactionQuestTier(String glyph, String colour, String rarityName, int combatXP, int reputation, int bits,
                     List<RewardItem> itemPool, List<RewardItem> fishingPool) {
        this.glyph = glyph;
        this.colour = colour;
        this.rarityName = rarityName;
        this.combatXP = combatXP;
        this.reputation = reputation;
        this.bits = bits;
        this.itemPool = itemPool;
        this.fishingPool = fishingPool;
    }

    public String coloured() {
        return colour + glyph;
    }

    public List<RewardItem> pool(FactionQuestType type) {
        return type == FactionQuestType.FISHING ? fishingPool : itemPool;
    }

    /** The day's five tiers at a reputation title (archived official wiki distribution table). */
    public static List<FactionQuestTier> mixFor(ReputationTier title) {
        return switch (title) {
            case NEUTRAL -> List.of(D, D, C, C, C);
            case FRIENDLY, TRUSTED -> List.of(D, C, C, B, A);
            case HONORED -> List.of(C, C, B, B, A);
            case HERO -> List.of(C, C, B, A, S);
        };
    }

    private static RewardItem reward(String itemId, int amount) {
        return new RewardItem(itemId, amount);
    }

    public record RewardItem(String itemId, int amount) {
        public @Nullable ItemType type() {
            return ItemType.get(itemId);
        }

        public String displayName() {
            ItemType type = type();
            return type == null ? itemId : type.getDisplayName();
        }
    }
}
