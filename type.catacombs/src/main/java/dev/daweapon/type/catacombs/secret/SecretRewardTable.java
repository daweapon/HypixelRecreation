package dev.daweapon.type.catacombs.secret;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.SecretType;
import dev.daweapon.dungeons.catacombs.secret.CatacombsSecretReward;
import dev.daweapon.dungeons.catacombs.secret.CatacombsSecretRewards;
import dev.daweapon.dungeons.catacombs.secret.CatacombsSecretType;
import dev.daweapon.dungeons.catacombs.secret.SecretRewardType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * What a secret gives: one weighted pick from the floor's {@link CatacombsSecretRewards} for that kind of secret. The
 * wiki lists the rewards but no chances, so the weights are ours.
 */
public final class SecretRewardTable {
    public static final double SECRET_DYE_PERCENT = 0.0001;

    public record Entry(SecretRewardType type, @Nullable ItemType item, double weight) {
        public boolean blessing() {
            return type == SecretRewardType.RANDOM_BLESSING;
        }
    }

    private static final Map<SecretRewardType, ItemType> ITEMS = new EnumMap<>(Map.ofEntries(
            Map.entry(SecretRewardType.HEALING_POTION, ItemType.HEALING_POTION_8),
            Map.entry(SecretRewardType.DECOY, ItemType.DUNGEON_DECOY),
            Map.entry(SecretRewardType.TRAINING_WEIGHTS, ItemType.TRAINING_WEIGHTS),
            Map.entry(SecretRewardType.SPIRIT_LEAP, ItemType.SPIRIT_LEAP),
            Map.entry(SecretRewardType.SUPERBOOM_TNT, ItemType.SUPERBOOM_TNT),
            Map.entry(SecretRewardType.REVIVE_STONE, ItemType.REVIVE_STONE),
            Map.entry(SecretRewardType.INFLATABLE_JERRY, ItemType.INFLATABLE_JERRY),
            Map.entry(SecretRewardType.TRAP, ItemType.DUNGEON_TRAP),
            Map.entry(SecretRewardType.DEFUSE_KIT, ItemType.DEFUSE_KIT),
            Map.entry(SecretRewardType.DUNGEON_CHEST_KEY, ItemType.DUNGEON_CHEST_KEY),
            Map.entry(SecretRewardType.TREASURE_TALISMAN, ItemType.TREASURE_TALISMAN),
            Map.entry(SecretRewardType.ARCHITECTS_FIRST_DRAFT, ItemType.ARCHITECTS_FIRST_DRAFT),
            Map.entry(SecretRewardType.SECRET_DYE, ItemType.DYE_SECRET)));

    private static final Map<SecretRewardType, Double> WEIGHTS = new EnumMap<>(Map.ofEntries(
            Map.entry(SecretRewardType.RANDOM_BLESSING, 20.0),
            Map.entry(SecretRewardType.HEALING_POTION, 15.0),
            Map.entry(SecretRewardType.SPIRIT_LEAP, 15.0),
            Map.entry(SecretRewardType.SUPERBOOM_TNT, 15.0),
            Map.entry(SecretRewardType.DECOY, 10.0),
            Map.entry(SecretRewardType.TRAINING_WEIGHTS, 6.0),
            Map.entry(SecretRewardType.INFLATABLE_JERRY, 6.0),
            Map.entry(SecretRewardType.TRAP, 6.0),
            Map.entry(SecretRewardType.DEFUSE_KIT, 4.0),
            Map.entry(SecretRewardType.ARCHITECTS_FIRST_DRAFT, 3.0),
            Map.entry(SecretRewardType.REVIVE_STONE, 2.0),
            Map.entry(SecretRewardType.DUNGEON_CHEST_KEY, 2.0),
            Map.entry(SecretRewardType.TREASURE_TALISMAN, 1.0)));

    private SecretRewardTable() {
    }

    public static CatacombsSecretType kind(SecretType type) {
        return switch (type) {
            case CHEST -> CatacombsSecretType.CHEST;
            case ITEM -> CatacombsSecretType.GROUND_ITEM;
            case BAT -> CatacombsSecretType.BAT;
            case ESSENCE -> CatacombsSecretType.WITHER_ESSENCE;
            case REDSTONE_KEY -> CatacombsSecretType.REDSTONE_KEY;
        };
    }

    public static List<Entry> entries(CatacombsFloor floor, CatacombsSecretType kind) {
        return CatacombsSecretRewards.defaults().stream()
                .filter(reward -> reward.canDrop(floor, kind))
                .map(CatacombsSecretReward::type)
                .filter(WEIGHTS::containsKey)
                .map(type -> new Entry(type, ITEMS.get(type), WEIGHTS.get(type)))
                .toList();
    }

    public static @Nullable ItemType item(SecretRewardType type) {
        return ITEMS.get(type);
    }

    /** The reward for one found secret, or null when this kind of secret only counts (Wither Essence, Redstone Keys). */
    public static @Nullable Entry roll(@Nullable UUID player, CatacombsFloor floor, CatacombsSecretType kind, RandomGenerator random) {
        List<Entry> entries = entries(floor, kind);
        if (entries.isEmpty()) return null;
        if (canDropDye(floor, kind) && LootRoll.roll(player, ItemType.DYE_SECRET, SECRET_DYE_PERCENT, random)) {
            return new Entry(SecretRewardType.SECRET_DYE, ItemType.DYE_SECRET, 0);
        }
        return LootRoll.pick(player, entries, Entry::item, Entry::weight, random);
    }

    private static boolean canDropDye(CatacombsFloor floor, CatacombsSecretType kind) {
        return CatacombsSecretRewards.defaults().stream()
                .anyMatch(reward -> reward.type() == SecretRewardType.SECRET_DYE && reward.canDrop(floor, kind));
    }
}
