package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static net.swofty.commons.StringUtility.decimalify;

/**
 * A pet ability with one or more values of the form {@code base + perLevel * level}, shown as
 * {@code {0}}, {@code {1}}, ... in its description. It either grants statistics, reacts to pet events,
 * is honoured by the gameplay system it describes, or is flagged as not active yet.
 */
public final class LeveledPetAbility implements PetAbility {
    private final String name;
    private final double[] base;
    private final double[] perLevel;
    private final boolean active;
    private final BiFunction<SkyBlockPlayer, double[], ItemStatistics> statistics;
    private final EventHandler eventHandler;
    private final String[] description;

    private LeveledPetAbility(String name, double[] base, double[] perLevel, boolean active,
                              BiFunction<SkyBlockPlayer, double[], ItemStatistics> statistics,
                              EventHandler eventHandler, String... description) {
        this.name = name;
        this.base = base;
        this.perLevel = perLevel;
        this.active = active;
        this.statistics = statistics;
        this.eventHandler = eventHandler;
        this.description = description;
    }

    @FunctionalInterface
    public interface EventHandler {
        void handle(PetEvent event, double[] values, AbilityRuntime runtime);
    }

    public static LeveledPetAbility gameplay(String name, double[] base, double[] perLevel, String... description) {
        return new LeveledPetAbility(name, base, perLevel, true, null, null, description);
    }

    public static LeveledPetAbility events(String name, double[] base, double[] perLevel, EventHandler handler,
                                           String... description) {
        return new LeveledPetAbility(name, base, perLevel, true, null, handler, description);
    }

    public static LeveledPetAbility notActive(String name, double[] base, double[] perLevel, String... description) {
        return new LeveledPetAbility(name, base, perLevel, false, null, null, description);
    }

    public static LeveledPetAbility statistics(String name, double[] base, double[] perLevel,
                                               BiFunction<SkyBlockPlayer, double[], ItemStatistics> statistics,
                                               String... description) {
        return new LeveledPetAbility(name, base, perLevel, true, statistics, null, description);
    }

    public static double[] values(double[] base, double[] perLevel, int level) {
        double[] values = new double[base.length];
        for (int i = 0; i < base.length; i++) values[i] = base[i] + perLevel[i] * level;
        return values;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        double[] values = valuesFor(instance);
        List<String> lines = new ArrayList<>();
        for (String line : description) {
            for (int i = 0; i < values.length; i++) line = line.replace("{" + i + "}", decimalify(values[i], 2));
            lines.add(line);
        }
        if (!active) lines.add("<8>(Not active yet)");
        return lines;
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        if (statistics == null) return ItemStatistics.empty();
        return statistics.apply(player, valuesFor(pet));
    }

    @Override
    public void onEvent(PetEvent event) {
        if (eventHandler == null) return;
        SkyBlockItem pet = event.player().getPetData().getEnabledPet();
        if (pet == null) return;
        eventHandler.handle(event, valuesFor(pet), event.player().getPetData().getAbilityRuntime(this));
    }

    private double[] valuesFor(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        return values(base, perLevel, pet.getAttributeHandler().getPetData().getAsLevel(rarity));
    }
}
