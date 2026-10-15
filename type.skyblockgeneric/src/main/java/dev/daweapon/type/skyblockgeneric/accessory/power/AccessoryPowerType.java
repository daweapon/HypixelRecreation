package dev.daweapon.type.skyblockgeneric.accessory.power;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static dev.daweapon.commons.skyblock.statistics.ItemStatistic.*;

/**
 * Every Accessory Bag power at Maxwell. Base values are Hypixel's per-multiplier stats (wiki
 * Powers/List of Accessory Powers for the ten default powers; Module:Power bucket data for the stone
 * powers); {@link PowerScaling} multiplies them by the Accessory Power multiplier. A stone power's
 * unique bonus is flat.
 */
@Getter
public enum AccessoryPowerType {
    FORTUITOUS("Fortuitous", PowerTier.STARTER, Material.GOLD_INGOT, null, 0,
            stats(HEALTH, 3.35, DEFENSE, 1.2, STRENGTH, 4.8, CRITICAL_CHANCE, 4.35, CRITICAL_DAMAGE, 4.8), none(), null, true),
    PRETTY("Pretty", PowerTier.STARTER, Material.OXEYE_DAISY, null, 0,
            stats(HEALTH, 1.65, DEFENSE, 1.2, SPEED, 0.65, STRENGTH, 4.8, INTELLIGENCE, 10.8, CRITICAL_CHANCE, 0.475, CRITICAL_DAMAGE, 1.2), none(), null, true),
    PROTECTED("Protected", PowerTier.STARTER, Material.IRON_CHESTPLATE, null, 0,
            stats(HEALTH, 11.75, DEFENSE, 10.8, STRENGTH, 2.4, CRITICAL_CHANCE, 0.475, CRITICAL_DAMAGE, 1.2), none(), null, true),
    SIMPLE("Simple", PowerTier.STARTER, Material.STONE, null, 0,
            stats(HEALTH, 5.02, DEFENSE, 3.6, SPEED, 1.2, STRENGTH, 3.6, INTELLIGENCE, 5.4, CRITICAL_CHANCE, 1.45, CRITICAL_DAMAGE, 3.6), none(), null, true),
    WARRIOR("Warrior", PowerTier.STARTER, Material.DIAMOND, null, 0,
            stats(HEALTH, 3.35, DEFENSE, 1.2, STRENGTH, 8.4, CRITICAL_CHANCE, 2.4, CRITICAL_DAMAGE, 6), none(), null, true),
    COMMANDO("Commando", PowerTier.INTERMEDIATE, Material.STICK, null, 15,
            stats(HEALTH, 5.02, DEFENSE, 2.4, STRENGTH, 8.4, CRITICAL_CHANCE, 0.475, CRITICAL_DAMAGE, 8.4), none(), null, true),
    DISCIPLINED("Disciplined", PowerTier.INTERMEDIATE, Material.IRON_SWORD, null, 15,
            stats(HEALTH, 5.02, DEFENSE, 2.4, STRENGTH, 7.2, CRITICAL_CHANCE, 1.45, CRITICAL_DAMAGE, 7.2), none(), null, true),
    INSPIRED("Inspired", PowerTier.INTERMEDIATE, Material.LAPIS_LAZULI, null, 15,
            stats(HEALTH, 1.65, DEFENSE, 1.2, STRENGTH, 4.8, INTELLIGENCE, 16.2, CRITICAL_CHANCE, 0.95, CRITICAL_DAMAGE, 3.6), none(), null, true),
    OMINOUS("Ominous", PowerTier.INTERMEDIATE, Material.PRISMARINE_CRYSTALS, null, 15,
            stats(HEALTH, 5.02, SPEED, 0.95, STRENGTH, 3.6, INTELLIGENCE, 6.1, CRITICAL_CHANCE, 1.45, CRITICAL_DAMAGE, 3.6, BONUS_ATTACK_SPEED, 0.9), none(), null, true),
    PREPARED("Prepared", PowerTier.INTERMEDIATE, Material.BREAD, null, 15,
            stats(HEALTH, 12.4, DEFENSE, 11.3, STRENGTH, 1.95, CRITICAL_CHANCE, 0.4, CRITICAL_DAMAGE, 0.95), none(), null, true),

    SANGUISUGE("Sanguisuge", PowerTier.STARTER_STONE, Material.PAPER, "DISPLACED_LEECH", 10,
            stats(HEALTH, 3.36, STRENGTH, 14.4, CRITICAL_CHANCE, 0.96, CRITICAL_DAMAGE, 4.8),
            stats(INTELLIGENCE, 100),
            "Unlocked from <a>Hemovibe V <f>collection.", false),
    SIGHTED("Sighted", PowerTier.INTERMEDIATE_STONE, Material.PAPER, "ENDER_MONOCLE", 15,
            stats(INTELLIGENCE, 36),
            stats(ABILITY_DAMAGE, 3),
            "Drops from Seers in <d>The End<f>.", false),
    SILKY("Silky", PowerTier.INTERMEDIATE_STONE, Material.PAPER, "LUXURIOUS_SPOOL", 15,
            stats(CRITICAL_DAMAGE, 22.8, SPEED, 0.6),
            stats(BONUS_ATTACK_SPEED, 5),
            "Drops from the <c>Broodmother <f>in the <c>Spider's Den<f>.", false),
    SWEET("Sweet", PowerTier.INTERMEDIATE_STONE, Material.PAPER, "ROCK_CANDY", 15,
            stats(HEALTH, 15.12, DEFENSE, 10.8, SPEED, 1.2),
            stats(SPEED, 5),
            "Drops from <6>Trick or Treat Chests <f>during the <6>Spooky Festival<f>.", false),
    ADEPT("Adept", PowerTier.ADVANCED_STONE, Material.PAPER, "END_STONE_SHULKER", 20,
            stats(HEALTH, 16.8, DEFENSE, 9.6, INTELLIGENCE, 3.6),
            stats(HEALTH, 100, DEFENSE, 50),
            "Rarely drops from <e>glowing <f>end stone blocks in <d>The End<f>.", false),
    BLOODY("Bloody", PowerTier.ADVANCED_STONE, Material.PAPER, "BEATING_HEART", 20,
            stats(STRENGTH, 10.8, CRITICAL_DAMAGE, 10.8, INTELLIGENCE, 3.6),
            stats(BONUS_ATTACK_SPEED, 10),
            "Drops from mobs in the <c>Blood Room <f>of <c>The Catacombs<f>.", false),
    BUTTERY("Buttery", PowerTier.ADVANCED_STONE, Material.PAPER, "SUNFLOWER_BUTTER", 20,
            stats(BONUS_ATTACK_SPEED, 2.16, SPEED, 2.4),
            stats(BONUS_ATTACK_SPEED, 5),
            "Crafted using <a>Salted Sunflower Seeds<f>.", false),
    CRUMBLY("Crumbly", PowerTier.ADVANCED_STONE, Material.PAPER, "CHOCOLATE_CHIP", 20,
            stats(HEALTH, 10.08, TRUE_DEFENSE, 1.632, CRITICAL_CHANCE, 0.48, BONUS_ATTACK_SPEED, 0.72, INTELLIGENCE, 5.4),
            stats(SPEED, 25),
            "Drops from the <4>Riftstalker Bloodfiend <f>slayer boss.", false),
    FORCEFUL("Forceful", PowerTier.ADVANCED_STONE, Material.PAPER, "ACACIA_BIRDHOUSE", 20,
            stats(HEALTH, 1.68, STRENGTH, 18, CRITICAL_DAMAGE, 4.8),
            stats(FEROCITY, 4),
            "Unlocked from <a>Acacia IX <f>collection.", true),
    FROZEN("Frozen", PowerTier.ADVANCED_STONE, Material.PAPER, "GLACITE_SHARD", 20,
            stats(DEFENSE, 13.2, TRUE_DEFENSE, 1.836),
            stats(TRUE_DEFENSE, 10, STRENGTH, 25, CRITICAL_DAMAGE, 25),
            "Drops from mobs in <b>Glacite Mineshafts<f>.", false),
    ITCHY("Itchy", PowerTier.ADVANCED_STONE, Material.PAPER, "FURBALL", 20,
            stats(STRENGTH, 7.2, CRITICAL_DAMAGE, 8.4, BONUS_ATTACK_SPEED, 2.16, SPEED, 0.6),
            stats(STRENGTH, 15, CRITICAL_DAMAGE, 15),
            "Drops from the <c>Sven Packmaster <f>slayer boss.", true),
    MYTHICAL("Mythical", PowerTier.ADVANCED_STONE, Material.PAPER, "OBSIDIAN_TABLET", 20,
            stats(HEALTH, 5.712, DEFENSE, 4.08, STRENGTH, 4.08, CRITICAL_CHANCE, 1.632, CRITICAL_DAMAGE, 4.08, INTELLIGENCE, 6.12, SPEED, 0.96),
            stats(HEALTH, 150, STRENGTH, 40),
            "Unlocked from <a>Obsidian IX <f>collection.", true),
    SHADED("Shaded", PowerTier.ADVANCED_STONE, Material.PAPER, "DARK_ORB", 20,
            stats(STRENGTH, 4.8, CRITICAL_DAMAGE, 18, SPEED, 0.6),
            stats(BONUS_ATTACK_SPEED, 3, FEROCITY, 3),
            "Sometimes obtained from defeating <c>Livid <f>in <c>The Catacombs<f>.", false),
    BIZARRE("Bizarre", PowerTier.MASTER_STONE, Material.PAPER, "ECCENTRIC_PAINTING", 25,
            stats(STRENGTH, -2.4, CRITICAL_DAMAGE, -2.4, INTELLIGENCE, 43.2),
            stats(ABILITY_DAMAGE, 5),
            "Sold by Sirius in the <5>Dark Auction<f>.", false),
    BUBBA("Bubba", PowerTier.MASTER_STONE, Material.PAPER, "BUBBA_BLISTER", 25,
            stats(HEALTH, 5.04, DEFENSE, -9.6, TRUE_DEFENSE, 0.816, STRENGTH, 6, CRITICAL_CHANCE, 0.96, CRITICAL_DAMAGE, 10.8, BONUS_ATTACK_SPEED, 1.8),
            stats(COMBAT_WISDOM, 2),
            "Drops from the <4>Riftstalker Bloodfiend <f>slayer boss.", false),
    DEMONIC("Demonic", PowerTier.MASTER_STONE, Material.PAPER, "HORNS_OF_TORMENT", 25,
            stats(STRENGTH, 5.52, INTELLIGENCE, 27.72),
            stats(CRITICAL_DAMAGE, 50),
            "Unlocked from the <a>Mutton IX <f>collection.", true),
    HURTFUL("Hurtful", PowerTier.MASTER_STONE, Material.PAPER, "MAGMA_URCHIN", 25,
            stats(STRENGTH, 4.8, CRITICAL_DAMAGE, 19.2),
            stats(BONUS_ATTACK_SPEED, 15),
            "Drops from minibosses in the <c>Crimson Isle<f>.", true),
    PLEASANT("Pleasant", PowerTier.MASTER_STONE, Material.PAPER, "PRECIOUS_PEARL", 25,
            stats(HEALTH, 13.44, DEFENSE, 14.4),
            none(),
            "Very rare reward from opening the <5>Crystal Nucleus<f>.", false),
    HEALTHY("Healthy", PowerTier.GRANDIOSE_STONE, Material.PAPER, "VITAMIN_DEATH", 30,
            stats(HEALTH, 33.6),
            stats(HEALTH, 200),
            "Drops from <c>Healthy <f>mobs in <c>The Catacombs<f>.", false),
    SLENDER("Slender", PowerTier.GRANDIOSE_STONE, Material.PAPER, "HAZMAT_ENDERMAN", 30,
            stats(HEALTH, 8.4, DEFENSE, 6, STRENGTH, 6, CRITICAL_DAMAGE, 6, BONUS_ATTACK_SPEED, 1.08, INTELLIGENCE, 9, SPEED, 0.6),
            stats(DEFENSE, 100, STRENGTH, 50),
            "Drops from the <5>Voidgloom Seraph <f>slayer boss.", true),
    STRONG("Strong", PowerTier.GRANDIOSE_STONE, Material.PAPER, "MANDRAA", 30,
            stats(STRENGTH, 12, CRITICAL_DAMAGE, 12),
            stats(STRENGTH, 25, CRITICAL_DAMAGE, 25),
            "Obtained from defeating <c>Kuudra <f>at the <6>Burning <f>tier or above.", false),
    SCORCHING("Scorching", PowerTier.MARVELOUS_STONE, Material.PAPER, "SCORCHED_BOOKS", 35,
            stats(STRENGTH, 8.4, CRITICAL_DAMAGE, 9.6, BONUS_ATTACK_SPEED, 1.8),
            stats(FEROCITY, 7),
            "Drops from the <4>Inferno Demonlord <f>slayer boss.", true);

    public static final int STONES_TO_LEARN = 9;

    private final String displayName;
    private final PowerTier tier;
    private final Material icon;
    private final @Nullable String stoneId;
    private final int combatLevel;
    private final Map<ItemStatistic, Double> baseStatistics;
    private final Map<ItemStatistic, Double> uniqueBonus;
    private final @Nullable String source;
    private final boolean sourceActive;

    AccessoryPowerType(String displayName, PowerTier tier, Material icon, @Nullable String stoneId, int combatLevel,
                       Map<ItemStatistic, Double> baseStatistics, Map<ItemStatistic, Double> uniqueBonus,
                       @Nullable String source, boolean sourceActive) {
        this.displayName = displayName;
        this.tier = tier;
        this.icon = icon;
        this.stoneId = stoneId;
        this.combatLevel = combatLevel;
        this.baseStatistics = Collections.unmodifiableMap(baseStatistics);
        this.uniqueBonus = Collections.unmodifiableMap(uniqueBonus);
        this.source = source;
        this.sourceActive = sourceActive;
    }

    public boolean isStonePower() {
        return stoneId != null;
    }

    public @Nullable ItemType stone() {
        return stoneId == null ? null : ItemType.get(stoneId);
    }

    public static @Nullable AccessoryPowerType byStone(@Nullable ItemType stone) {
        if (stone == null) return null;
        for (AccessoryPowerType power : values()) {
            if (stone.name().equals(power.stoneId)) return power;
        }
        return null;
    }

    public static @Nullable AccessoryPowerType byName(@Nullable String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<AccessoryPowerType> stonePowers() {
        return Arrays.stream(values()).filter(AccessoryPowerType::isStonePower).toList();
    }

    /** Maxwell's order: stone powers from the rarest tier down, then intermediate, then starter powers. */
    public static List<AccessoryPowerType> menuOrder() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing((AccessoryPowerType power) -> -power.tier.ordinal())
                        .thenComparing(AccessoryPowerType::getDisplayName))
                .toList();
    }

    private static Map<ItemStatistic, Double> none() {
        return Map.of();
    }

    private static Map<ItemStatistic, Double> stats(Object... pairs) {
        Map<ItemStatistic, Double> map = new EnumMap<>(ItemStatistic.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((ItemStatistic) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return map;
    }
}
