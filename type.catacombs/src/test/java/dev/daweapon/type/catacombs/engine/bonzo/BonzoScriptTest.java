package dev.daweapon.type.catacombs.engine.bonzo;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BonzoScriptTest {
    private record Timed(long tick, BonzoScript.Action action) {
    }

    private static List<Timed> run(BonzoScript script, int ticks) {
        List<Timed> seen = new ArrayList<>();
        for (int tick = 0; tick < ticks; tick++) {
            for (BonzoScript.Action action : script.tick()) seen.add(new Timed(script.now(), action));
        }
        return seen;
    }

    private static List<Long> ticksOf(List<Timed> seen, Class<?> type) {
        return seen.stream().filter(timed -> type.isInstance(timed.action())).map(Timed::tick).toList();
    }

    @Test
    void introLinesComeEveryFourSecondsWhileHeCannotBeHurt() {
        BonzoScript script = new BonzoScript(new SplittableRandom(1));
        List<Timed> seen = run(script, BonzoScript.MOVE_TICK - 1);
        List<String> lines = seen.stream().map(Timed::action).filter(BonzoScript.Say.class::isInstance)
                .map(action -> ((BonzoScript.Say) action).line()).toList();
        assertEquals(List.of("Gratz for making it this far, but I'm basically unbeatable.",
                "I don't even need to fight, this is the life!",
                "I can summon lots of undead! Check this out."), lines);
        assertEquals(List.of(1L, 83L, 165L), ticksOf(seen, BonzoScript.Say.class));
        assertFalse(script.vulnerable(), "the captures show no damage before he starts moving at 11.5 s");
        assertTrue(script.hurt().isEmpty());
    }

    @Test
    void heStartsMovingAndSummoningAtElevenAndAHalfSeconds() {
        BonzoScript script = new BonzoScript(new SplittableRandom(2));
        List<Timed> seen = run(script, 600);
        assertEquals(List.of((long) BonzoScript.MOVE_TICK), ticksOf(seen, BonzoScript.StartMoving.class));
        List<Long> summons = ticksOf(seen, BonzoScript.SummonOrb.class);
        assertEquals(BonzoScript.MOVE_TICK, summons.getFirst());
        for (int index = 1; index < summons.size(); index++) {
            long gap = summons.get(index) - summons.get(index - 1);
            assertTrue(gap >= BonzoScript.SUMMON_GAP_MIN && gap <= BonzoScript.SUMMON_GAP_MAX, "gap " + gap);
        }
        List<Long> skulls = ticksOf(seen, BonzoScript.SkullVolley.class);
        assertEquals(BonzoScript.MOVE_TICK + BonzoScript.SKULL_AFTER_MOVING, skulls.getFirst());
        assertEquals(BonzoScript.SECOND_SKULL, skulls.get(1) - skulls.get(0));
        assertTrue(script.vulnerable());
    }

    @Test
    void firstHurtComplainsOnceThenChattersEveryFourSeconds() {
        BonzoScript script = new BonzoScript(new SplittableRandom(3));
        run(script, BonzoScript.MOVE_TICK + 10);
        assertEquals(List.of(new BonzoScript.Say("Oh noes, you got me.. what ever will I do?!")), script.hurt());
        assertTrue(script.hurt().isEmpty());
        List<Timed> seen = run(script, BonzoScript.LINE_GAP);
        List<Timed> chatter = seen.stream().filter(timed -> timed.action() instanceof BonzoScript.Say).toList();
        assertEquals(1, chatter.size());
        assertTrue(BonzoScript.FIRST_LIFE_LINES.contains(((BonzoScript.Say) chatter.getFirst().action()).line()));
    }

    @Test
    void firstDeathSaysSikeThreeSecondsLaterAndRevivesAfterAnotherSecond() {
        BonzoScript script = new BonzoScript(new SplittableRandom(4));
        run(script, BonzoScript.MOVE_TICK + 5);
        long down = script.now();
        assertEquals(List.of(new BonzoScript.Say("Oh I'm dead!")), script.lifeLost());
        assertFalse(script.vulnerable());
        List<Timed> seen = run(script, 100);
        assertEquals(List.of(down + BonzoScript.SIKE_AFTER), ticksOf(seen, BonzoScript.Say.class).subList(0, 1));
        assertEquals(new BonzoScript.Say("Sike"), seen.stream().filter(timed -> timed.action() instanceof BonzoScript.Say)
                .findFirst().orElseThrow().action());
        assertEquals(List.of(down + BonzoScript.SIKE_AFTER + BonzoScript.REVIVE_AFTER_SIKE), ticksOf(seen, BonzoScript.Revive.class));
        assertTrue(script.vulnerable());
        assertTrue(ticksOf(seen, BonzoScript.SummonOrb.class).isEmpty(), "no summons while he is down");
    }

    @Test
    void secondLifeFiresBalloonsAndABarrage() {
        BonzoScript script = new BonzoScript(new SplittableRandom(5));
        run(script, BonzoScript.MOVE_TICK + 1);
        script.lifeLost();
        run(script, BonzoScript.SIKE_AFTER + BonzoScript.REVIVE_AFTER_SIKE);
        List<Timed> seen = run(script, BonzoScript.BARRAGE_FIRST + 1);
        assertFalse(ticksOf(seen, BonzoScript.Balloon.class).isEmpty());
        assertEquals(1, ticksOf(seen, BonzoScript.BalloonBarrage.class).size());
        assertTrue(seen.stream().anyMatch(timed -> timed.action().equals(new BonzoScript.Say(BonzoScript.SHOWTIME))));
        List<Timed> after = run(script, BonzoScript.BARRAGE_LINE);
        assertTrue(after.stream().anyMatch(timed -> timed.action() instanceof BonzoScript.Say(String line)
                && BonzoScript.BARRAGE_LINES.contains(line)));
    }

    @Test
    void theEndingFollowsTheCapturedTimings() {
        BonzoScript script = new BonzoScript(new SplittableRandom(6));
        run(script, BonzoScript.MOVE_TICK + 1);
        script.lifeLost();
        run(script, BonzoScript.SIKE_AFTER + BonzoScript.REVIVE_AFTER_SIKE);
        long end = script.now();
        assertEquals(List.of(new BonzoScript.Say("Alright, maybe I'm just weak after all..")), script.lifeLost());
        assertTrue(script.lifeLost().isEmpty());
        List<Timed> seen = run(script, 200);
        assertEquals(List.of(end + 7), ticksOf(seen, BonzoScript.DeathSounds.class));
        assertEquals(List.of(end + 45), ticksOf(seen, BonzoScript.ScoreScreen.class), "score screen 2.0-2.4 s after the last line");
        assertEquals(List.of(end + 84, end + 124), ticksOf(seen, BonzoScript.OpenGate.class), "gates at 4.1-4.4 s and 2 s later");
        assertEquals(List.of(new BonzoScript.OpenGate(1), new BonzoScript.OpenGate(2)),
                seen.stream().map(Timed::action).filter(BonzoScript.OpenGate.class::isInstance).toList());
        List<String> lines = seen.stream().map(Timed::action).filter(BonzoScript.Say.class::isInstance)
                .map(action -> ((BonzoScript.Say) action).line()).toList();
        assertEquals(List.of("But my masters are a lot stronger..", "Just you wait..."), lines);
        assertEquals(List.of(end + 82, end + 164), ticksOf(seen, BonzoScript.Say.class));
        assertTrue(script.finished());
        assertTrue(ticksOf(seen, BonzoScript.Balloon.class).isEmpty());
    }

    @Test
    void orbsArcUpAndLandOnTheirTarget() {
        Pos from = new Pos(-42.5, 73, 20.5);
        Pos to = new Pos(-42.5, 71, 22.5);
        assertEquals(from, BonzoScript.orbPosition(from, to, 0));
        Pos landed = BonzoScript.orbPosition(from, to, 1);
        assertEquals(72, landed.y(), 1e-9);
        assertEquals(22.5, landed.z(), 1e-9);
        double peak = BonzoScript.orbPosition(from, to, 0.5).y();
        assertTrue(peak > 80 && peak < 83, "the captured orb rose to about y 82.5, was " + peak);
    }
}
