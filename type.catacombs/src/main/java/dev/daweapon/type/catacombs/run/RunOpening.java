package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.catacombs.engine.DungeonEngine;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;

import java.util.List;

/**
 * The moment a run starts, timed from the two captured runs: the dragon growl, Mort's three lines (each with his
 * villager voice, two and one and a half seconds apart) and, a few ticks in, the Entrance gate sinking away.
 */
final class RunOpening {
    static final int GATE_DELAY_TICKS = 3;
    static final int[] MORT_LINE_TICKS = {0, 40, 70};
    private static final Sound GROWL = Sound.sound(Key.key("entity.ender_dragon.growl"), Sound.Source.HOSTILE, 1f, 1f);
    private static final Sound MORT_VOICE = Sound.sound(Key.key("entity.villager.trade"), Sound.Source.NEUTRAL, 1f, 0.6984127f);

    private RunOpening() {
    }

    static void play(CatacombsRun run, DungeonEngine engine) {
        run.onlineMembers().forEach(member -> member.playSound(GROWL));
        List<String> lines = CatacombsMessages.mortLines();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            run.later(MORT_LINE_TICKS[index], () -> {
                run.broadcast(line);
                Pos mort = run.arena().mortSpot().orElse(null);
                run.onlineMembers().forEach(member -> {
                    if (mort != null) member.playSound(MORT_VOICE, mort.x(), mort.y(), mort.z());
                    else member.playSound(MORT_VOICE);
                });
            });
        }
        run.later(GATE_DELAY_TICKS, engine::openEntrance);
    }
}
