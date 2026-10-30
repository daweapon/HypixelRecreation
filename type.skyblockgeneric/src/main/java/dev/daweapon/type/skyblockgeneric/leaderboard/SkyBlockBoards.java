package dev.daweapon.type.skyblockgeneric.leaderboard;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;
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
    private static final List<String> WINDOWED_SOURCES = List.of(LEVEL, "skill_total", "slayer_total");
    private static final List<SkyBlockBoard> WINDOWED = buildWindowed();

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
        boards.add(new SkyBlockBoard("hotm_xp", "Heart of the Mountain XP", BoardCategory.PROGRESSION, Material.DIAMOND_PICKAXE, "XP",
                player -> HotmService.data(player).getExperience()));
        boards.add(new SkyBlockBoard("hotf_xp", "Heart of the Forest XP", BoardCategory.PROGRESSION, Material.JUNGLE_LOG, "XP",
                player -> HotfService.experience(player)));
        boards.add(new SkyBlockBoard("garden_xp", "Garden XP", BoardCategory.PROGRESSION, Material.WHEAT, "XP",
                player -> GardenService.profile(player).getGardenXp()));
        boards.add(new SkyBlockBoard("pet_score", "Pet Score", BoardCategory.PROGRESSION, Material.BONE, "score",
                player -> PetScore.of(player)));
        boards.add(new SkyBlockBoard("museum_donations", "Museum Donations", BoardCategory.ACTIVITIES, Material.ITEM_FRAME, "donated",
                player -> player.getMuseumData().getDonatedEntries().size()));
        boards.add(new SkyBlockBoard("bestiary_kills", "Bestiary Kills", BoardCategory.ACTIVITIES, Material.WRITTEN_BOOK, "kills",
                player -> player.getBestiaryData().getMobs().values().stream().mapToLong(Integer::longValue).sum()));
        boards.add(new SkyBlockBoard("trophy_fish", "Trophy Fish Caught", BoardCategory.ACTIVITIES, Material.COD, "caught",
                player -> player.getTrophyFishData().getFish().values().stream().mapToLong(fish -> fish.getTotalCatches()).sum()));
        boards.add(new SkyBlockBoard("kuudra_total", "Kuudra Completions", BoardCategory.ACTIVITIES, Material.MAGMA_BLOCK, "clears",
                player -> totalKuudraClears(player)));
        for (KuudraTier tier : KuudraTier.values()) {
            boards.add(new SkyBlockBoard("kuudra:" + tier.name(), tier.getDisplayName() + " Kuudra Completions", BoardCategory.ACTIVITIES,
                    Material.MAGMA_CREAM, "clears", player -> KuudraProgress.clears(player, tier)));
        }
        return List.copyOf(boards);
    }

    private static List<SkyBlockBoard> buildWindowed() {
        List<SkyBlockBoard> boards = new ArrayList<>();
        for (Timeframe timeframe : Timeframe.values()) {
            BoardCategory category = timeframe == Timeframe.WEEK ? BoardCategory.WEEKLY : BoardCategory.MONTHLY;
            for (String sourceId : WINDOWED_SOURCES) {
                SkyBlockBoard source = fixed(sourceId).orElseThrow();
                boards.add(new SkyBlockBoard(timeframe.getPrefix() + ":" + sourceId, source.name() + " Gained", category,
                        source.icon(), source.unit(), source.score(), timeframe));
            }
        }
        return List.copyOf(boards);
    }

    private static double totalKuudraClears(SkyBlockPlayer player) {
        double total = 0;
        for (KuudraTier tier : KuudraTier.values()) total += KuudraProgress.clears(player, tier);
        return total;
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

    public static List<SkyBlockBoard> windowed() {
        return WINDOWED;
    }

    public static List<SkyBlockBoard> inCategory(BoardCategory category) {
        List<SkyBlockBoard> boards = new ArrayList<>(FIXED.stream().filter(board -> board.category() == category).toList());
        boards.addAll(WINDOWED.stream().filter(board -> board.category() == category).toList());
        return boards;
    }

    public static Map<String, Double> windowedScoresOf(SkyBlockPlayer player, Map<String, Double> baselines) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (SkyBlockBoard board : WINDOWED) {
            double gained = Timeframe.gained(board.score().applyAsDouble(player), baselines.getOrDefault(board.id(), 0D));
            if (gained > 0) scores.put(board.key(), gained);
        }
        return scores;
    }

    public static Map<String, Double> windowedCurrentScores(SkyBlockPlayer player, Timeframe timeframe) {
        Map<String, Double> current = new LinkedHashMap<>();
        for (SkyBlockBoard board : WINDOWED) {
            if (board.timeframe() == timeframe) current.put(board.id(), board.score().applyAsDouble(player));
        }
        return current;
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
        return fixed(id).or(() -> WINDOWED.stream().filter(board -> board.id().equals(id)).findFirst());
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
