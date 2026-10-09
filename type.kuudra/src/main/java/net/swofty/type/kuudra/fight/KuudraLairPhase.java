package net.swofty.type.kuudra.fight;

import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.kuudra.fight.world.KuudraBoss;
import net.swofty.type.kuudra.fight.world.LairCollapse;
import net.swofty.type.kuudra.fight.world.TentacleSlams;
import net.swofty.type.kuudra.mob.KuudraTentacle;
import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Infernal's last quarter (research K1 3.5 and the Infernal capture). At 25% Elle's "POW!" lines play while
 * tentacles flail across the platform, the platform centre collapses in two stages, Kuudra dives into the lair,
 * and Elle refuses to follow. In the lair his head pokes out of one wall at a time; hitting it takes real damage
 * off his last 240M. Telegraphed floor sections go green to red and kill anyone on them, and every death down
 * here is final.
 */
final class KuudraLairPhase {
    private static final String LAIR_SLAM = "LAIR";

    private final KuudraFight fight;
    private final KuudraFightSpec.Lair spec;
    private final KuudraArena.Lair geometry;
    private final TentacleSlams slams;
    private KuudraFinale finale;
    private boolean entered;
    private long nextTelegraph;
    private boolean lineNext;

    KuudraLairPhase(KuudraFight fight, KuudraArena.Lair geometry) {
        this.fight = fight;
        this.spec = fight.spec().lair();
        this.geometry = geometry;
        this.slams = new TentacleSlams(fight.instance(), geometry.floorY() - 1, fight.spec().tentacles().telegraphTicks());
    }

    boolean started() {
        return finale != null;
    }

    /** Kuudra is in the lair: hits count, deaths are final. */
    boolean entered() {
        return entered;
    }

    /** Kuudra hit 25%: the skip begins (both captures: "POW!" right as the bar reached 25%). */
    void begin() {
        if (finale != null) return;
        finale = KuudraFinale.lair(spec);
        fight.closeStomach();
        KuudraBossState state = fight.bossState();
        state.recover();
        state.setCycling(false);
        state.rise();
        KuudraBoss boss = fight.boss();
        if (boss != null) boss.setSag(0);
        fight.run().enterPhase(KuudraPhase.LAIR);
        flail();
    }

    /** "One last flail": tentacle sweeps cross the platform from its edges toward the centre (the Infernal capture). */
    private void flail() {
        List<KuudraTentacle> tentacles = fight.tentacles();
        for (int i = 0; i < spec.flailSlams(); i++) {
            Pos from = tentacles.isEmpty() ? fight.randomEdge() : tentacles.get(i % tentacles.size()).getPosition();
            int delay = i * 10;
            fight.later(Math.max(1, delay), () -> fight.flailSlam(from, spec.flailSlamDamage()));
        }
    }

    void tick(long tick) {
        if (finale != null) {
            for (KuudraFinale.Step step : finale.tick()) run(step);
        }
        if (!entered) return;
        slams.tick(this::land);
        if (tick >= nextTelegraph) {
            nextTelegraph = tick + spec.telegraphEverySeconds() * 20L;
            telegraph();
        }
    }

    void skip() {
        if (finale == null) begin();
        for (KuudraFinale.Step step : finale.rest()) run(step);
    }

    private void run(KuudraFinale.Step step) {
        switch (step.kind()) {
            case DEFEATED_LINE -> line("defeated", step.index());
            case COLLAPSE -> {
                if (step.index() == LairCollapse.stages(geometry) - 1) fight.removeBallista();
                LairCollapse.collapse(fight.instance(), geometry, step.index());
                fight.playAll("entity.generic.explode", 0.5f);
            }
            case KUUDRA_ENTERS_LAIR -> enter();
            case LAIR_LINE -> {
                line("lair_opens", step.index());
                if (step.index() == spec.lairLineTicks().size() - 1) bringEveryoneDown();
            }
            case FINISH -> {
            }
        }
    }

    private void line(String key, int index) {
        List<String> lines = fight.dialogue().chat(key, ThreadLocalRandom.current());
        if (index < lines.size()) fight.run().broadcast(lines.get(index));
    }

    private void enter() {
        if (entered) return;
        entered = true;
        KuudraBoss boss = fight.boss();
        KuudraBossState state = fight.bossState();
        if (boss != null) boss.enterLair(geometry.sides(), geometry.bounds().centre(), spec.retractBlocks(), ThreadLocalRandom.current());
        state.retime(spec.peekSeconds(), spec.moveSeconds(), spec.moveTicks());
        state.setFloor(0);
        state.setCycling(true);
        state.hide();
        nextTelegraph = fight.currentTick() + spec.telegraphEverySeconds() * 20L;
    }

    /** Elle's last line: anyone still up on the platform rim is dropped into the pit (EST). */
    private void bringEveryoneDown() {
        for (SkyBlockPlayer player : fight.alivePlayers()) {
            if (!geometry.contains(player.getPosition())) player.teleport(geometry.landing());
        }
    }

    /** Alternates a square of floor near a player and a tentacle line from the wall Kuudra is in (the capture). */
    private void telegraph() {
        List<SkyBlockPlayer> down = fight.alivePlayers().stream().filter(player -> geometry.contains(player.getPosition())).toList();
        Pos target = down.isEmpty() ? geometry.landing() : down.get(ThreadLocalRandom.current().nextInt(down.size())).getPosition();
        lineNext = !lineNext;
        KuudraBoss boss = fight.boss();
        if (lineNext && boss != null) {
            Pos from = boss.base().withY(geometry.floorY());
            slams.start(new TentacleSlams.Slam(TentacleSlams.path(from, target, spec.pathLength(), spec.pathWidth()), LAIR_SLAM));
        } else {
            slams.start(new TentacleSlams.Slam(square(target, spec.areaSize()), LAIR_SLAM));
        }
        fight.playAt(target, "entity.ravager.roar", 0.5f);
    }

    static List<TentacleSlams.Column> square(Pos centre, int size) {
        List<TentacleSlams.Column> columns = new ArrayList<>();
        int half = size / 2;
        for (int dx = -half; dx < size - half; dx++) {
            for (int dz = -half; dz < size - half; dz++) {
                columns.add(new TentacleSlams.Column(centre.blockX() + dx, centre.blockZ() + dz));
            }
        }
        return columns;
    }

    private void land(TentacleSlams.Slam slam) {
        for (SkyBlockPlayer player : fight.alivePlayers()) {
            Pos at = player.getPosition();
            if (TentacleSlams.covers(slam, at) && at.y() < geometry.floorY() + 3) fight.killInLair(player);
        }
        TentacleSlams.Column middle = slam.path().get(slam.path().size() / 2);
        fight.playAt(new Pos(middle.x(), geometry.floorY(), middle.z()), "entity.generic.explode", 0.8f);
    }

    /** A weapon hit on his head while it pokes out: real damage off the last 240M, with the capture's hit title. */
    void hit(SkyBlockPlayer player, double damage) {
        KuudraBossState state = fight.bossState();
        if (!entered || state.stance() != KuudraBossState.Stance.SURFACED || state.health() <= 0) return;
        double perPoint = spec.damagePerPoint(state.maxHealth());
        fight.damageKuudraPoints(LairHealth.points(damage, perPoint));
        player.showTitle(Title.title(Text.empty().asComponent(),
                Text.parse(LairHealth.title(state.health(), perPoint, spec.health())).asComponent(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
    }

    void stop() {
        slams.clear();
    }
}
