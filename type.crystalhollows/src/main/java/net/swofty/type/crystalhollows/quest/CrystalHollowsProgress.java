package net.swofty.type.crystalhollows.quest;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.temple.JungleTemple;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CrystalHollowsProgress {
    public static final long KINGS_SCENT_MILLIS = 20L * 60 * 1000;
    public static final int SLUDGE_HITS_TO_FREE = 5;

    public static final String JUNGLE_KEY_ACTIVE = "jungle_key_active";
    public static final String METAL_DETECTOR_GIVEN = "metal_detector_given";
    public static final String JADE_CRYSTAL_CLAIMED = "jade_crystal_claimed";
    public static final String SAPPHIRE_STORAGE_OPEN = "sapphire_storage_open";
    public static final String SAPPHIRE_CRYSTAL_CLAIMED = "sapphire_crystal_claimed";
    public static final String MET_KING_YOLKAR = "met_king_yolkar";
    public static final String SLUDGE_FREED = "sludge_freed";
    public static final String DEN_ACCESS = "goblin_den_access";

    public static final List<ItemType> AUTOMATON_PARTS = List.of(
            ItemType.ELECTRON_TRANSMITTER, ItemType.FTX_3070, ItemType.ROBOTRON_REFLECTOR,
            ItemType.SUPERLITE_MOTOR, ItemType.CONTROL_SWITCH, ItemType.SYNTHETIC_HEART);

    public static final List<ItemType> GOBLIN_EGGS = List.of(
            ItemType.GOBLIN_EGG, ItemType.GREEN_GOBLIN_EGG, ItemType.BLUE_GOBLIN_EGG,
            ItemType.RED_GOBLIN_EGG, ItemType.YELLOW_GOBLIN_EGG);

    private static final Map<UUID, Set<String>> FLAGS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SCENT_EXPIRY = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SLUDGE_HITS = new ConcurrentHashMap<>();

    private CrystalHollowsProgress() {
    }

    public static boolean has(UUID player, String flag) {
        Set<String> flags = FLAGS.get(player);
        return flags != null && flags.contains(flag);
    }

    public static boolean mark(UUID player, String flag) {
        return FLAGS.computeIfAbsent(player, _ -> ConcurrentHashMap.newKeySet()).add(flag);
    }

    public static void clear(UUID player, String flag) {
        Set<String> flags = FLAGS.get(player);
        if (flags != null) flags.remove(flag);
    }

    public static @Nullable ItemType scavengedItem(Keeper keeper) {
        return ItemType.get(keeper.scavengedItemId);
    }

    public static boolean turnedIn(UUID player, Keeper keeper) {
        return has(player, "keeper_" + keeper.name());
    }

    public static void turnIn(UUID player, Keeper keeper) {
        mark(player, "keeper_" + keeper.name());
    }

    public static boolean allKeepersSatisfied(UUID player) {
        for (Keeper keeper : Keeper.values()) {
            if (!turnedIn(player, keeper)) return false;
        }
        return true;
    }

    public static int partsSubmitted(UUID player) {
        int count = 0;
        for (ItemType part : AUTOMATON_PARTS) {
            if (has(player, "part_" + part.name())) count++;
        }
        return count;
    }

    public static boolean partSubmitted(UUID player, ItemType part) {
        return has(player, "part_" + part.name());
    }

    public static void submitPart(UUID player, ItemType part) {
        mark(player, "part_" + part.name());
    }

    public static void startKingsScent(UUID player) {
        SCENT_EXPIRY.put(player, System.currentTimeMillis() + KINGS_SCENT_MILLIS);
    }

    public static boolean hasKingsScent(UUID player) {
        Long expiry = SCENT_EXPIRY.get(player);
        if (expiry == null) return false;
        if (expiry <= System.currentTimeMillis()) {
            SCENT_EXPIRY.remove(player);
            return false;
        }
        return true;
    }

    public static long kingsScentMillisLeft(UUID player) {
        return hasKingsScent(player) ? SCENT_EXPIRY.get(player) - System.currentTimeMillis() : 0;
    }

    public static void clearKingsScent(UUID player) {
        SCENT_EXPIRY.remove(player);
    }

    public static boolean hasActiveJungleKey(UUID player) {
        return has(player, JUNGLE_KEY_ACTIVE);
    }

    public static void consumeJungleKey(UUID player) {
        clear(player, JUNGLE_KEY_ACTIVE);
        JungleTemple.reset(player);
    }

    public static int hitSludge(UUID player) {
        return SLUDGE_HITS.merge(player, 1, Integer::sum);
    }

    public enum Keeper {
        LAPIS("Lapis", "<9>", "DWARVEN_LAPIS_SWORD", "Lapis Sword"),
        GOLD("Gold", "<6>", "DWARVEN_GOLD_HAMMER", "Golden Hammer"),
        DIAMOND("Diamond", "<b>", "DWARVEN_DIAMOND_AXE", "Diamond Axe"),
        EMERALD("Emerald", "<a>", "DWARVEN_EMERALD_HAMMER", "Emerald Hammer");

        public final String displayName;
        public final String color;
        public final String scavengedItemId;
        public final String scavengedName;

        Keeper(String displayName, String color, String scavengedItemId, String scavengedName) {
            this.displayName = displayName;
            this.color = color;
            this.scavengedItemId = scavengedItemId;
            this.scavengedName = scavengedName;
        }
    }
}
