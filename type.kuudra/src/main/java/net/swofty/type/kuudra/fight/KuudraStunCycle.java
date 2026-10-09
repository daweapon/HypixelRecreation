package net.swofty.type.kuudra.fight;

import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.commons.text.Text;
import net.swofty.type.kuudra.fight.world.KuudraBoss;
import net.swofty.type.kuudra.fight.world.KuudraItems;
import net.swofty.type.kuudra.fight.world.StomachSite;
import net.swofty.type.kuudra.mob.KuudraMob;
import net.swofty.type.kuudra.mob.MagmaBacteria;
import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DrillComponent;
import net.swofty.type.skyblockgeneric.item.components.PickaxeComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The stun cycle on Burning, Fiery and Infernal (research K1 3.4 and the Burning and Infernal captures). Once the
 * Ballista is built Kuudra leans in, Elle gets eaten, and players get inside him through the Consumption Zone or a
 * Human Cannonball. Mining one of the three pods stuns him for a few seconds, during which the Ballista and
 * weapons hurt him; the stunner is spat back out of his mouth. He flushes everyone out when he submerges.
 */
final class KuudraStunCycle {
    private final KuudraFight fight;
    private final KuudraFightSpec.Stun stun;
    private final KuudraFightSpec.Stomach rules;
    private final KuudraArena.Stomach geometry;
    private final KuudraStomach stomach;
    private final StomachSite site;
    private final List<KuudraMob> bacteria = new ArrayList<>();
    private final Map<UUID, Long> cannonballs = new ConcurrentHashMap<>();
    private final StunnedHits hits;
    private final Set<UUID> greeted = ConcurrentHashMap.newKeySet();
    private boolean begun;
    private boolean elleEaten;
    private boolean bacteriaBarked;
    private long lastBacteria = Long.MIN_VALUE / 2;
    private long spitAt = -1;

    KuudraStunCycle(KuudraFight fight, KuudraArena.Stomach geometry) {
        this.fight = fight;
        this.stun = fight.spec().stun();
        this.hits = new StunnedHits(stun.stunnedHitCooldownTicks(), stun.stunnedArrowHitsPerTick());
        this.rules = fight.spec().stomach();
        this.geometry = geometry;
        this.stomach = new KuudraStomach(geometry.pods().keySet());
        this.site = new StomachSite(fight.instance(), geometry);
    }

    /** The Ballista is built: Kuudra leans in with a long surface timer, his stomach opens and Elle gets eaten. */
    void begin() {
        if (begun) return;
        begun = true;
        KuudraBoss boss = fight.boss();
        if (boss != null) boss.leanIn(fight.centre(), stun.leanInBlocks());
        fight.bossState().retime(stun.fuelSurfaceSeconds(), fight.spec().boss().submergedSeconds(), fight.spec().boss().moveTicks());
        stomach.setOpen(fight.bossState().stance() == KuudraBossState.Stance.SURFACED);
        List<Integer> gaps = stun.elleEatenTicks();
        List<String> lines = fight.dialogue().chat("elle_eaten", ThreadLocalRandom.current());
        int at = 0;
        for (int i = 0; i < gaps.size(); i++) {
            at += gaps.get(i);
            if (i < lines.size()) {
                String line = lines.get(i);
                fight.later(at, () -> fight.run().broadcast(line));
            } else {
                fight.later(at, this::eatElle);
            }
        }
    }

    boolean begun() {
        return begun;
    }

    /** Fuel loaded: the objective turns to stunning him and his surface timer is cut (the captures). */
    void charged() {
        fight.bossState().submergeWithin(stun.chargedSurfaceSeconds(fight.tier()));
    }

    void eatElle() {
        if (elleEaten) return;
        elleEaten = true;
        fight.run().moveElle(geometry.elle());
        fight.run().broadcast(fight.dialogue().announcement("elle_eaten", Map.of()));
        fight.playAll("entity.player.burp", 0.6f);
        if (stomach.inside().isEmpty()) {
            List<String> lines = fight.dialogue().chat("belly_outside", ThreadLocalRandom.current());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                fight.later(rules.barkGapTicks() * (i + 1), () -> outside().forEach(player -> player.sendMessage(line)));
            }
        }
    }

    private List<SkyBlockPlayer> outside() {
        return fight.run().onlineMembers().stream().filter(player -> !stomach.isInside(player.getUuid())).toList();
    }

    void onSurfaced() {
        if (begun && !fight.finishing()) stomach.setOpen(true);
    }

    /** He went under: the stomach floods and everyone inside is launched out (wiki, Kuudra's stomach). */
    void onSubmerged() {
        stomach.setOpen(false);
        List<String> lines = fight.dialogue().chat("stomach_flooding", ThreadLocalRandom.current());
        for (UUID uuid : stomach.flush()) {
            SkyBlockPlayer player = fight.member(uuid);
            if (player == null) continue;
            lines.stream().limit(2).forEach(player::sendMessage);
            launchOut(player);
        }
        clearBacteria();
    }

    void tick(long tick) {
        if (!begun) return;
        tickZone();
        tickCannonballs(tick);
        if (spitAt >= 0 && tick >= spitAt) {
            spitAt = -1;
            for (UUID uuid : stomach.flush()) {
                SkyBlockPlayer player = fight.member(uuid);
                if (player != null) launchOut(player);
            }
            clearBacteria();
        }
        for (UUID uuid : stomach.inside()) {
            SkyBlockPlayer player = fight.member(uuid);
            if (player == null || fight.isGhost(player)) {
                stomach.leave(uuid);
                continue;
            }
            Pos at = player.getPosition();
            if (at.distance(geometry.throat()) <= 3) {
                stomach.leave(uuid);
                launchOut(player);
            } else if (!geometry.contains(at)) {
                stomach.leave(uuid);
            }
        }
        tickBacteria(tick);
        if (tick % 10 == 0) drawZone();
    }

    private boolean zoneActive() {
        KuudraBossState state = fight.bossState();
        return stomach.isOpen() && fight.boss() != null && state.stance() == KuudraBossState.Stance.SURFACED
                && !state.isStunned() && !fight.finishing();
    }

    /** The Consumption Zone: the platform under Kuudra's face. Standing in it long enough gets you swallowed. */
    boolean inZone(Pos at) {
        KuudraBoss boss = fight.boss();
        if (boss == null) return false;
        Pos base = boss.base();
        double reach = boss.width() / 2 + rules.consumptionMargin();
        return Math.hypot(at.x() - base.x(), at.z() - base.z()) <= reach
                && at.y() >= fight.arena().floorY() - 2 && at.y() <= fight.arena().floorY() + 12;
    }

    private void tickZone() {
        boolean active = zoneActive();
        for (SkyBlockPlayer player : fight.alivePlayers()) {
            if (stomach.isInside(player.getUuid())) continue;
            boolean standing = active && inZone(player.getPosition());
            if (stomach.standInZone(player.getUuid(), standing, rules.consumptionTicks())) eat(player);
        }
    }

    private void drawZone() {
        KuudraBoss boss = fight.boss();
        if (boss == null || !zoneActive()) return;
        double radius = boss.width() / 2 + rules.consumptionMargin();
        Pos base = boss.base();
        int points = (int) (radius * 6);
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            Pos at = new Pos(base.x() + Math.cos(angle) * radius, fight.arena().floorY() + 0.2, base.z() + Math.sin(angle) * radius);
            if (!fight.arena().onPlatform(at)) continue;
            fight.instance().sendGroupedPacket(new ParticlePacket(Particle.WITCH, at, Vec.ZERO, 0, 1));
        }
    }

    /** Human Cannonball while his stomach is open fires the player into his mouth. */
    boolean fireIntoMouth(SkyBlockPlayer player) {
        KuudraBoss boss = fight.boss();
        if (boss == null || !zoneActive()) return false;
        Vec toMouth = boss.mouth().sub(player.getPosition()).asVec();
        double seconds = rules.cannonballFlightTicks() / 20.0;
        player.setVelocity(toMouth.div(seconds).add(0, 4.9 * seconds, 0));
        cannonballs.put(player.getUuid(), fight.currentTick() + rules.cannonballFlightTicks());
        player.sendMessage("<a>You were fired into Kuudra's mouth!");
        return true;
    }

    private void tickCannonballs(long tick) {
        for (Map.Entry<UUID, Long> entry : cannonballs.entrySet()) {
            SkyBlockPlayer player = fight.member(entry.getKey());
            KuudraBoss boss = fight.boss();
            if (player == null || boss == null) {
                cannonballs.remove(entry.getKey());
                continue;
            }
            boolean arrived = player.getPosition().distance(boss.mouth()) <= boss.width() / 2 + 2;
            if (!arrived && tick < entry.getValue()) continue;
            cannonballs.remove(entry.getKey());
            if (!eat(player)) player.sendMessage("<c>Kuudra's mouth is shut!");
        }
    }

    /** Swallows a player into the stomach; false when it is shut or they are already inside. */
    boolean eat(SkyBlockPlayer player) {
        if (fight.isGhost(player) || !stomach.eat(player.getUuid(), fight.currentTick())) return false;
        fight.release(player);
        player.teleport(geometry.landing());
        player.setVelocity(Vec.ZERO);
        fight.run().broadcast(fight.dialogue().announcement("player_eaten", Map.of("player", player.getUsername())));
        fight.playAt(geometry.landing(), "entity.player.burp", 0.5f);
        if (elleEaten && greeted.add(player.getUuid())) {
            List<String> lines = fight.dialogue().chat("belly_inside", ThreadLocalRandom.current());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                fight.later(rules.barkGapTicks() / 2 * (i + 1), () -> {
                    if (stomach.isInside(player.getUuid())) player.sendMessage(line);
                });
            }
        }
        return true;
    }

    /** Kuudra spits the player out of his mouth toward the platform (the Burning capture). */
    void launchOut(SkyBlockPlayer player) {
        KuudraBoss boss = fight.boss();
        stomach.leave(player.getUuid());
        if (boss == null) {
            player.teleport(fight.arena().respawn());
            return;
        }
        Pos centre = fight.centre();
        Pos mouth = boss.mouth();
        Vec toCentre = new Vec(centre.x() - mouth.x(), 0, centre.z() - mouth.z());
        Vec heading = toCentre.lengthSquared() < 1e-6 ? new Vec(1, 0, 0) : toCentre.normalize();
        Pos out = new Pos(mouth.x(), boss.headBottom() + 4, mouth.z()).add(heading.mul(boss.width() / 2 + 1));
        player.teleport(out.withDirection(heading));
        player.setVelocity(heading.mul(Math.min(22, toCentre.length() * 0.6)).withY(14));
        fight.playAt(out, "entity.generic.explode", 1.4f);
    }

    /** A swing inside the stomach with a pickaxe chips at the pod being looked at. */
    boolean swing(SkyBlockPlayer player) {
        if (!stomach.isInside(player.getUuid()) || !holdsPickaxe(player)) return false;
        String pod = site.podInSight(player.getPosition().add(0, player.getEyeHeight(), 0),
                player.getPosition().direction(), rules.podReach());
        if (pod == null) return false;
        double damage = KuudraStomach.podDamage(rules.podHitPercent(), fight.perks().secondValue(player.getUuid(), "MINING_FRENZY"));
        int cooldown = KuudraStomach.hitCooldown(rules.podHitCooldownTicks(), fight.perks().fraction(player.getUuid(), "MINING_FRENZY"));
        KuudraStomach.Hit hit = stomach.hit(player.getUuid(), pod, fight.currentTick(), damage, cooldown);
        if (hit == KuudraStomach.Hit.NONE) return false;
        site.show(pod, stomach.health(pod));
        fight.playAt(player.getPosition(), "block.amethyst_block.break", 0.7f);
        if (hit == KuudraStomach.Hit.DESTROYED) podDestroyed(player, pod);
        return true;
    }

    private static boolean holdsPickaxe(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) return false;
        if (KuudraItems.kindOf(held) == KuudraItems.Kind.ELLES_PICKAXE) return true;
        if (held.material().key().value().contains("pickaxe")) return true;
        try {
            SkyBlockItem item = new SkyBlockItem(held);
            return item.hasComponent(PickaxeComponent.class) || item.hasComponent(DrillComponent.class);
        } catch (Exception exception) {
            return false;
        }
    }

    private void podDestroyed(@Nullable SkyBlockPlayer player, String pod) {
        stomach.destroy(pod);
        site.breakPod(pod);
        if (player != null) {
            fight.run().broadcast(fight.dialogue().announcement("pod_destroyed", Map.of("player", player.getUsername())));
        }
        stunNow();
    }

    /** Stuns Kuudra now (a broken pod, or staff); true if he was up to be stunned. */
    boolean stunNow() {
        if (!fight.bossState().stun(stun.stunSeconds() * 20)) return false;
        KuudraBoss boss = fight.boss();
        if (boss != null) boss.setSag(stun.sagBlocks());
        stomach.setOpen(false);
        spitAt = fight.currentTick() + rules.exitDelayTicks();
        Text subtitle = Text.parse(fight.dialogue().announcement("kuudra_stunned", Map.of()));
        fight.run().onlineMembers().forEach(member -> member.showTitle(Title.title(Text.empty().asComponent(),
                subtitle.asComponent(), Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ZERO))));
        fight.playAll("entity.wither.hurt", 0.6f);
        fight.run().enterPhase(KuudraPhase.DPS);
        return true;
    }

    /** Staff: break the first standing pod as if the player had mined it. */
    boolean breakAPod(SkyBlockPlayer player) {
        for (String pod : stomach.pods()) {
            if (stomach.health(pod) <= 0) continue;
            if (fight.bossState().stance() != KuudraBossState.Stance.SURFACED) return false;
            podDestroyed(player, pod);
            return true;
        }
        return false;
    }

    /** The stun wore off: pods grow back and the stomach opens again. */
    void onRecovered() {
        KuudraBoss boss = fight.boss();
        if (boss != null) boss.setSag(0);
        site.restore();
        stomach.restore();
        stomach.setOpen(!fight.finishing());
        if (!fight.finishing()) fight.run().resumePhase(fight.armed() ? KuudraPhase.STUN : KuudraPhase.FUEL);
    }

    /** A weapon hit on a stunned Kuudra counts toward the hit-based DPS check (research K1 3.4). */
    boolean stunnedHit(SkyBlockPlayer player, boolean arrow) {
        if (!fight.bossState().isStunned()) return false;
        long tick = fight.currentTick();
        boolean counted = arrow ? hits.arrow(player.getUuid(), tick) : hits.melee(player.getUuid(), tick);
        if (counted) fight.damageKuudraPoints(points(stun, fight.tier(), arrow));
        return true;
    }

    static double points(KuudraFightSpec.Stun stun, KuudraTier tier, boolean arrow) {
        double points = stun.stunnedHitPoints().get(tier);
        return arrow ? points * stun.stunnedArrowHitFactor() : points;
    }

    private void tickBacteria(long tick) {
        bacteria.removeIf(mob -> mob.isDead() || mob.isRemoved());
        long since = stomach.occupiedSince();
        if (since < 0) {
            if (!bacteria.isEmpty()) clearBacteria();
            return;
        }
        if (tick - since < rules.bacteriaFirstSeconds() * 20L) return;
        if (tick - lastBacteria < rules.bacteriaEverySeconds() * 20L || bacteria.size() >= rules.bacteriaMax()) return;
        if (geometry.bacteria().isEmpty()) return;
        lastBacteria = tick;
        Pos at = geometry.bacteria().get(ThreadLocalRandom.current().nextInt(geometry.bacteria().size()));
        KuudraMob mob = fight.spawnMob(MagmaBacteria.ID, at);
        if (mob == null) return;
        bacteria.add(mob);
        if (!bacteriaBarked && elleEaten) {
            bacteriaBarked = true;
            List<String> lines = fight.dialogue().chat("belly_bacteria", ThreadLocalRandom.current());
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                fight.later(rules.barkGapTicks() / 2 * i + 1, () -> fight.stomachPlayers().forEach(player -> player.sendMessage(line)));
            }
        }
    }

    private void clearBacteria() {
        bacteria.forEach(KuudraMob::remove);
        bacteria.clear();
    }

    boolean isInside(SkyBlockPlayer player) {
        return stomach.isInside(player.getUuid());
    }

    Set<UUID> inside() {
        return stomach.inside();
    }

    void leave(SkyBlockPlayer player) {
        stomach.leave(player.getUuid());
        cannonballs.remove(player.getUuid());
    }

    /** Closes everything for the end of the fight or the lair: flush, bacteria gone, pods back. */
    void close() {
        stomach.setOpen(false);
        spitAt = -1;
        for (UUID uuid : stomach.flush()) {
            SkyBlockPlayer player = fight.member(uuid);
            if (player != null) launchOut(player);
        }
        cannonballs.clear();
        clearBacteria();
        site.restore();
        stomach.restore();
    }

    void dispose() {
        close();
        site.remove();
    }

    KuudraStomach stomach() {
        return stomach;
    }
}
