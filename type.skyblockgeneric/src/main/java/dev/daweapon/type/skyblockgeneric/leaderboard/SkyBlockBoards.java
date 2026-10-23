package dev.daweapon.type.skyblockgeneric.leaderboard;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SkyBlockBoards {
    public static final String LEVEL = "level";
    public static final String COLLECTION_PREFIX = "collection:";
    private static final List<SkyBlockBoard> FIXED = build();

    private SkyBlockBoards() {
    }

    private static List<SkyBlockBoard> build() {
        List<SkyBlockBoard> boards = new ArrayList<>();
        boards.add(new SkyBlockBoard(LEVEL, "SkyBlock Level", BoardCategory.GENERAL, Material.NETHER_STAR, "XP",
                player -> player.getSkyBlockExperience().getTotalXP()));
        boards.add(new SkyBlockBoard("fairy_souls", "Fairy Souls", BoardCategory.GENERAL, Material.PLAYER_HEAD, "souls",
                player -> player.getFairySouls().getAllFairySouls().size()));
        for (SkillCategories skill : SkillCategories.values()) {
            if (skill.isCosmetic()) continue;
            boards.add(new SkyBlockBoard("skill:" + skill.name(), StringUtility.toNormalCase(skill.name()) + " XP",
                    BoardCategory.SKILLS, skillIcon(skill), "XP", player -> player.getSkills().getRaw(skill)));
        }
        boards.add(new SkyBlockBoard("skill_total", "Total Skill XP", BoardCategory.SKILLS, Material.EXPERIENCE_BOTTLE, "XP",
                player -> totalSkillXp(player)));
        for (SlayerType type : SlayerType.values()) {
            boards.add(new SkyBlockBoard(slayerId(type), type.displayName() + " XP", BoardCategory.SLAYERS, type.menuMaterial(), "XP",
                    player -> slayerXp(player, type)));
        }
        boards.add(new SkyBlockBoard("slayer_total", "Total Slayer XP", BoardCategory.SLAYERS, Material.EXPERIENCE_BOTTLE, "XP",
                player -> java.util.Arrays.stream(SlayerType.values()).mapToDouble(type -> slayerXp(player, type)).sum()));
        return List.copyOf(boards);
    }

    private static Material skillIcon(SkillCategories skill) {
        return switch (skill) {
            case COMBAT -> Material.IRON_SWORD;
            case FARMING -> Material.GOLDEN_HOE;
            case FISHING -> Material.FISHING_ROD;
            case MINING -> Material.STONE_PICKAXE;
            case FORAGING -> Material.JUNGLE_SAPLING;
            case ENCHANTING -> Material.ENCHANTING_TABLE;
            case ALCHEMY -> Material.BREWING_STAND;
            case TAMING -> Material.BONE;
            case CARPENTRY -> Material.CRAFTING_TABLE;
            case RUNECRAFTING -> Material.MAGMA_CREAM;
            default -> Material.BOOK;
        };
    }

    private static double totalSkillXp(SkyBlockPlayer player) {
        double total = 0;
        for (SkillCategories skill : SkillCategories.values()) {
            if (!skill.isCosmetic()) total += player.getSkills().getRaw(skill);
        }
        return total;
    }

    private static double slayerXp(SkyBlockPlayer player, SlayerType type) {
        DatapointSlayer.SlayerProgress progress = player.getSlayerData().getProgress().get(type);
        return progress == null ? 0 : progress.getXp();
    }

    public static String slayerId(SlayerType type) {
        return "slayer:" + type.name();
    }

    public static List<SkyBlockBoard> fixed() {
        return FIXED;
    }

    public static Optional<SkyBlockBoard> fixed(String id) {
        return FIXED.stream().filter(board -> board.id().equals(id)).findFirst();
    }

    public static SkyBlockBoard collection(ItemType item) {
        return new SkyBlockBoard(COLLECTION_PREFIX + item.name(), StringUtility.toNormalCase(item.name()) + " Collection",
                BoardCategory.COLLECTIONS, Material.PAINTING, "collected", player -> player.getCollection().get(item));
    }

    public static Optional<SkyBlockBoard> byId(String id) {
        if (id.startsWith(COLLECTION_PREFIX)) {
            try {
                return Optional.of(collection(ItemType.valueOf(id.substring(COLLECTION_PREFIX.length()))));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }
        return fixed(id);
    }

    public static Map<String, Double> scoresOf(SkyBlockPlayer player) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (SkyBlockBoard board : FIXED) {
            double score = board.score().applyAsDouble(player);
            if (score > 0) scores.put(board.key(), score);
        }
        for (Map.Entry<ItemType, Integer> entry : player.getCollection().getItems().entrySet()) {
            if (entry.getValue() > 0 && CollectionCategories.getCategory(entry.getKey()) != null) {
                scores.put(SkyBlockBoard.KEY_PREFIX + COLLECTION_PREFIX + entry.getKey().name(), entry.getValue().doubleValue());
            }
        }
        return scores;
    }
}
