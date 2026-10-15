package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SafeConfig;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.pets.PetItemAbility;
import dev.daweapon.type.skyblockgeneric.user.pets.PetItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Pet Item: what it does while a pet holds it, which pets may hold it, and its lore. Every effect is
 * data; {@link PetItemAbility} applies the ones that need the player (timed buffs on kill).
 */
@Getter
public class PetItemComponent extends SkyBlockItemComponent {
    public static final String NOT_ACTIVE = "<8>(Not active yet)";

    private final ItemStatistics statistics;
    private final Map<ItemStatistic, Double> petStatisticBoosts;
    private final double allPetStatisticsBoost;
    private final int levelStep;
    private final ItemStatistics levelStatistics;
    private final double killBuffSeconds;
    private final ItemStatistics killBuffStatistics;
    private final boolean knockbackImmune;
    private final double petExpBoost;
    private final @Nullable SkillCategories petExpSkill;
    private final double expShareRate;
    private final @Nullable TierBoost tierBoost;
    private final Map<String, Double> trophyTierBoost;
    private final Set<String> onlyFor;
    private final Set<String> excludedPets;
    private final boolean inactive;
    private final List<String> description;
    private final @Nullable PetAbility ability;

    public record TierBoost(Rarity max, Map<String, Rarity> maxFor) {
        public Rarity maxFor(ItemType pet) {
            return maxFor.getOrDefault(pet.name(), max);
        }
    }

    private PetItemComponent(SafeConfig config) {
        this.statistics = statistics(config, "statistics");
        Map<ItemStatistic, Double> boosts = new EnumMap<>(ItemStatistic.class);
        double allBoost = 0;
        if (config.containsKey("pet_stat_boost")) {
            SafeConfig boostConfig = config.getNested("pet_stat_boost");
            for (String key : boostConfig.getKeys()) {
                double value = boostConfig.getDouble(key, 0.0);
                if (key.equalsIgnoreCase("all")) allBoost = value;
                else boosts.put(ItemStatistic.valueOf(key.toUpperCase()), value);
            }
        }
        this.petStatisticBoosts = Map.copyOf(boosts);
        this.allPetStatisticsBoost = allBoost;

        SafeConfig level = config.containsKey("level_statistics") ? config.getNested("level_statistics") : null;
        this.levelStep = level == null ? 1 : Math.max(1, level.getInt("every", 1));
        this.levelStatistics = level == null ? ItemStatistics.empty() : statistics(level, "statistics");

        SafeConfig kill = config.containsKey("kill_buff") ? config.getNested("kill_buff") : null;
        this.killBuffSeconds = kill == null ? 0 : kill.getDouble("seconds", 0.0);
        this.killBuffStatistics = kill == null ? ItemStatistics.empty() : statistics(kill, "statistics");

        this.knockbackImmune = config.getBoolean("knockback_immune", false);
        this.petExpBoost = config.getDouble("pet_exp_boost", 0.0);
        String expSkill = config.getString("pet_exp_skill", null);
        this.petExpSkill = expSkill == null ? null : SkillCategories.valueOf(expSkill.toUpperCase());
        this.expShareRate = config.getDouble("exp_share_rate", 0.0);
        this.tierBoost = config.containsKey("tier_boost") ? tierBoost(config.getNested("tier_boost")) : null;

        Map<String, Double> trophy = new LinkedHashMap<>();
        if (config.containsKey("trophy_tier_boost")) {
            SafeConfig trophyConfig = config.getNested("trophy_tier_boost");
            for (String key : trophyConfig.getKeys()) trophy.put(key.toUpperCase(), trophyConfig.getDouble(key, 0.0));
        }
        this.trophyTierBoost = Map.copyOf(trophy);

        this.onlyFor = names(config, "pet");
        this.excludedPets = names(config, "excluded_pets");
        this.inactive = config.getBoolean("inactive", false);
        this.description = config.containsKey("description") ? config.getList("description", String.class) : List.of();
        this.ability = killBuffSeconds > 0 ? new PetItemAbility(this) : null;

        addInheritedComponent(new ExtraRarityComponent("PET ITEM"));
        addInheritedComponent(new ExtraUnderNameComponent("Consumed on use"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new InteractableComponent(PetItems::give, null, null));
        if (!description.isEmpty()) {
            addInheritedComponent(new LoreUpdateComponent(new LoreConfig((item, player) -> lore(), null), false));
        }
    }

    public static PetItemComponent parse(SafeConfig config) {
        return new PetItemComponent(config);
    }

    private static ItemStatistics statistics(SafeConfig config, String key) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        if (!config.containsKey(key)) return builder.build();
        SafeConfig nested = config.getNested(key);
        for (String stat : nested.getKeys()) {
            builder.withBase(ItemStatistic.valueOf(stat.toUpperCase()), nested.getDouble(stat, 0.0));
        }
        return builder.build();
    }

    private static TierBoost tierBoost(SafeConfig config) {
        Map<String, Rarity> maxFor = new LinkedHashMap<>();
        if (config.containsKey("max_for")) {
            SafeConfig overrides = config.getNested("max_for");
            for (String pet : overrides.getKeys()) maxFor.put(pet, Rarity.valueOf(overrides.getString(pet).toUpperCase()));
        }
        return new TierBoost(Rarity.valueOf(config.getString("max", "LEGENDARY").toUpperCase()), Map.copyOf(maxFor));
    }

    private static Set<String> names(SafeConfig config, String key) {
        if (!config.containsKey(key)) return Set.of();
        Object raw = config.get(key, Object.class, null);
        Set<String> names = new LinkedHashSet<>();
        if (raw instanceof List<?> list) list.forEach(entry -> names.add(String.valueOf(entry).toUpperCase()));
        else if (raw != null) names.add(String.valueOf(raw).toUpperCase());
        return Set.copyOf(names);
    }

    public List<String> effectLines() {
        if (!inactive) return description;
        List<String> lines = new ArrayList<>(description);
        lines.add(NOT_ACTIVE);
        return lines;
    }

    private List<String> lore() {
        List<String> lore = new ArrayList<>();
        if (onlyFor.isEmpty()) {
            lore.add("<a>Pet Items <7>can boost pets in many");
            lore.add("<7>powerful ways! A pet can only hold");
            lore.add("<7>one <a>Pet Item<7>, but you can <e>swap <7>it at");
            lore.add("<7>any time!");
            lore.add("<8>The pet must be visible to apply the item!");
        } else {
            lore.add("<7>This pet item may only be used on a");
            lore.add("<6>" + String.join("<7>/<6>", onlyFor.stream().map(PetItemComponent::petName).toList()) + " <7>pet.");
        }
        lore.add("");
        lore.addAll(effectLines());
        lore.add("");
        lore.add("<e>Right-click on your summoned pet to");
        lore.add("<e>give it this item!");
        return lore;
    }

    private static String petName(String id) {
        return StringUtility.toNormalCase(id.replace("_PET", ""));
    }

    public boolean appliesTo(ItemType pet) {
        if (excludedPets.contains(pet.name())) return false;
        return onlyFor.isEmpty() || onlyFor.contains(pet.name());
    }

    public boolean hasLevelStatistics() {
        return !levelStatistics.getStatisticsBase().isEmpty();
    }
}
