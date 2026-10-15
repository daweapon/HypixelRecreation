package dev.daweapon.type.kuudra.fight;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.kuudra.fight.world.LavaCargo;
import dev.daweapon.type.kuudra.fight.world.TentacleSlams;
import dev.daweapon.type.kuudra.mob.KuudraMob;
import dev.daweapon.type.kuudra.mob.KuudraTentacle;
import dev.daweapon.type.kuudra.run.KuudraPhase;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

final class KuudraTentacleOps {
    private record FlailSource(@Nullable KuudraTentacle tentacle, double damage) {
    }

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraTier tier;
    private final KuudraConfig config;
    private final KuudraFightSpec spec;
    private final KuudraArena arena;
    private final Instance instance;
    private final Set<KuudraMob> mobs;
    private final KuudraStage stage;

    final List<Long> tentacleRespawns = new CopyOnWriteArrayList<>();

    KuudraTentacleOps(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.tier = fight.tier;
        this.config = fight.config;
        this.spec = fight.spec;
        this.arena = fight.arena;
        this.instance = fight.instance;
        this.mobs = fight.mobs;
        this.stage = fight.stage;
    }

    void spawnTentacle() {
        KuudraFightSpec.Tentacles tentacles = spec.tentacles();
        KuudraMobSpec tentacleSpec = KuudraTentacle.spec(tier, tentacles.health().get(tier), tentacles.slamDamage().get(tier),
                config.tentacleTokens());
        KuudraTentacle tentacle = new KuudraTentacle(fight, tentacleSpec);
        mobs.add(tentacle);
        List<KuudraArena.SupplySpot> spots = new ArrayList<>(arena.supplySpots().values());
        Pos near = spots.get(ThreadLocalRandom.current().nextInt(spots.size())).position();
        tentacle.setInstance(instance, arena.lavaBeside(near.add(ThreadLocalRandom.current().nextDouble(-10, 10), 0,
                ThreadLocalRandom.current().nextDouble(-10, 10)), 1.5).add(0, -2, 0));
    }

    void tickTentacleRespawns() {
        for (Long due : tentacleRespawns) {
            if (due > fight.tick) continue;
            tentacleRespawns.remove(due);
            spawnTentacle();
        }
    }

    void slam(KuudraTentacle tentacle) {
        if (fight.slams == null || fight.stopped) return;
        List<SkyBlockPlayer> targets = fight.alivePlayers().stream().filter(player -> arena.onPlatform(player.getPosition())).toList();
        Pos toward = targets.isEmpty() ? stage.centre() : targets.get(ThreadLocalRandom.current().nextInt(targets.size())).getPosition();
        Pos from = tentacle.getPosition().withY(arena.floorY());
        fight.slams.start(new TentacleSlams.Slam(TentacleSlams.path(from, toward, spec.tentacles().pathLength(),
                spec.tentacles().pathWidth()), tentacle));
        stage.playAt(from, "entity.ravager.roar", 0.6f);
        for (LavaCargo item : fight.supplies.cargo) {
            CargoPickup pickup = item.pickup();
            if (pickup == null) continue;
            SkyBlockPlayer player = fight.member(pickup.player());
            if (player == null || player.getPosition().distance(tentacle.getPosition()) > spec.tentacles().grabRange()) continue;
            item.setPickup(null);
            player.setVelocity(new Vec(0, 25, 0));
            player.sendMessage("<c>A tentacle snatched you away from the supplies!");
        }
    }

    /** One of "One last flail"'s sweeps from the platform edge across the centre (the Infernal capture). */
    void flailSlam(Pos from, double damage) {
        if (fight.slams == null || fight.stopped) return;
        Pos edge = from.withY(arena.floorY());
        int length = (int) Math.ceil(KuudraStage.horizontal(edge, stage.centre()) * 2);
        KuudraTentacle source = tentacles().stream().findFirst().orElse(null);
        fight.slams.start(new TentacleSlams.Slam(TentacleSlams.path(edge, stage.centre(), length, spec.lair().pathWidth()),
                new FlailSource(source, damage)));
        stage.playAt(edge, "entity.ravager.roar", 0.6f);
    }

    void landSlam(TentacleSlams.Slam slam) {
        if (slam.source() instanceof FlailSource(KuudraTentacle tentacle, double damage)) {
            if (tentacle == null || tentacle.isDead() || tentacle.isRemoved()) return;
            for (SkyBlockPlayer player : fight.alivePlayers()) {
                Pos at = player.getPosition();
                if (TentacleSlams.covers(slam, at) && at.y() < arena.floorY() + 3) MobAttackService.trueStrike(tentacle, player, damage);
            }
            return;
        }
        if (!(slam.source() instanceof KuudraTentacle tentacle) || tentacle.isDead() || tentacle.isRemoved()) return;
        double damage = spec.tentacles().slamDamage().get(tier);
        for (SkyBlockPlayer player : fight.alivePlayers()) {
            Pos at = player.getPosition();
            if (TentacleSlams.covers(slam, at) && at.y() < arena.floorY() + 3) MobAttackService.trueStrike(tentacle, player, damage);
        }
        TentacleSlams.Column middle = slam.path().get(slam.path().size() / 2);
        stage.ring(new Pos(middle.x() + 0.5, arena.floorY(), middle.z() + 0.5), 2, Particle.EXPLOSION);
        stage.playAt(new Pos(middle.x(), arena.floorY(), middle.z()), "entity.generic.explode", 0.8f);
        if (fight.build != null && fight.ballista != null && run.state().phase() == KuudraPhase.BUILD) {
            boolean nearBallista = slam.path().stream().anyMatch(column ->
                    Math.hypot(column.x() + 0.5 - fight.ballista.base().x(), column.z() + 0.5 - fight.ballista.base().z()) <= spec.build().mobHitRange());
            if (nearBallista && fight.build.knockAny(spec.tentacles().buildKnockback(), ThreadLocalRandom.current()) != null) fight.ballistaOps.barkBallistaDamaged();
        }
    }

    List<KuudraTentacle> tentacles() {
        return mobs.stream().filter(mob -> mob instanceof KuudraTentacle && !mob.isDead())
                .map(mob -> (KuudraTentacle) mob).toList();
    }
}
