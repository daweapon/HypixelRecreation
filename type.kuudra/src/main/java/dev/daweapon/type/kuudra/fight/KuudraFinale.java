package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The scripted end of the fight, counted in ticks from Elle's "POW!" line (both captures). On Basic to Fiery it
 * is three lines and then KUUDRA DOWN. On Infernal at 25% it is the same lines, the platform centre collapsing in
 * two stages, Kuudra diving into the lair and Elle's lair lines.
 */
public final class KuudraFinale {
    public enum Kind {DEFEATED_LINE, COLLAPSE, KUUDRA_ENTERS_LAIR, LAIR_LINE, FINISH}

    public record Step(int tick, Kind kind, int index) {
    }

    private final List<Step> steps;
    private int ticks = -1;
    private int next;

    private KuudraFinale(List<Step> steps) {
        List<Step> sorted = new ArrayList<>(steps);
        sorted.sort(Comparator.comparingInt(Step::tick));
        this.steps = List.copyOf(sorted);
    }

    public static KuudraFinale death(KuudraFightSpec.Death death) {
        List<Step> steps = new ArrayList<>();
        lines(steps, death.lineTicks(), Kind.DEFEATED_LINE);
        steps.add(new Step(death.finishTicks(), Kind.FINISH, 0));
        return new KuudraFinale(steps);
    }

    public static KuudraFinale lair(KuudraFightSpec.Lair lair) {
        List<Step> steps = new ArrayList<>();
        lines(steps, lair.skipLineTicks(), Kind.DEFEATED_LINE);
        lines(steps, lair.collapseTicks(), Kind.COLLAPSE);
        steps.add(new Step(lair.kuudraEntersTicks(), Kind.KUUDRA_ENTERS_LAIR, 0));
        lines(steps, lair.lairLineTicks(), Kind.LAIR_LINE);
        return new KuudraFinale(steps);
    }

    private static void lines(List<Step> steps, List<Integer> ticks, Kind kind) {
        for (int i = 0; i < ticks.size(); i++) steps.add(new Step(ticks.get(i), kind, i));
    }

    /** Advances one tick and returns the steps due on it, in order. */
    public List<Step> tick() {
        ticks++;
        List<Step> due = new ArrayList<>();
        while (next < steps.size() && steps.get(next).tick() <= ticks) due.add(steps.get(next++));
        return due;
    }

    /** Runs the rest at once, for a staff skip. */
    public List<Step> rest() {
        List<Step> due = new ArrayList<>(steps.subList(next, steps.size()));
        next = steps.size();
        return due;
    }

    public boolean done() {
        return next >= steps.size();
    }

    public List<Step> steps() {
        return steps;
    }
}
