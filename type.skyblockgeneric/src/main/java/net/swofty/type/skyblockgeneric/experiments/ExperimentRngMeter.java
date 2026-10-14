package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointExperiments;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ExperimentRngMeter {
    public static final long ENCHANTING_XP_PER_METER_XP = 1_000;

    public record Target(String id, PairPrize prize, long meterXp) {
    }

    private static final List<Target> TARGETS = buildTargets();

    private ExperimentRngMeter() {
    }

    private static List<Target> buildTargets() {
        List<Target> targets = new ArrayList<>();
        targets.add(new Target("TITANIC_BOTTLE", PairPrize.item("TITANIC_BOTTLE", Material.EXPERIENCE_BOTTLE,
                "<9>Titanic Experience Bottle", ItemType.TITANIC_EXP_BOTTLE, 1), 15_000));
        addItem(targets, "EXPERIMENT_THE_FISH", Material.COD, "<6>Experiment the Fish", 50_000);
        addItem(targets, "METAPHYSICAL_SERUM", Material.POTION, "<5>Metaphysical Serum", 50_000);
        targets.add(new Target("GUARDIAN_PET", PairPrize.pet(Rarity.LEGENDARY), 150_000));
        for (PairPrize prize : SuperpairsRewards.ultraRarePool()) {
            targets.add(new Target(prize.key(), prize, prize.kind() == PairPrize.Kind.ITEM || prize.level() >= 7 ? 500_000 : 150_000));
        }
        addItem(targets, "DYE_NADESHIKO", Material.PINK_DYE, "<d>Nadeshiko Dye", 2_500_000);
        return List.copyOf(targets);
    }

    private static void addItem(List<Target> targets, String id, Material icon, String name, long meterXp) {
        ItemType type = ItemType.get(id);
        if (type != null) targets.add(new Target(id, PairPrize.item(id, icon, name, type, 1), meterXp));
    }

    public static List<Target> targets() {
        return TARGETS;
    }

    public static @Nullable Target byId(@Nullable String id) {
        return id == null ? null : TARGETS.stream().filter(target -> target.id().equals(id)).findFirst().orElse(null);
    }

    public static @Nullable Target selected(SkyBlockPlayer player) {
        return byId(ExperimentTable.data(player).getRngSelected());
    }

    public static long stored(SkyBlockPlayer player) {
        return ExperimentTable.data(player).getRngXp();
    }

    public static void select(SkyBlockPlayer player, @Nullable Target target) {
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        data.setRngSelected(target == null ? null : target.id());
        ExperimentTable.save(player);
    }

    public static void addFromEnchantingXp(SkyBlockPlayer player, long enchantingXp) {
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        data.setRngXp(data.getRngXp() + enchantingXp / ENCHANTING_XP_PER_METER_XP);
        ExperimentTable.save(player);
    }

    public static void add(SkyBlockPlayer player, long meterXp) {
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        data.setRngXp(data.getRngXp() + meterXp);
        ExperimentTable.save(player);
    }

    public static @Nullable PairPrize guaranteedPrize(SkyBlockPlayer player) {
        Target target = selected(player);
        return target != null && stored(player) >= target.meterXp() ? target.prize() : null;
    }

    public static void consumeIfWon(SkyBlockPlayer player, List<PairPrize> found) {
        Target target = selected(player);
        if (target == null || found.stream().noneMatch(prize -> prize.key().equals(target.prize().key()))) return;
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        data.setRngXp(Math.max(0, data.getRngXp() - target.meterXp()));
        ExperimentTable.save(player);
    }

    public static double percent(SkyBlockPlayer player, Target target) {
        return Math.min(100D, stored(player) * 100D / target.meterXp());
    }
}
