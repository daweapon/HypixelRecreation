package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pet abilities that buff other players. Parrot's Bird Discourse gives +{0} Strength to every player within 20 blocks, the best one counting. Turtle's Genius Amniote gives its owner's nearest four other players within
 * 50 blocks +{0}% Defense; when several turtles reach a player, the best one counts.
 */
public final class PetAuras {
    private PetAuras() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer receiver) {
        double percent = geniusAmniotePercent(receiver);
        double strength = birdDiscourseStrength(receiver);
        if (percent <= 0 && strength <= 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        if (percent > 0) builder.withAdditivePercentage(ItemStatistic.DEFENSE, percent);
        if (strength > 0) builder.withBase(ItemStatistic.STRENGTH, strength);
        return builder.build();
    }

    public static double birdDiscourseStrength(SkyBlockPlayer receiver) {
        Instance instance = receiver.getInstance();
        if (instance == null) return 0;
        double best = 0;
        for (Player other : instance.getPlayers()) {
            if (!(other instanceof SkyBlockPlayer owner) || owner.getSkyblockDataHandler() == null) continue;
            if (!PetPerkMath.inAuraRange(owner.getPosition().distance(receiver.getPosition()))) continue;
            best = PetPerkMath.bestAura(best, PetAbilities.value(owner, ItemType.PARROT_PET, "Bird Discourse", 0));
        }
        return best;
    }

    public static double geniusAmniotePercent(SkyBlockPlayer receiver) {
        Instance instance = receiver.getInstance();
        if (instance == null) return 0;
        double best = 0;
        for (Player other : instance.getPlayers()) {
            if (other == receiver || !(other instanceof SkyBlockPlayer owner) || owner.getSkyblockDataHandler() == null) continue;
            double percent = PetAbilities.value(owner, ItemType.TURTLE_PET, "Genius Amniote", 0);
            if (percent <= best) continue;
            if (isAmongNearest(owner, receiver, instance)) best = percent;
        }
        return best;
    }

    private static boolean isAmongNearest(SkyBlockPlayer owner, SkyBlockPlayer receiver, Instance instance) {
        List<Player> others = new ArrayList<>();
        for (Player candidate : instance.getPlayers()) {
            if (candidate != owner && candidate.getPosition().distance(owner.getPosition()) <= PetPerkMath.AURA_RANGE) {
                others.add(candidate);
            }
        }
        others.sort(Comparator.comparingDouble(candidate -> candidate.getPosition().distance(owner.getPosition())));
        return others.subList(0, Math.min(PetPerkMath.AURA_TARGETS, others.size())).contains(receiver);
    }
}
