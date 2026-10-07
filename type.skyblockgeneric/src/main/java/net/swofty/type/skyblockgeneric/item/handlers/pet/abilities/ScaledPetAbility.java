package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static net.swofty.commons.StringUtility.decimalify;

/**
 * A pet ability whose value grows linearly from level 1 to its rarity's level-100 figure, or holds
 * one flat value at every level.
 *
 * Abilities that only change a statistic are applied for real. The rest still show their real
 * text so the pet reads as it does on Hypixel, flagged as not yet active, because the combat
 * hooks they need do not exist yet.
 */
public final class ScaledPetAbility implements PetAbility {
    private final String name;
    private final String[] description;
    private final RarityValue<Double> atLevel100;
    private final Effect effect;
    private final boolean scalesWithLevel;

    private record Effect(List<ItemStatistic> statistics, boolean percentage, Predicate<SkyBlockPlayer> applies) {
    }

    private ScaledPetAbility(String name, RarityValue<Double> atLevel100, Effect effect, String... description) {
        this(name, atLevel100, effect, true, description);
    }

    private ScaledPetAbility(String name, RarityValue<Double> atLevel100, Effect effect, boolean scalesWithLevel,
                             String... description) {
        this.name = name;
        this.description = description;
        this.atLevel100 = atLevel100;
        this.effect = effect;
        this.scalesWithLevel = scalesWithLevel;
    }

    public static ScaledPetAbility flatPercentage(String name, double value, ItemStatistic statistic,
                                                  String... description) {
        return new ScaledPetAbility(name, RarityValue.singleDouble(value),
                new Effect(List.of(statistic), true, _ -> true), false, description);
    }

    public static ScaledPetAbility stat(String name, RarityValue<Double> atLevel100, ItemStatistic statistic,
                                        String... description) {
        return stats(name, atLevel100, List.of(statistic), description);
    }

    public static ScaledPetAbility stats(String name, RarityValue<Double> atLevel100, List<ItemStatistic> statistics,
                                         String... description) {
        return new ScaledPetAbility(name, atLevel100, new Effect(statistics, false, _ -> true), description);
    }

    public static ScaledPetAbility percentage(String name, RarityValue<Double> atLevel100, ItemStatistic statistic,
                                              String... description) {
        return new ScaledPetAbility(name, atLevel100, new Effect(List.of(statistic), true, _ -> true), description);
    }

    public static ScaledPetAbility percentageWhen(Predicate<SkyBlockPlayer> condition, String name,
                                                  RarityValue<Double> atLevel100, ItemStatistic statistic,
                                                  String... description) {
        return new ScaledPetAbility(name, atLevel100, new Effect(List.of(statistic), true, condition), description);
    }

    public static ScaledPetAbility statIn(Set<RegionType> regions, String name, RarityValue<Double> atLevel100,
                                          ItemStatistic statistic, String... description) {
        Predicate<SkyBlockPlayer> inRegion = player ->
                player.getRegion() != null && regions.contains(player.getRegion().getType());
        return new ScaledPetAbility(name, atLevel100, new Effect(List.of(statistic), false, inRegion), description);
    }

    public static ScaledPetAbility notActive(String name, RarityValue<Double> atLevel100, String... description) {
        return new ScaledPetAbility(name, atLevel100, null, description);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        String value = decimalify(valueFor(instance), 2);
        List<String> lines = new ArrayList<>();
        for (String line : description) {
            lines.add(line.replace("{0}", value));
        }
        if (effect == null) lines.add("<8>(Not active yet)");
        return lines;
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        if (effect == null || !effect.applies().test(player)) return ItemStatistics.empty();
        double value = valueFor(pet);
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ItemStatistic statistic : effect.statistics()) {
            if (effect.percentage()) builder.withAdditivePercentage(statistic, value);
            else builder.withBase(statistic, value);
        }
        return builder.build();
    }

    private double valueFor(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        if (!scalesWithLevel) return atLevel100.getForRarity(rarity);
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        return atLevel100.getForRarity(rarity) * level / 100.0;
    }
}
