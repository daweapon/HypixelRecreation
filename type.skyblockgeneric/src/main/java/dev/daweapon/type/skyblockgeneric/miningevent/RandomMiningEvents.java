package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.GoldenGoblin;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobPowderGhast;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.MobStarSentry;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmSkyMallBuff;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomMiningEvents {
    private static final double STAR_EFFECT_RADIUS = 20;
    private static final double SITE_REACH = 70;
    private static final double GOBLIN_SKY_MALL_MULTIPLIER = 10;
    private static final double PERCENT = 100;

    private static final List<Entity> starProps = new ArrayList<>();
    private static Pos starPosition;
    private static long starEndsAt;
    private static long starCooldownUntil;
    private static long ghastCooldownUntil;

    private RandomMiningEvents() {
    }

    public static synchronized void onOreMined(SkyBlockPlayer player, Point block, boolean mithril) {
        MiningEventZone zone = MiningEventZone.ofServer();
        if (zone != MiningEventZone.DWARVEN_MINES) return;
        long now = System.currentTimeMillis();
        MiningEventConfig config = MiningEventConfig.get();
        double luck = 1 + HotmService.perkValue(player, "luck_of_the_cave") / PERCENT;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (now >= starCooldownUntil && starPosition == null
                && random.nextDouble() * PERCENT < config.number(0.02, "random", "fallen_star", "chance_percent_per_ore") * luck) {
            spawnFallenStar();
        }
        if (mithril && now >= ghastCooldownUntil
                && random.nextDouble() * PERCENT < config.number(0.02, "random", "powder_ghast", "chance_percent_per_mithril") * luck) {
            spawnPowderGhast(player.getPosition());
        }
        double goblin = config.number(0.01, "random", "golden_goblin", "chance_percent_per_ore") * luck
                * (HotmService.skyMallBuff(player) == HotmSkyMallBuff.GOBLINS ? GOBLIN_SKY_MALL_MULTIPLIER : 1);
        if (random.nextDouble() * PERCENT < goblin) spawnGoldenGoblin(player);
    }

    public static synchronized boolean spawnFallenStar() {
        MiningEventConfig config = MiningEventConfig.get();
        List<MiningEventConfig.Site> sites = config.sites("random", "fallen_star", "sites");
        if (sites.isEmpty()) return false;
        endFallenStar();
        Instance instance = HypixelConst.getInstanceContainer();
        MiningEventConfig.Site site = EventSites.pick(sites);
        Pos ground = EventSites.near(instance, site);
        long now = System.currentTimeMillis();
        starPosition = ground;
        starEndsAt = now + (long) (config.number(340, "random", "fallen_star", "duration_seconds") * 1000);
        starCooldownUntil = now + (long) (config.number(10, "random", "fallen_star", "cooldown_minutes") * 60_000);

        Entity crystal = new Entity(EntityType.END_CRYSTAL);
        crystal.setNoGravity(true);
        crystal.setGlowing(true);
        crystal.setInstance(instance, ground.add(0, 1.5, 0));
        starProps.add(crystal);
        int sentries = config.integer(4, "random", "fallen_star", "sentries");
        double radius = config.number(6, "random", "fallen_star", "sentry_radius");
        for (int index = 0; index < sentries; index++) {
            MobStarSentry sentry = new MobStarSentry();
            sentry.setInstance(instance, EventSites.near(instance, new MiningEventConfig.Site(site.name(), ground.x(), ground.y(), ground.z(), radius), radius));
            starProps.add(sentry);
        }
        MiningEvents.broadcast("<5>✯ <e>A <5>Fallen Star <e>has crashed at <2>" + site.name() + "<e>! Nearby ore and Powder drops are amplified!");
        MinecraftServer.getSchedulerManager().buildTask(RandomMiningEvents::expireStar)
                .delay(TaskSchedule.seconds((long) config.number(340, "random", "fallen_star", "duration_seconds"))).schedule();
        return true;
    }

    private static synchronized void expireStar() {
        if (starPosition != null && System.currentTimeMillis() >= starEndsAt) endFallenStar();
    }

    public static synchronized void endFallenStar() {
        starProps.forEach(entity -> {
            if (!entity.isRemoved()) entity.remove();
        });
        starProps.clear();
        starPosition = null;
    }

    public static synchronized boolean nearFallenStar(Point point) {
        return starPosition != null && starPosition.distance(point) <= STAR_EFFECT_RADIUS;
    }

    public static synchronized boolean spawnPowderGhast(Point near) {
        MiningEventConfig config = MiningEventConfig.get();
        List<MiningEventConfig.Site> sites = config.sites("random", "powder_ghast", "sites");
        MiningEventConfig.Site closest = null;
        double best = Double.MAX_VALUE;
        for (MiningEventConfig.Site site : sites) {
            double distance = Math.hypot(site.x() - near.x(), site.z() - near.z());
            if (distance < best) {
                best = distance;
                closest = site;
            }
        }
        if (closest == null || best > SITE_REACH) return false;
        Instance instance = HypixelConst.getInstanceContainer();
        ghastCooldownUntil = System.currentTimeMillis() + (long) (config.number(10, "random", "powder_ghast", "cooldown_minutes") * 60_000);
        MobPowderGhast ghast = new MobPowderGhast();
        ghast.setInstance(instance, EventSites.near(instance, closest).add(0, 6, 0));
        MiningEvents.broadcast("<6>The sound of pickaxes clashing against the rock has attracted the attention of the <l>POWDER GHAST<r><6>!");
        MiningEvents.broadcast("<e>Find the <6>Powder Ghast <e>near the <b>" + closest.name() + "<e>!");
        return true;
    }

    public static boolean spawnGoldenGoblin(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        GoldenGoblin goblin = new GoldenGoblin();
        goblin.setInstance(instance, player.getPosition().add(player.getPosition().direction().mul(3)));
        long lifetime = (long) MiningEventConfig.get().number(30, "random", "golden_goblin", "lifetime_seconds");
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!goblin.isRemoved() && !goblin.isDead()) goblin.remove();
        }).delay(TaskSchedule.seconds(lifetime)).schedule();
        return true;
    }

    public static void applyFallenStar(SkyBlockPlayer player, Point block, List<SkyBlockItem> drops, boolean mithril, long powderGained) {
        if (!nearFallenStar(block)) return;
        MiningEventConfig config = MiningEventConfig.get();
        int ore = config.integer(2, "random", "fallen_star", "bonus_ore_drops");
        drops.forEach(drop -> drop.setAmount(drop.getAmount() + ore));
        if (mithril) {
            long powder = config.integer(2, "random", "fallen_star", "bonus_mithril_powder");
            double stellar = ReforgeLoadout.starPowderPercent(player);
            powder += Math.round(powderGained * stellar / PERCENT);
            HotmService.addFlatPowder(player, HotmPowder.MITHRIL, MiningEvents.doublePowder() ? powder * 2 : powder);
        }
        if (LootRoll.roll(player.getUuid(), ItemType.STARFALL, config.number(2, "random", "fallen_star", "starfall_chance_percent"))) {
            drops.add(new SkyBlockItem(ItemType.STARFALL, 1));
        }
    }

    public static @Nullable Pos fallenStarPosition() {
        return starPosition;
    }
}
