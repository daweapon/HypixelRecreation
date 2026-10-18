package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Per-item collection XP from the wiki's Module:Minion/Data (drops.none and drops.sc3000 exp fields).
public final class MinionCollectionXp {
    public record Reward(SkillCategories skill, double xpPerItem) {
    }

    private static final String TABLE = """
            WHEAT FARMING 0.2
            SEEDS FARMING 0.1
            ENCHANTED_WHEAT FARMING 32
            ENCHANTED_HAY_BALE FARMING 5120
            ENCHANTED_SEEDS FARMING 16
            BOX_OF_SEEDS FARMING 2560
            SUNFLOWER FARMING 0.3
            MOONFLOWER FARMING 0.3
            ENCHANTED_SUNFLOWER FARMING 48
            ENCHANTED_MOONFLOWER FARMING 48
            COMPACTED_SUNFLOWER FARMING 7680
            COMPACTED_MOONFLOWER FARMING 7680
            CARROT FARMING 0.1
            ENCHANTED_CARROT FARMING 16
            ENCHANTED_GOLDEN_CARROT FARMING 2560
            POTATO FARMING 0.1
            ENCHANTED_POTATO FARMING 16
            ENCHANTED_BAKED_POTATO FARMING 2560
            PUMPKIN FARMING 0.3
            ENCHANTED_PUMPKIN FARMING 48
            POLISHED_PUMPKIN FARMING 7680
            MELON_SLICE FARMING 0.1
            MELON_BLOCK FARMING 0.9
            ENCHANTED_MELON FARMING 16
            ENCHANTED_MELON_BLOCK FARMING 2560
            RED_MUSHROOM FARMING 0.3
            BROWN_MUSHROOM FARMING 0.3
            RED_MUSHROOM_BLOCK FARMING 0.3
            BROWN_MUSHROOM_BLOCK FARMING 0.3
            ENCHANTED_RED_MUSHROOM FARMING 48
            ENCHANTED_BROWN_MUSHROOM FARMING 48
            ENCHANTED_RED_MUSHROOM_BLOCK FARMING 1536
            ENCHANTED_BROWN_MUSHROOM_BLOCK FARMING 1536
            COCOA_BEANS FARMING 0.2
            ENCHANTED_COCOA_BEANS FARMING 32
            ENCHANTED_COOKIE FARMING 5120
            CACTUS FARMING 0.2
            CACTUS_GREEN FARMING 0.2
            ENCHANTED_CACTUS_GREEN FARMING 80
            ENCHANTED_CACTUS FARMING 12800
            SUGAR_CANE FARMING 0.1
            ENCHANTED_SUGAR ALCHEMY 16
            ENCHANTED_SUGAR_CANE FARMING 2560
            RAW_CHICKEN FARMING 0.1
            FEATHER FARMING 0.2
            EGG FARMING 0.2
            ENCHANTED_RAW_CHICKEN FARMING 16
            ENCHANTED_FEATHER FARMING 32
            ENCHANTED_EGG FARMING 115
            SUPER_ENCHANTED_EGG FARMING 16560
            OMEGA_ENCHANTED_EGG FARMING 149040
            RAW_BEEF FARMING 0.1
            LEATHER FARMING 0.2
            ENCHANTED_RAW_BEEF FARMING 16
            ENCHANTED_LEATHER FARMING 115
            RAW_PORKCHOP FARMING 0.2
            ENCHANTED_PORK FARMING 32
            ENCHANTED_GRILLED_PORK FARMING 5120
            MUTTON FARMING 0.1
            WHITE_WOOL FARMING 0.1
            ENCHANTED_MUTTON FARMING 16
            ENCHANTED_COOKED_MUTTON FARMING 2560
            ENCHANTED_WOOL FARMING 16
            RAW_RABBIT FARMING 0.1
            RABBIT FARMING 0.2
            RABBIT_HIDE FARMING 0.2
            ENCHANTED_RAW_RABBIT FARMING 16
            ENCHANTED_COOKED_RABBIT FARMING 2560
            ENCHANTED_RABBIT_FOOT FARMING 32
            ENCHANTED_RABBIT_HIDE FARMING 115
            NETHER_WART FARMING 0.2
            ENCHANTED_NETHER_WART FARMING 32
            MUTANT_NETHER_WART FARMING 5120
            COBBLESTONE MINING 0.1
            STONE MINING 0.1
            ENCHANTED_COBBLESTONE MINING 16
            COAL MINING 0.3
            COAL_BLOCK MINING 2.7
            ENCHANTED_COAL MINING 48
            ENCHANTED_COAL_BLOCK MINING 7680
            IRON_ORE MINING 0.3
            IRON_INGOT MINING 0.3
            IRON_BLOCK MINING 2.7
            ENCHANTED_IRON_INGOT MINING 48
            ENCHANTED_IRON_BLOCK MINING 7680
            GOLD_ORE MINING 0.4
            GOLD_INGOT MINING 0.4
            GOLD_BLOCK MINING 3.6
            ENCHANTED_GOLD_INGOT MINING 64
            ENCHANTED_GOLD_BLOCK MINING 10240
            DIAMOND MINING 0.4
            DIAMOND_BLOCK MINING 3.6
            ENCHANTED_DIAMOND MINING 64
            ENCHANTED_DIAMOND_BLOCK MINING 10240
            LAPIS_LAZULI MINING 0.1
            LAPIS_LAZULI_BLOCK MINING 0.9
            ENCHANTED_LAPIS_LAZULI MINING 16
            ENCHANTED_LAPIS_LAZULI_BLOCK MINING 2560
            EMERALD MINING 0.4
            EMERALD_BLOCK MINING 3.6
            ENCHANTED_EMERALD MINING 64
            ENCHANTED_EMERALD_BLOCK MINING 10240
            REDSTONE MINING 0.2
            REDSTONE_BLOCK MINING 1.8
            ENCHANTED_REDSTONE MINING 32
            ENCHANTED_REDSTONE_BLOCK MINING 5120
            QUARTZ MINING 0.3
            QUARTZ_BLOCK MINING 1.2
            ENCHANTED_QUARTZ MINING 48
            ENCHANTED_QUARTZ_BLOCK MINING 7680
            OBSIDIAN MINING 0.4
            ENCHANTED_OBSIDIAN MINING 64
            GLOWSTONE_DUST MINING 0.2
            GLOWSTONE MINING 0.8
            ENCHANTED_GLOWSTONE_DUST MINING 32
            ENCHANTED_GLOWSTONE MINING 6144
            GRAVEL MINING 0.2
            FLINT MINING 0.2
            ENCHANTED_FLINT MINING 32
            ICE MINING 0.5
            PACKED_ICE MINING 4.5
            ENCHANTED_ICE MINING 80
            ENCHANTED_PACKED_ICE MINING 12800
            SAND MINING 0.2
            ENCHANTED_SAND MINING 32
            END_STONE MINING 0.4
            ENCHANTED_END_STONE MINING 64
            MITHRIL MINING 0.4
            ENCHANTED_MITHRIL MINING 64
            HARD_STONE MINING 0.1
            ENCHANTED_HARD_STONE MINING 57.6
            CONCENTRATED_STONE MINING 33177.6
            MYCELIUM MINING 0.2
            ENCHANTED_MYCELIUM MINING 32
            ENCHANTED_MYCELIUM_CUBE MINING 5120
            RED_SAND MINING 0.2
            ENCHANTED_RED_SAND MINING 32
            ENCHANTED_RED_SAND_CUBE MINING 5120
            SNOWBALL MINING 0.1
            SNOW_BLOCK MINING 0.4
            ENCHANTED_SNOW_BLOCK MINING 64
            ROTTEN_FLESH COMBAT 0.3
            POISONOUS_POTATO FARMING 0.1
            ENCHANTED_ROTTEN_FLESH COMBAT 48
            ENCHANTED_POISONOUS_POTATO FARMING 16
            BONE COMBAT 0.2
            ENCHANTED_BONE COMBAT 32
            STRING COMBAT 0.2
            SPIDER_EYE COMBAT 0.3
            ENCHANTED_STRING COMBAT 38
            ENCHANTED_SPIDER_EYE COMBAT 48
            GUNPOWDER COMBAT 0.3
            ENCHANTED_GUNPOWDER COMBAT 48
            ENDER_PEARL COMBAT 0.3
            ENCHANTED_ENDER_PEARL COMBAT 9
            ABSOLUTE_ENDER_PEARL COMBAT 720
            GHAST_TEAR COMBAT 0.5
            ENCHANTED_GHAST_TEAR COMBAT 7.5
            SILVER_FANG COMBAT 187.5
            SLIME_BALL COMBAT 0.2
            SLIME_BLOCK COMBAT 1.8
            ENCHANTED_SLIME_BALL COMBAT 32
            ENCHANTED_SLIME_BLOCK COMBAT 5120
            BLAZE_ROD COMBAT 0.3
            ENCHANTED_BLAZE_POWDER COMBAT 48
            ENCHANTED_BLAZE_ROD COMBAT 7680
            MAGMA_CREAM COMBAT 0.2
            ENCHANTED_MAGMA_CREAM COMBAT 32
            WHIPPED_MAGMA_CREAM COMBAT 5120
            HEMOVIBE COMBAT 5
            HEMOGLASS COMBAT 800
            OAK_LOG FORAGING 0.1
            ENCHANTED_OAK_LOG FORAGING 16
            SPRUCE_LOG FORAGING 0.1
            ENCHANTED_SPRUCE_LOG FORAGING 16
            BIRCH_LOG FORAGING 0.1
            ENCHANTED_BIRCH_LOG FORAGING 16
            DARK_OAK_LOG FORAGING 0.1
            ENCHANTED_DARK_OAK_LOG FORAGING 16
            ACACIA_LOG FORAGING 0.1
            ENCHANTED_ACACIA_LOG FORAGING 16
            JUNGLE_LOG FORAGING 0.1
            ENCHANTED_JUNGLE_LOG FORAGING 16
            DANDELION FORAGING 0.1
            POPPY FORAGING 0.1
            ALLIUM FORAGING 0.1
            AZURE_BLUET FORAGING 0.1
            BLUE_ORCHID FORAGING 0.1
            OXEYE_DAISY FORAGING 0.1
            RED_TULIP FORAGING 0.1
            ORANGE_TULIP FORAGING 0.1
            WHITE_TULIP FORAGING 0.1
            PINK_TULIP FORAGING 0.1
            LILAC FORAGING 0.2
            PEONY FORAGING 0.2
            ROSE_BUSH FORAGING 0.2
            ENCHANTED_DANDELION FORAGING 16
            ENCHANTED_POPPY FORAGING 57.6
            WILD_ROSE FARMING 0.3
            ENCHANTED_WILD_ROSE FARMING 48
            COMPACTED_WILD_ROSE FARMING 7680
            RAW_FISH FISHING 0.5
            RAW_SALMON FISHING 0.7
            PUFFERFISH FISHING 1
            TROPICAL_FISH FISHING 2
            PRISMARINE_CRYSTALS FISHING 0.5
            PRISMARINE_SHARD FISHING 0.5
            SPONGE FISHING 0.5
            ENCHANTED_RAW_FISH FISHING 80
            ENCHANTED_COOKED_FISH FISHING 12800
            ENCHANTED_RAW_SALMON FISHING 112
            ENCHANTED_COOKED_SALMON FISHING 17920
            ENCHANTED_PUFFERFISH FISHING 160
            ENCHANTED_TROPICAL_FISH FISHING 320
            ENCHANTED_PRISMARINE_CRYSTALS FISHING 40
            ENCHANTED_PRISMARINE_SHARD FISHING 40
            ENCHANTED_SPONGE FISHING 20
            ENCHANTED_WET_SPONGE FISHING 800
            CLAY_BALL FISHING 0.1
            CLAY_BALL FISHING 0.4
            ENCHANTED_CLAY_BALL FISHING 16
            ENCHANTED_CLAY_BLOCK FISHING 2560
            LILY_PAD FISHING 0.8
            ENCHANTED_LILY_PAD FISHING 128
            CONDENSED_LILY_PAD FISHING 20480
            """;

    private static final Map<ItemType, Reward> REWARDS = new HashMap<>();

    static {
        for (String line : TABLE.strip().split("\\R")) {
            String[] parts = line.trim().split(" ");
            ItemType type = ItemType.get(parts[0]);
            if (type != null) REWARDS.put(type, new Reward(SkillCategories.valueOf(parts[1]), Double.parseDouble(parts[2])));
        }
    }

    private MinionCollectionXp() {
    }

    public static Reward rewardFor(ItemType type) {
        return type == null ? null : REWARDS.get(type);
    }

    public static void award(SkyBlockPlayer player, List<ItemQuantifiable> collected) {
        Map<SkillCategories, Double> totals = new EnumMap<>(SkillCategories.class);
        for (ItemQuantifiable entry : collected) {
            Reward reward = rewardFor(entry.getItem().getAttributeHandler().getPotentialType());
            if (reward == null) continue;
            totals.merge(reward.skill(), reward.xpPerItem() * entry.getAmount(), Double::sum);
        }
        totals.forEach((skill, xp) -> player.getSkills().increase(player, skill, xp));
    }
}
