package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One pet ability defined in YAML. See {@code PET_FORMAT.md} for every field.
 */
@Getter
public final class ConfiguredPetAbility implements PetAbility {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");
    private static final String NOT_ACTIVE = "<8>(Not active yet)";

    private final String name;
    private final Rarity minRarity;
    private final @Nullable Rarity maxRarity;
    private final List<String> text;
    private final Map<Rarity, List<String>> textByRarity;
    private final List<PetValue> values;
    private final PetAbilityKind kind;
    private final Map<ItemStatistic, ValueRef> statistics;
    private final @Nullable PerStat perStat;
    private final Set<PetTrigger> triggers;
    private final List<ItemType> items;
    private final @Nullable ValueRef chance;
    private final Set<EntityType> mobs;
    private final Set<String> minions;
    private final Predicate<SkyBlockPlayer> condition;
    private final @Nullable String handlerId;
    private final @Nullable PetAbilityHandler handler;
    private final int baseLevel;

    public record PerStat(ItemStatistic grants, ValueRef amount, ItemStatistic per, ValueRef every, double above,
                          @Nullable Double max) {
        double grantFor(double playerStat, double[] values) {
            double every = this.every.resolve(values);
            if (every <= 0) return 0;
            double steps = Math.floor(Math.max(0, playerStat - above) / every);
            double granted = steps * amount.resolve(values);
            return max == null ? granted : Math.min(max, granted);
        }
    }

    ConfiguredPetAbility(String name, Rarity minRarity, @Nullable Rarity maxRarity, List<String> text,
                         Map<Rarity, List<String>> textByRarity, List<PetValue> values, PetAbilityKind kind,
                         Map<ItemStatistic, ValueRef> statistics, @Nullable PerStat perStat, Set<PetTrigger> triggers,
                         List<ItemType> items, @Nullable ValueRef chance, Set<EntityType> mobs, Set<String> minions,
                         Predicate<SkyBlockPlayer> condition, @Nullable String handlerId,
                         @Nullable PetAbilityHandler handler, int baseLevel) {
        this.name = name;
        this.minRarity = minRarity;
        this.maxRarity = maxRarity;
        this.text = text;
        this.textByRarity = textByRarity;
        this.values = values;
        this.kind = kind;
        this.statistics = statistics;
        this.perStat = perStat;
        this.triggers = triggers;
        this.items = items;
        this.chance = chance;
        this.mobs = mobs;
        this.minions = minions;
        this.condition = condition;
        this.handlerId = handlerId;
        this.handler = handler;
        this.baseLevel = baseLevel;
    }

    public boolean appliesAt(Rarity rarity) {
        return rarity.isAtLeast(minRarity) && (maxRarity == null || maxRarity.isAtLeast(rarity));
    }

    public boolean isActive() {
        return kind.isActive() && (kind != PetAbilityKind.HANDLER || handler != null);
    }

    public double minionSpeedPercent(String minion, SkyBlockItem pet, SkyBlockPlayer player) {
        if (kind != PetAbilityKind.MINION_SPEED || !minions.contains(minion) || !condition.test(player)) return 0;
        double[] evaluated = valuesFor(pet);
        return evaluated.length == 0 ? 0 : evaluated[0];
    }

    public double[] valuesFor(SkyBlockItem pet) {
        return valuesAt(pet.getAttributeHandler().getRarity(), PetLevels.level(pet));
    }

    public double[] valuesAt(Rarity rarity, int level) {
        double[] evaluated = new double[values.size()];
        for (int i = 0; i < evaluated.length; i++) evaluated[i] = values.get(i).at(rarity, level, baseLevel);
        return evaluated;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDescription(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        double[] evaluated = valuesFor(pet);
        if (handler != null) {
            List<String> custom = handler.description(context(pet, null, evaluated));
            if (custom != null) return custom;
        }
        List<String> lines = new ArrayList<>();
        for (String line : textByRarity.getOrDefault(rarity, text)) lines.add(fill(line, evaluated));
        if (!isActive()) lines.add(NOT_ACTIVE);
        return lines;
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        return switch (kind) {
            case STAT, PERCENT_STAT -> condition.test(player) ? flatOrPercent(valuesFor(pet)) : ItemStatistics.empty();
            case STAT_PER_STAT -> condition.test(player) ? perStat(player, valuesFor(pet)) : ItemStatistics.empty();
            case HANDLER -> handler == null || !condition.test(player) ? ItemStatistics.empty()
                    : handler.statistics(context(pet, player, valuesFor(pet)));
            default -> ItemStatistics.empty();
        };
    }

    @Override
    public void onEvent(PetEvent event, SkyBlockItem pet) {
        if (!isActive() || pet == null) return;
        if (!triggers.isEmpty() && !PetTrigger.any(triggers, event)) return;
        if (!mobs.isEmpty() && !matchesMob(event)) return;
        if (!condition.test(event.player())) return;
        double[] evaluated = valuesFor(pet);
        switch (kind) {
            case DROP -> drop(event.player(), evaluated);
            case DAMAGE_REDUCTION -> reduce(event, evaluated);
            case HANDLER -> handler.onEvent(event, context(pet, event.player(), evaluated));
            default -> {
            }
        }
    }

    private ItemStatistics flatOrPercent(double[] evaluated) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        statistics.forEach((statistic, ref) -> {
            double value = ref.resolve(evaluated);
            if (value == 0) return;
            if (kind == PetAbilityKind.PERCENT_STAT) builder.withAdditivePercentage(statistic, value);
            else builder.withBase(statistic, value);
        });
        return builder.build();
    }

    private ItemStatistics perStat(SkyBlockPlayer player, double[] evaluated) {
        double playerStat = player.getStatistics().allStatistics().getOverall(perStat.per());
        double granted = perStat.grantFor(playerStat, evaluated);
        if (granted == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(perStat.grants(), granted).build();
    }

    private void drop(SkyBlockPlayer player, double[] evaluated) {
        if (items.isEmpty() || chance == null) return;
        ItemType forced = LootRoll.claim(player.getUuid(), items);
        ItemType dropped = forced;
        if (dropped == null) {
            if (!LootRoll.roll(null, null, chance.resolve(evaluated))) return;
            dropped = items.get(ThreadLocalRandom.current().nextInt(items.size()));
        }
        player.addAndUpdateItem(new SkyBlockItem(dropped));
    }

    private void reduce(PetEvent event, double[] evaluated) {
        double percent = Math.max(0, Math.min(100, evaluated.length == 0 ? 0 : evaluated[0]));
        double factor = 1 - percent / 100D;
        if (event instanceof PetEvent.DamagedByMob hit) hit.damage(hit.damage() * factor);
        else if (event instanceof PetEvent.FallDamage fall) fall.damage(fall.damage() * factor);
    }

    private boolean matchesMob(PetEvent event) {
        SkyBlockMob mob = switch (event) {
            case PetEvent.Kill kill -> kill.mob();
            case PetEvent.MeleeHit hit -> hit.mob();
            case PetEvent.DamagedByMob hit -> hit.mob();
            default -> null;
        };
        return mob != null && mobs.contains(mob.getEntityType());
    }

    private PetAbilityContext context(SkyBlockItem pet, @Nullable SkyBlockPlayer player, double[] evaluated) {
        AbilityRuntime runtime = player == null ? null : player.getPetData().getAbilityRuntime(this);
        return new PetAbilityContext(pet, player, pet.getAttributeHandler().getRarity(), PetLevels.level(pet),
                evaluated, runtime);
    }

    private static String fill(String line, double[] evaluated) {
        Matcher matcher = PLACEHOLDER.matcher(line);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            String value = index < evaluated.length ? PetNumbers.format(evaluated[index]) : matcher.group();
            matcher.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
