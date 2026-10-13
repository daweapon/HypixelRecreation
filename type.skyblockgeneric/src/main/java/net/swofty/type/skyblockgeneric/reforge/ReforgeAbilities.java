package net.swofty.type.skyblockgeneric.reforge;

import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.type.skyblockgeneric.reforge.abilities.BloodSoakedAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.BountifulAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.CandiedAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.DimensionalAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.EruditeAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.FabledAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.FangedAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.HyperAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.MagneticAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.MarshyAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.MobDamageReductionAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.PestReforgeAbilities;
import net.swofty.type.skyblockgeneric.reforge.abilities.SaltyAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.SeaCreatureDefenseAbility;
import net.swofty.type.skyblockgeneric.reforge.abilities.StatisticAbilities;
import net.swofty.type.skyblockgeneric.reforge.abilities.StickyAbility;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ReforgeAbilities {
    private static volatile Registry registry;

    private ReforgeAbilities() {
    }

    public record Registry(Map<String, ReforgeAbility> abilities, Map<String, ReforgeAbilityCatalogue.Entry> catalogue) {
        public Optional<ReforgeAbility> ability(String reforgeId) {
            return Optional.ofNullable(abilities.get(reforgeId.toLowerCase(Locale.ROOT)));
        }

        public Optional<ReforgeAbilityCatalogue.Entry> entry(String reforgeId) {
            return Optional.ofNullable(catalogue.get(reforgeId.toLowerCase(Locale.ROOT)));
        }
    }

    public static List<ReforgeAbility> implementations() {
        List<ReforgeAbility> all = new ArrayList<>(StatisticAbilities.all());
        all.addAll(List.of(
                new SaltyAbility(),
                new MobDamageReductionAbility("undead", MobType.UNDEAD, 2),
                new MobDamageReductionAbility("cubic", MobType.INFERNAL, 2),
                new SeaCreatureDefenseAbility("calcified", 25),
                new FabledAbility(),
                new FangedAbility(),
                new HyperAbility(),
                new MagneticAbility(),
                new DimensionalAbility(),
                new BountifulAbility(),
                new StickyAbility(),
                new BloodSoakedAbility(),
                new EruditeAbility(),
                new CandiedAbility(),
                new MarshyAbility(),
                new PestReforgeAbilities.Beady(),
                new PestReforgeAbilities.Buzzing(),
                new PestReforgeAbilities.Squeaky()));
        return all;
    }

    public static Registry build(Path catalogueFile) {
        Map<String, ReforgeAbilityCatalogue.Entry> catalogue = ReforgeAbilityCatalogue.load(catalogueFile);
        Map<String, ReforgeAbility> abilities = new LinkedHashMap<>();
        for (ReforgeAbility implementation : implementations()) {
            abilities.put(implementation.reforgeId(), implementation);
        }
        catalogue.values().stream()
                .filter(entry -> entry.unavailable() != null)
                .forEach(entry -> abilities.put(entry.reforgeId(),
                        new UnavailableReforgeAbility(entry.reforgeId(), entry.unavailable())));
        return new Registry(Map.copyOf(abilities), Map.copyOf(catalogue));
    }

    public static Registry registry() {
        Registry current = registry;
        if (current == null) {
            synchronized (ReforgeAbilities.class) {
                if (registry == null) registry = build(ReforgeAbilityCatalogue.DEFAULT_PATH);
                current = registry;
            }
        }
        return current;
    }

    public static Optional<ReforgeAbility> of(Reforge reforge) {
        return reforge == null ? Optional.empty() : registry().ability(reforge.getId());
    }

    public static Optional<ReforgeAbility> active(Reforge reforge) {
        return of(reforge).filter(ability -> !(ability instanceof UnavailableReforgeAbility));
    }

    public static Collection<ReforgeAbility> all() {
        return registry().abilities().values();
    }
}
