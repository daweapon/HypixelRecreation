package net.swofty.type.skyblockgeneric.elections.perks;

import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor.Perk;

import java.util.EnumMap;
import java.util.Map;

/**
 * The number behind each perk, as the wiki states it. Percentages are stored as whole percents
 * (read them through {@link ActivePerks#multiplier}); "x times" perks are stored as the factor
 * (read them through {@link ActivePerks#factor}); stat perks are stored as the flat stat amount
 * (read them through {@link ActivePerks#bonus}). A Minister applies the same number as a Mayor.
 */
public final class PerkMagnitudes {
    private static final Map<Perk, Double> MAGNITUDES = new EnumMap<>(Map.ofEntries(
            Map.entry(Perk.SLASHED_PRICING, 50D),
            Map.entry(Perk.SLAYER_XP_BUFF, 25D),
            Map.entry(Perk.PATHFINDER, 20D),
            Map.entry(Perk.PROSPECTION, 25D),
            Map.entry(Perk.MINING_XP_BUFF, 60D),
            Map.entry(Perk.MINING_FIESTA, 75D),
            Map.entry(Perk.MOLTEN_FORGE, 25D),
            Map.entry(Perk.HUNTRESS_INTUITION, 10D),
            Map.entry(Perk.PET_XP_BUFF, 35D),
            Map.entry(Perk.SHARING_IS_CARING, 10D),
            Map.entry(Perk.VOLUME_TRADING, 2D),
            Map.entry(Perk.SHOPPING_SPREE, 10D),
            Map.entry(Perk.PEST_ERADICATOR, 20D),
            Map.entry(Perk.GOATED, 10D),
            Map.entry(Perk.BLOOMING_BUSINESS, 10D),
            Map.entry(Perk.SWEET_BENEVOLENCE, 30D),
            Map.entry(Perk.FISHING_XP_BUFF, 50D),
            Map.entry(Perk.LUCK_OF_THE_SEA, 15D),
            Map.entry(Perk.DOUBLE_TROUBLE, 0.1D),
            Map.entry(Perk.MARAUDER, 20D),
            Map.entry(Perk.EZPZ, 10D),
            Map.entry(Perk.BENEDICTION, 25D),
            Map.entry(Perk.ARCANE_CATALYST, 15D),
            Map.entry(Perk.ASTRAL_NEGOTIATOR, 15D),
            Map.entry(Perk.MAGIC_XP_BOOST, 15D),
            Map.entry(Perk.QUAD_TAXES, 4D),
            Map.entry(Perk.TURBO_MINIONS, 2D),
            Map.entry(Perk.DOUBLE_MOBS_HP, 2D),
            Map.entry(Perk.MOAR_SKILLZ, 50D),
            Map.entry(Perk.STATSPOCALYPSE, 10D)
    ));

    private PerkMagnitudes() {}

    public static double of(Perk perk) {
        return MAGNITUDES.getOrDefault(perk, 0D);
    }
}
