package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Pair weights and per-board counts are estimates: the official wiki that listed them has shut down. */
public final class SuperpairsRewards {
    public static final double ULTRA_RARE_BASE_PERCENT = 1.0;
    public static final double SERUM_PERCENT = 1.0638;
    public static final double FISH_PERCENT = 5.28;
    private static final double[] NADESHIKO_PERCENT = {0, 0, 0, 1D / 750, 1D / 500, 1D / 250};
    private static final List<String[]> REGULAR_BOOKS = List.of(
            new String[]{"SCAVENGER", "5"}, new String[]{"SHARPNESS", "6"}, new String[]{"LIFE_STEAL", "4"},
            new String[]{"ENDER_SLAYER", "6"}, new String[]{"THUNDERBOLT", "6"}, new String[]{"GROWTH", "6"},
            new String[]{"VENOMOUS", "6"}, new String[]{"WOODSPLITTER", "6"}, new String[]{"GIANT_KILLER", "6"},
            new String[]{"DRAIN", "4"}, new String[]{"PROTECTION", "6"}, new String[]{"TITAN_KILLER", "6"},
            new String[]{"FIRE_PROTECTION", "6"}, new String[]{"BLAST_PROTECTION", "6"},
            new String[]{"PROJECTILE_PROTECTION", "6"}, new String[]{"POWER", "6"});
    private static final List<String[]> ULTRA_RARE_BOOKS = List.of(
            new String[]{"BLAST_PROTECTION", "7"}, new String[]{"CRITICAL", "7"}, new String[]{"FIRE_PROTECTION", "7"},
            new String[]{"GIANT_KILLER", "7"}, new String[]{"GROWTH", "7"}, new String[]{"LUCK", "7"},
            new String[]{"POWER", "7"}, new String[]{"PROJECTILE_PROTECTION", "7"}, new String[]{"PROTECTION", "7"},
            new String[]{"SHARPNESS", "7"}, new String[]{"TITAN_KILLER", "7"}, new String[]{"THUNDERBOLT", "7"},
            new String[]{"THUNDERLORD", "7"}, new String[]{"CLEAVE", "6"}, new String[]{"CUBISM", "6"},
            new String[]{"EXECUTE", "6"}, new String[]{"GRAVITY", "6"}, new String[]{"LOOTING", "5"},
            new String[]{"PROSECUTE", "6"}, new String[]{"SNIPE", "4"}, new String[]{"FIRST_STRIKE", "5"},
            new String[]{"TRIPLE_STRIKE", "5"}, new String[]{"LIFE_STEAL", "5"}, new String[]{"CHANCE", "5"},
            new String[]{"DRAIN", "5"});
    private static final List<String> ENDCAPS = List.of("PESTHUNTING_GUIDE", "CHAIN_OF_THE_END_TIMES", "ENDSTONE_IDOL",
            "ENSNARED_SNAIL", "FATEFUL_STINGER", "GOLD_BOTTLE_CAP", "GOLDEN_BOUNTY", "OCTOPUS_TENDRIL", "SEVERED_HAND",
            "SEVERED_PINCER", "TROUBLED_BUBBLE", "VIBRANT_CORAL");
    private static final List<Material> XP_ICONS = List.of(Material.LAPIS_LAZULI, Material.IRON_INGOT,
            Material.GOLD_INGOT, Material.AMETHYST_SHARD, Material.PRISMARINE_CRYSTALS);

    private record WeightedPrize(PairPrize prize, double weight) {
    }

    private SuperpairsRewards() {
    }

    public static List<PairPrize> ultraRarePool() {
        List<PairPrize> pool = new ArrayList<>();
        for (String[] book : ULTRA_RARE_BOOKS) {
            PairPrize prize = bookPrize(book);
            if (prize != null) pool.add(prize);
        }
        for (String id : ENDCAPS) {
            ItemType type = ItemType.get(id);
            if (type != null) pool.add(PairPrize.item("ENDCAP_" + id, Material.PLAYER_HEAD, "<6>" + new net.swofty.type.skyblockgeneric.item.SkyBlockItem(type).getDisplayName(), type, 1));
        }
        return pool;
    }

    public static List<PairPrize> board(SuperpairsStake stake, @Nullable UUID player, double ultraRareMultiplier,
                                        @Nullable PairPrize guaranteed, RandomGenerator random) {
        List<PairPrize> board = new ArrayList<>();
        int pairs = stake.pairCount();
        board.add(PairPrize.lapis(Math.round(stake.pairXpMax() * (3 + random.nextDouble() * 3.5) / 100) * 100));
        if (pairs >= 10) board.add(PairPrize.powerUp(PairPrize.Kind.DIAMOND));

        List<PairPrize> special = new ArrayList<>();
        if (guaranteed != null) special.add(guaranteed);
        int index = stake.ordinal();
        if (index >= SuperpairsStake.SUPREME.ordinal()
                && LootRoll.roll(player, ItemType.get("DYE_NADESHIKO"), NADESHIKO_PERCENT[index], random)) {
            special.add(PairPrize.item("NADESHIKO", Material.PINK_DYE, "<d>Nadeshiko Dye", ItemType.get("DYE_NADESHIKO"), 1));
        }
        if (index >= SuperpairsStake.GRAND.ordinal()
                && LootRoll.roll(player, ItemType.get("METAPHYSICAL_SERUM"), SERUM_PERCENT, random)) {
            special.add(PairPrize.item("SERUM", Material.POTION, "<5>Metaphysical Serum", ItemType.get("METAPHYSICAL_SERUM"), 1));
        }
        if (stake == SuperpairsStake.METAPHYSICAL
                && LootRoll.roll(player, ItemType.get("EXPERIMENT_THE_FISH"), FISH_PERCENT, random)) {
            special.add(PairPrize.item("FISH", Material.COD, "<6>Experiment the Fish", ItemType.get("EXPERIMENT_THE_FISH"), 1));
        }
        if (index >= SuperpairsStake.SUPREME.ordinal()
                && LootRoll.roll(player, null, ULTRA_RARE_BASE_PERCENT * ultraRareMultiplier, random)) {
            List<PairPrize> pool = ultraRarePool();
            if (!pool.isEmpty()) {
                special.add(LootRoll.pick(player, pool, PairPrize::item, prize -> 1, random));
            }
        }
        for (PairPrize prize : special) {
            if (board.size() < pairs && board.stream().noneMatch(existing -> existing.key().equals(prize.key()))) board.add(prize);
        }

        List<WeightedPrize> pool = pool(stake, random);
        while (board.size() < pairs && !pool.isEmpty()) {
            WeightedPrize picked = LootRoll.pick(null, pool, entry -> null, WeightedPrize::weight, random);
            pool.remove(picked);
            board.add(picked.prize());
        }
        Collections.shuffle(board, new java.util.Random(random.nextLong()));
        return board;
    }

    public static long xpFor(PairPrize prize) {
        return prize.xp();
    }

    private static List<WeightedPrize> pool(SuperpairsStake stake, RandomGenerator random) {
        List<WeightedPrize> pool = new ArrayList<>();
        long span = stake.pairXpMax() - stake.pairXpMin();
        for (int i = 0; i < XP_ICONS.size(); i++) {
            long low = stake.pairXpMin() + span * i / XP_ICONS.size();
            long high = stake.pairXpMin() + span * (i + 1) / XP_ICONS.size();
            long value = Math.round((low + random.nextDouble() * (high - low)) / 100) * 100;
            pool.add(new WeightedPrize(PairPrize.xp("XP_" + i, XP_ICONS.get(i), value), 10));
        }
        int index = stake.ordinal();
        if (index <= SuperpairsStake.HIGH.ordinal()) {
            pool.add(new WeightedPrize(PairPrize.item("BOTTLE", Material.EXPERIENCE_BOTTLE, "<f>Experience Bottle <8>x" + (10 + random.nextInt(21)),
                    ItemType.EXPERIENCE_BOTTLE, 10 + random.nextInt(21)), 12));
        }
        if (index >= SuperpairsStake.HIGH.ordinal()) {
            int grand = 5 + random.nextInt(16);
            pool.add(new WeightedPrize(PairPrize.item("GRAND_BOTTLE", Material.EXPERIENCE_BOTTLE, "<a>Grand Experience Bottle <8>x" + grand,
                    ItemType.GRAND_EXP_BOTTLE, grand), 10));
            int titanic = index >= SuperpairsStake.TRANSCENDENT.ordinal() ? 1 + random.nextInt(2) : 1;
            pool.add(new WeightedPrize(PairPrize.item("TITANIC_BOTTLE", Material.EXPERIENCE_BOTTLE, "<9>Titanic Experience Bottle <8>x" + titanic,
                    ItemType.TITANIC_EXP_BOTTLE, titanic), 3));
        }
        if (index >= SuperpairsStake.GRAND.ordinal()) {
            List<String[]> books = new ArrayList<>(REGULAR_BOOKS);
            Collections.shuffle(books, new java.util.Random(random.nextLong()));
            for (String[] book : books.subList(0, 6)) {
                PairPrize prize = bookPrize(book);
                if (prize != null) pool.add(new WeightedPrize(prize, 4));
            }
        }
        if (index <= SuperpairsStake.GRAND.ordinal()) {
            pool.add(new WeightedPrize(PairPrize.pet(randomPetRarity(random)), 2));
        }
        return pool;
    }

    private static Rarity randomPetRarity(RandomGenerator random) {
        double roll = random.nextDouble();
        if (roll < 0.45) return Rarity.COMMON;
        if (roll < 0.75) return Rarity.UNCOMMON;
        if (roll < 0.9) return Rarity.RARE;
        if (roll < 0.98) return Rarity.EPIC;
        return Rarity.LEGENDARY;
    }

    private static @Nullable PairPrize bookPrize(String[] book) {
        try {
            return PairPrize.book(EnchantmentType.valueOf(book[0]), Integer.parseInt(book[1]));
        } catch (IllegalArgumentException missing) {
            return null;
        }
    }
}
