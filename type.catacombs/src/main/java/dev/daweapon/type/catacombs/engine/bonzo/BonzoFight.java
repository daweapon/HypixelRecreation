package dev.daweapon.type.catacombs.engine.bonzo;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.type.catacombs.engine.CatacombsMob;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.catacombs.engine.look.MobLook;
import dev.daweapon.type.catacombs.engine.look.MobLooks;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs Bonzo's fight in the captured arena by playing {@link BonzoScript}: his lines in chat and over his head, the
 * boss bar ({@code §c§lBonzo}, red, following his health and refilling for his second life), the intro and revive
 * ambience, his dash, the summoning orbs that land as Undead, his Wither Skulls, and in his second life Bonzo's
 * Balloon and Balloon Barrage. When he is beaten the end of the run follows the script: score, exit gates, last lines.
 */
public final class BonzoFight implements BonzoBoss.Fight {
    public static final String PREFIX = "<c>[BOSS] Bonzo<f>: ";
    public static final String BAR_TITLE = "<c><l>Bonzo";
    static final int ORB_TICKS = 45;
    static final double ORB_PEAK = 9.5;
    static final int MAX_UNDEAD = 8;
    static final double UNDEAD_SPEED = 116;
    static final double DASH_DISTANCE = 7;
    static final double SKULL_SPEED = 0.8;
    static final double SKULL_DAMAGE = 1_598;
    static final int BARRAGE_BALLOONS = 12;
    static final int GATE_CLICKS = 9;
    static final int GATE_CLICK_GAP = 5;

    private static final Sound AMBIENCE_BASE = Sound.sound(Key.key("entity.zombie_villager.converted"), Sound.Source.HOSTILE, 1f, 1f);
    private static final Sound TELEPORT = Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.HOSTILE, 1f, 1f);
    private static final Sound ORB = Sound.sound(Key.key("entity.zombie.infect"), Sound.Source.HOSTILE, 1f, 2f);
    private static final Sound SUMMONED = Sound.sound(Key.key("entity.zombie_villager.cure"), Sound.Source.HOSTILE, 1f, 2f);
    private static final Sound SHOOT = Sound.sound(Key.key("entity.wither.shoot"), Sound.Source.HOSTILE, 0.2f, 1.4920635f);
    private static final Sound GOLEM = Sound.sound(Key.key("entity.iron_golem.attack"), Sound.Source.MASTER, 1f, 1.3968254f);
    private static final Sound DOOR = Sound.sound(Key.key("entity.zombie.break_wooden_door"), Sound.Source.MASTER, 0.25f, 1.3333334f);
    private static final Sound PLING = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.BLOCK, 8f, 4.047619f);
    private static final Sound CLICK = Sound.sound(Key.key("block.wooden_pressure_plate.click_on"), Sound.Source.MASTER, 1f, 0.4920635f);
    private static final Particle ORB_DUST = Particle.DUST.withColor(new Color(25, 25, 25));

    /** What the run does at the end of the fight. */
    public interface Ending {
        void scoreScreen();

        void openGate(int gate);
    }

    private final DungeonHost host;
    private final Ending ending;
    private final BonzoScript script;
    private final List<CatacombsMob> undead = new ArrayList<>();
    private final List<Orb> orbs = new ArrayList<>();
    private BonzoBoss bonzo;
    private BossBar bar;
    private Task ticker;
    private long nextAmbience;
    private boolean disposed;

    private record Orb(Pos from, Pos to, long launched) {
    }

    public BonzoFight(DungeonHost host, Ending ending) {
        this.host = host;
        this.ending = ending;
        this.script = new BonzoScript(ThreadLocalRandom.current());
    }

    public synchronized void start() {
        DungeonMobDefinition definition = DungeonMobRegistry.defaults().mob("BONZO");
        MobLook first = MobLooks.shared().look("BONZO").orElseGet(() -> CatacombsMob.lookOf("BONZO"));
        bonzo = new BonzoBoss(definition, first, this, BonzoArenaSpots.BONZO_SPAWN);
        bonzo.setInstance(host.instance(), BonzoArenaSpots.BONZO_SPAWN);
        bar = BossBar.bossBar(Text.parse(BAR_TITLE).asComponent(), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        host.alivePlayers().forEach(player -> player.showBossBar(bar));
        ticker = MinecraftServer.getSchedulerManager().buildTask(this::tick).repeat(TaskSchedule.nextTick()).schedule();
    }

    private synchronized void tick() {
        if (disposed) return;
        perform(script.tick());
        flyOrbs();
        ambience();
        bar.progress(bonzo.down() ? 0f : Math.clamp(bonzo.getHealth() / bonzo.getMaxHealth(), 0f, 1f));
        if (script.finished()) ticker.cancel();
    }

    private void perform(List<BonzoScript.Action> actions) {
        for (BonzoScript.Action action : actions) {
            switch (action) {
                case BonzoScript.Say(String line) -> say(line);
                case BonzoScript.StartMoving() -> startMoving();
                case BonzoScript.SummonOrb() -> launchOrb();
                case BonzoScript.SkullVolley(boolean toCentre) -> skull(toCentre);
                case BonzoScript.Revive() -> revive();
                case BonzoScript.Balloon() -> balloon();
                case BonzoScript.BalloonBarrage() -> barrage();
                case BonzoScript.DeathSounds() -> deathSounds();
                case BonzoScript.ScoreScreen() -> ending.scoreScreen();
                case BonzoScript.OpenGate(int gate) -> openGate(gate);
            }
        }
    }

    private void say(String line) {
        host.broadcast(PREFIX + line);
        bonzo.say(line);
    }

    private void startMoving() {
        Pos from = bonzo.getPosition();
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        Pos to = from.add(Math.cos(angle) * DASH_DISTANCE, 0, Math.sin(angle) * DASH_DISTANCE);
        teleport(to);
        bonzo.vulnerable(true);
        bonzo.stance(BonzoBoss.Stance.WANDER);
    }

    private void teleport(Pos to) {
        Instance instance = host.instance();
        Pos from = bonzo.getPosition();
        instance.playSound(TELEPORT, from.x(), from.y() + 1, from.z());
        instance.sendGroupedPacket(new ParticlePacket(Particle.PORTAL, from.x(), from.y() + 1, from.z(), 1f, 1f, 1f, 0.1f, 10));
        bonzo.teleport(to.withView(from.yaw(), from.pitch()));
        instance.playSound(TELEPORT, to.x(), to.y(), to.z());
    }

    private void launchOrb() {
        if (undead.size() + orbs.size() >= MAX_UNDEAD) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<SkyBlockPlayer> players = host.alivePlayers();
        Pos self = bonzo.getPosition();
        Pos target = players.isEmpty() || random.nextBoolean() ? self
                : blockCentre(players.get(random.nextInt(players.size())).getPosition());
        orbs.add(new Orb(self.add(0, 2, 0), target, script.now()));
    }

    private static Pos blockCentre(Pos at) {
        return new Pos(Math.floor(at.x()) + 0.5, Math.floor(at.y()), Math.floor(at.z()) + 0.5);
    }

    private void flyOrbs() {
        Instance instance = host.instance();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        orbs.removeIf(orb -> {
            long age = script.now() - orb.launched();
            Pos at = orbPosition(orb.from(), orb.to(), (double) age / ORB_TICKS);
            instance.sendGroupedPacket(new ParticlePacket(ORB_DUST, true, true, at.x(), at.y(), at.z(), 0.1f, 0.1f, 0.1f, 0f, 1));
            instance.sendGroupedPacket(new ParticlePacket(Particle.ENTITY_EFFECT.withColor(new net.minestom.server.color.AlphaColor(
                    255, random.nextInt(256), random.nextInt(256), random.nextInt(256))), true, true, at.x(), at.y(), at.z(),
                    0.1f, 0.1f, 0.1f, 1f, 1));
            if (age % 3 == 0) instance.playSound(ORB, at.x(), at.y(), at.z());
            if (age < ORB_TICKS) return false;
            summonUndead(orb.to());
            return true;
        });
    }

    /** The orb's path: straight across from Bonzo to where the Undead will stand, arcing about 9.5 blocks up. */
    static Pos orbPosition(Pos from, Pos to, double progress) {
        double t = Math.clamp(progress, 0, 1);
        double x = from.x() + (to.x() - from.x()) * t;
        double z = from.z() + (to.z() - from.z()) * t;
        double y = from.y() + (to.y() + 1 - from.y()) * t + 4 * ORB_PEAK * t * (1 - t);
        return new Pos(x, y, z);
    }

    private void summonUndead(Pos at) {
        DungeonMobDefinition definition = DungeonMobRegistry.defaults().mob("BONZO_UNDEAD");
        MobLook look = MobLooks.shared().look("BONZO_UNDEAD").orElseGet(() -> CatacombsMob.lookOf("BONZO_UNDEAD"));
        CatacombsMob mob = new CatacombsMob(definition, -1, false, this, look, UNDEAD_SPEED);
        undead.add(mob);
        mob.setInstance(host.instance(), at.withYaw(ThreadLocalRandom.current().nextFloat(-180, 180)));
        host.instance().playSound(SUMMONED, at.x(), at.y() + 2.25, at.z());
    }

    private void skull(boolean toCentre) {
        if (toCentre) teleport(BonzoArenaSpots.BONZO_SPAWN);
        SkyBlockPlayer target = bonzo.nearestPlayer(48, player -> true).orElse(null);
        if (target == null) return;
        Pos at = bonzo.getPosition();
        host.instance().playSound(SHOOT, at.x(), at.y(), at.z());
        MobProjectile.witherSkull(bonzo, target, SKULL_SPEED, hit -> hit(hit, "Bonzo's Wither Skull"));
    }

    private void balloon() {
        SkyBlockPlayer target = bonzo.nearestPlayer(48, player -> true).orElse(null);
        if (target == null) return;
        Pos at = bonzo.getPosition();
        host.instance().playSound(SHOOT, at.x(), at.y(), at.z());
        MobProjectile.witherSkull(bonzo, target, SKULL_SPEED, hit -> hit(hit, "Bonzo's Balloon"));
    }

    private void barrage() {
        teleport(BonzoArenaSpots.BONZO_SPAWN);
        Pos at = bonzo.getPosition();
        host.instance().playSound(SHOOT, at.x(), at.y(), at.z());
        for (int index = 0; index < BARRAGE_BALLOONS; index++) {
            double angle = Math.PI * 2 * index / BARRAGE_BALLOONS;
            MobProjectile.witherSkull(bonzo, new Vec(Math.cos(angle), 0, Math.sin(angle)), SKULL_SPEED,
                    hit -> hit(hit, "Bonzo's Balloon"));
        }
    }

    private void hit(SkyBlockPlayer player, String source) {
        float dealt = MobAttackService.strike(bonzo, player, SKULL_DAMAGE);
        Pos at = player.getPosition();
        float pitch = 0.7f + ThreadLocalRandom.current().nextFloat(-0.05f, 0.08f);
        host.instance().playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.BLOCK, 4f, pitch), at.x(), at.y() + 1, at.z());
        if (dealt > 0) player.sendMessage("<7>" + source + " hit you for <c>" + StringUtility.commaify((long) Math.ceil(dealt)) + "<7> damage.");
    }

    private void revive() {
        MobLook revived = MobLooks.shared().look("BONZO_REVIVED").orElseGet(() -> CatacombsMob.lookOf("BONZO"));
        bonzo.revive(revived);
        bonzo.vulnerable(true);
        bonzo.stance(BonzoBoss.Stance.CHASE);
    }

    private void deathSounds() {
        Pos at = bonzo.getPosition().add(0, 0.5, 0);
        for (int index = 0; index < 4; index++) host.instance().playSound(GOLEM, at.x(), at.y(), at.z());
        host.instance().playSound(DOOR, at.x(), at.y(), at.z());
    }

    private void openGate(int gate) {
        if (gate == 1) {
            host.alivePlayers().forEach(player -> player.playSound(PLING));
            Pos at = BonzoArenaSpots.GATE_SOUND;
            for (int click = 0; click < GATE_CLICKS; click++) {
                host.later(1 + click * GATE_CLICK_GAP, () -> host.instance().playSound(CLICK, at.x(), at.y(), at.z()));
            }
        }
        ending.openGate(gate);
    }

    private void ambience() {
        BonzoScript.Phase phase = script.phase();
        if (phase != BonzoScript.Phase.INTRO && phase != BonzoScript.Phase.DOWNED) return;
        if (script.now() < nextAmbience) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Pos at = bonzo.getPosition();
        host.instance().playSound(Sound.sound(AMBIENCE_BASE.name(), AMBIENCE_BASE.source(), 1f, 0.49f + random.nextFloat() * 0.49f),
                at.x(), at.y() + 1, at.z());
        nextAmbience = script.now() + 2 + random.nextInt(5);
    }

    @Override
    public synchronized void bonzoHurt(BonzoBoss boss) {
        perform(script.hurt());
    }

    @Override
    public synchronized void bonzoLifeLost(BonzoBoss boss) {
        perform(script.lifeLost());
    }

    @Override
    public synchronized void mobDied(CatacombsMob mob) {
        undead.remove(mob);
    }

    public BonzoScript script() {
        return script;
    }

    public BonzoBoss bonzo() {
        return bonzo;
    }

    public synchronized void showBar(SkyBlockPlayer player) {
        if (bar != null) player.showBossBar(bar);
    }

    public synchronized void hideBar(SkyBlockPlayer player) {
        if (bar != null) player.hideBossBar(bar);
    }

    /** Staff shortcut: takes Bonzo's current life away as if the party had beaten it. */
    public synchronized boolean killLife() {
        if (bonzo == null || !script.vulnerable()) return false;
        bonzo.kill();
        return true;
    }

    public synchronized void dispose() {
        disposed = true;
        if (ticker != null) ticker.cancel();
        undead.forEach(CatacombsMob::remove);
        undead.clear();
        orbs.clear();
        if (bonzo != null) bonzo.remove();
        if (bar != null) MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.hideBossBar(bar));
    }
}
