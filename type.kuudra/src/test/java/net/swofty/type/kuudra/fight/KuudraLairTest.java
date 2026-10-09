package net.swofty.type.kuudra.fight;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.kuudra.fight.world.TentacleSlams;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraLairTest {
    private static final KuudraConfig CONFIG = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra"));
    private static final KuudraFightSpec.Lair LAIR = CONFIG.fight().lair();
    private static final KuudraArena.Lair GEOMETRY = CONFIG.arena().lair();

    private static List<KuudraFinale.Step> runAll(KuudraFinale finale, int ticks) {
        List<KuudraFinale.Step> seen = new ArrayList<>();
        for (int i = 0; i <= ticks; i++) seen.addAll(finale.tick());
        return seen;
    }

    @Test
    void theCapturedCollapseHasTwoStagesInsideThePit() {
        assertEquals(2, GEOMETRY.collapse().size());
        assertEquals(556, GEOMETRY.collapse().get(0).stream().mapToInt(KuudraArena.Run::size).sum());
        assertEquals(635, GEOMETRY.collapse().get(1).stream().mapToInt(KuudraArena.Run::size).sum());
        assertTrue(GEOMETRY.collapse().get(0).stream().allMatch(run -> run.y() == 77));
        for (List<KuudraArena.Run> stage : GEOMETRY.collapse()) {
            for (KuudraArena.Run run : stage) {
                assertTrue(GEOMETRY.bounds().containsColumn(run.xFrom(), run.z()), run.toString());
                assertTrue(GEOMETRY.bounds().containsColumn(run.xTo(), run.z()), run.toString());
            }
        }
    }

    @Test
    void kuudraPeeksFromOutsideThePitAndPlayersLandInIt() {
        assertEquals(4, GEOMETRY.sides().size());
        GEOMETRY.sides().values().forEach(side ->
                assertFalse(GEOMETRY.bounds().containsColumn(side.blockX(), side.blockZ()), side.toString()));
        assertTrue(GEOMETRY.contains(GEOMETRY.landing()));
        assertFalse(GEOMETRY.contains(new Pos(-101.5, 79, -105.5)));
    }

    @Test
    void theInfernalSkipRunsInTheCapturedOrder() {
        List<KuudraFinale.Step> steps = runAll(KuudraFinale.lair(LAIR), 700);
        assertEquals(List.of(
                KuudraFinale.Kind.DEFEATED_LINE, KuudraFinale.Kind.DEFEATED_LINE, KuudraFinale.Kind.DEFEATED_LINE,
                KuudraFinale.Kind.COLLAPSE, KuudraFinale.Kind.LAIR_LINE, KuudraFinale.Kind.COLLAPSE,
                KuudraFinale.Kind.KUUDRA_ENTERS_LAIR, KuudraFinale.Kind.LAIR_LINE, KuudraFinale.Kind.LAIR_LINE,
                KuudraFinale.Kind.LAIR_LINE), steps.stream().map(KuudraFinale.Step::kind).toList());
        assertEquals(List.of(0, 60, 120, 132, 170, 292, 330, 372, 472, 593), steps.stream().map(KuudraFinale.Step::tick).toList());
    }

    @Test
    void stepsArriveOnTheirTickAndTheRestCanBeSkipped() {
        KuudraFinale finale = KuudraFinale.lair(LAIR);
        assertEquals(1, finale.tick().size());
        for (int i = 1; i < 60; i++) assertTrue(finale.tick().isEmpty());
        assertEquals(KuudraFinale.Kind.DEFEATED_LINE, finale.tick().getFirst().kind());
        assertEquals(8, finale.rest().size());
        assertTrue(finale.done());
        assertTrue(finale.tick().isEmpty());
    }

    @Test
    void kuudraDiesWithThreeLinesThenKuudraDown() {
        List<KuudraFinale.Step> steps = runAll(KuudraFinale.death(CONFIG.fight().death()), 400);
        assertEquals(List.of(KuudraFinale.Kind.DEFEATED_LINE, KuudraFinale.Kind.DEFEATED_LINE,
                KuudraFinale.Kind.DEFEATED_LINE, KuudraFinale.Kind.FINISH), steps.stream().map(KuudraFinale.Step::kind).toList());
        assertEquals(310, steps.getLast().tick());
        assertEquals(3, CONFIG.dialogue().lines("defeated").size());
        assertEquals(4, CONFIG.dialogue().lines("lair_opens").size());
    }

    @Test
    void theLastQuarterIs240MOfRealDamage() {
        double perPoint = LAIR.damagePerPoint(CONFIG.fight().boss().health());
        assertEquals(9_600, perPoint, 1e-9);
        assertEquals(10, LairHealth.points(96_000, perPoint), 1e-9);
        assertEquals(0, LairHealth.points(-5, perPoint));
        assertEquals("<d>☠ <a>240M<f>/<a>240M<c>❤", LairHealth.title(25_000, perPoint, LAIR.health()));
        assertEquals("<d>☠ <a>239.9M<f>/<a>240M<c>❤", LairHealth.title(24_990, perPoint, LAIR.health()));
        assertEquals("<d>☠ <a>0<f>/<a>240M<c>❤", LairHealth.title(0, perPoint, LAIR.health()));
        assertEquals("950k", LairHealth.shortNumber(950_000));
    }

    @Test
    void theInfernalBarStopsAtAQuarterUntilTheLair() {
        KuudraBossState state = new KuudraBossState(CONFIG.fight().boss().health(), 30, 4, 6, 60);
        state.setFloor(state.maxHealth() * LAIR.threshold());
        state.damage(state.maxHealth());
        assertEquals(25, state.percent());
        assertTrue(state.atFloor());
        state.setFloor(0);
        double perPoint = LAIR.damagePerPoint(state.maxHealth());
        assertTrue(state.damage(LairHealth.points(LAIR.health(), perPoint)));
    }

    @Test
    void lairTelegraphsCoverTheCapturedSquare() {
        List<TentacleSlams.Column> square = KuudraLairPhase.square(new Pos(-100.5, 6, -105.5), LAIR.areaSize());
        assertEquals(256, square.size());
        assertTrue(square.contains(new TentacleSlams.Column(-109, -114)));
        assertTrue(square.contains(new TentacleSlams.Column(-94, -99)));
    }

    @Test
    void everyLairDeathIsFinalUntilASupportRevives() {
        KuudraLives lives = new KuudraLives(10, 3);
        UUID player = UUID.randomUUID();
        assertEquals(KuudraLives.Death.FINAL, lives.die(player, 0, true, false, 0));
        assertTrue(lives.isFinal(player));
        assertEquals(-1, lives.secondsToRespawn(player, 0));
        assertTrue(lives.tick(100_000).isEmpty());
        assertTrue(lives.revive(player, 100));
        assertFalse(lives.isGhost(player));
        assertTrue(lives.isInvulnerable(player, 101));
    }
}
