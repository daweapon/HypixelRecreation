package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.rabbits.Chocobits;
import dev.daweapon.type.skyblockgeneric.rabbits.FactionRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitFactions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class EggBonusRoller {
    public static final double FACTION_WEIGHT_FACTOR = 0.5;

    private EggBonusRoller() {
    }

    public enum Kind {
        RABBIT, FACTION, CHOCOBIT
    }

    public record Outcome(Kind kind, @Nullable FactionRabbit factionRabbit) {
        static final Outcome RABBIT = new Outcome(Kind.RABBIT, null);
        static final Outcome CHOCOBIT = new Outcome(Kind.CHOCOBIT, null);
    }

    private record Option(Kind kind, @Nullable RabbitRarity rarity, double weight) {
    }

    public static boolean allDivinesFound(ChocolateFactoryData data) {
        for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
            if (rabbit.getRarity() == RabbitRarity.DIVINE && !data.hasFoundRabbit(rabbit)) return false;
        }
        return true;
    }

    public static double chocobitWeight(RabbitRoller.Context context) {
        return RabbitRoller.rarityWeight(context, RabbitRarity.MYTHIC);
    }

    public static Outcome roll(UUID player, ChocolateFactoryData data, RabbitRoller.Context context, RandomGenerator random) {
        boolean chocobits = Chocobits.isUnlocked(data);
        if (chocobits && allDivinesFound(data) && context.divinePity() + 1 >= RabbitRoller.DIVINE_PITY) return Outcome.CHOCOBIT;
        if (RabbitRoller.pityDue(context)) return Outcome.RABBIT;

        List<ItemType> factionItems = RabbitFactions.eligibleItems(data);
        ItemType forced = LootRoll.claim(player, factionItems);
        if (forced != null) return new Outcome(Kind.FACTION, FactionRabbit.byItemId(forced.name()));

        List<Option> options = new ArrayList<>();
        double normal = 0;
        for (RabbitRarity rarity : RabbitRoller.availableRarities(context.factoryLevel())) {
            normal += RabbitRoller.rarityWeight(context, rarity);
        }
        options.add(new Option(Kind.RABBIT, null, normal));
        for (RabbitRarity rarity : RabbitFactions.eligibleRarities(data)) {
            options.add(new Option(Kind.FACTION, rarity, RabbitRoller.rarityWeight(context, rarity) * FACTION_WEIGHT_FACTOR));
        }
        if (chocobits) options.add(new Option(Kind.CHOCOBIT, null, chocobitWeight(context)));

        Option picked = LootRoll.pick(player, options, ignored -> null, Option::weight, random);
        return switch (picked.kind()) {
            case RABBIT -> Outcome.RABBIT;
            case CHOCOBIT -> Outcome.CHOCOBIT;
            case FACTION -> {
                FactionRabbit rabbit = RabbitFactions.pick(player, data.getFaction(), picked.rarity(), random);
                yield rabbit == null ? Outcome.RABBIT : new Outcome(Kind.FACTION, rabbit);
            }
        };
    }
}
