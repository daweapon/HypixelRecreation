package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Bal (wiki: Bal): the Magma Fields boss in Khazad-dum. He hops toward the nearest player and throws a fireball each
 * hop, knocks back whoever he lands on, shrinks as he is hurt and comments at 75%, 50% and 33% health. Every hit shakes
 * loose Bald Blazes and a steady stream of Fire Bats keeps coming. Whoever hurt him when he sinks earns the Topaz
 * Crystal, then his remains sink for 25 seconds. Drops roll on every hit. The wiki gives no real health, so he takes 60 hits (hitDamage of his 200 health each); the hop interval and
 * power, fireball and contact damage, blaze and bat counts, caps and his lines are estimates.
 */
public class MobBal extends CrystalHollowsBossMob {
    private static final float MAX_HEALTH = 200;
    private static final float HIT_DAMAGE = MAX_HEALTH / 60;
    private static final int BLAZES_PER_HIT = 2;
    private static final int MAX_BLAZES = 14;
    private static final int BATS_PER_WAVE = 3;
    private static final int MAX_BATS = 12;
    private static final int BAT_INTERVAL = 100;
    private static final int HOP_INTERVAL = 30;
    private static final int CONTACT_INTERVAL = 10;
    private static final double HOP_SPEED = 14;
    private static final double HOP_LIFT = 9;
    private static final double AGGRO_RANGE = 40;
    private static final double CONTACT_KNOCKBACK = 12;
    private static final double FIREBALL_DAMAGE_SCALE = 1;
    private static final long INVULNERABLE_MILLIS = 300;
    private static final double[] LINE_THRESHOLDS = {0.75, 0.5, 0.33};
    private static final String[] LINES = {
            "<c>You dare wound a mountain of magma?",
            "<c>The fire in me only burns hotter!",
            "<c>I will not return to the lava... not yet!"};
    private static final double SUMMON_SPREAD = 5;
    private static final int MAGMA_CUBE_SIZE = 12;
    private static final int SINK_START_SIZE = 4;
    private static final long SINK_MILLIS = 25_000;

    private final List<CrimsonIsleMob> blazes = new ArrayList<>();
    private final List<CrimsonIsleMob> bats = new ArrayList<>();
    private int linesSpoken;

    public MobBal() {
        super(CrystalHollowsBossProfiles.BAL);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        CrystalNucleus.revokeAvailable(NucleusCrystal.TOPAZ);
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(MAGMA_CUBE_SIZE));
    }

    @Override
    protected boolean rollsLootOnKill() {
        return false;
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    public long damageCooldown() {
        return INVULNERABLE_MILLIS;
    }

    @Override
    protected float hitDamage() {
        return HIT_DAMAGE;
    }

    @Override
    protected double hitMultiplier(SkyBlockPlayer attacker) {
        return MiningPetBonuses.cannibalMultiplier(attacker);
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        CrimsonMobLoot.roll(player, this, getProfile().drops());
        blazes.removeIf(mob -> mob.isDead() || mob.isRemoved());
        for (int i = 0; i < BLAZES_PER_HIT && blazes.size() < MAX_BLAZES; i++) {
            blazes.add(summon(MobBaldBlaze::new));
        }
        shrinkWithHealth();
        speakAtThresholds();
    }

    private int sizeFor(double fraction) {
        return Math.max(2, (int) Math.round(MAGMA_CUBE_SIZE * Math.max(0, Math.min(1, fraction))));
    }

    private void shrinkWithHealth() {
        int size = sizeFor(getHealth() / MAX_HEALTH);
        editEntityMeta(MagmaCubeMeta.class, meta -> {
            if (meta.getSize() != size) meta.setSize(size);
        });
    }

    private void speakAtThresholds() {
        double fraction = getHealth() / MAX_HEALTH;
        while (linesSpoken < LINE_THRESHOLDS.length && fraction <= LINE_THRESHOLDS[linesSpoken]) {
            String line = "<c>[BOSS] Bal<f>: " + LINES[linesSpoken++];
            playersWithin(AGGRO_RANGE).forEach(player -> player.sendMessage(line));
        }
    }

    @Override
    protected void onAiTick(int ticks) {
        List<SkyBlockPlayer> fighters = playersWithin(AGGRO_RANGE);
        if (fighters.isEmpty()) return;
        if (ticks % BAT_INTERVAL == 0) {
            bats.removeIf(mob -> mob.isDead() || mob.isRemoved());
            for (int i = 0; i < BATS_PER_WAVE && bats.size() < MAX_BATS; i++) bats.add(summon(MobFireBat::new));
        }
        if (ticks % HOP_INTERVAL == 0 && isOnGround()) hop(fighters);
        if (ticks % CONTACT_INTERVAL == 0) knockBackLandedOn(fighters);
    }

    private void hop(List<SkyBlockPlayer> fighters) {
        SkyBlockPlayer target = fighters.stream()
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())))
                .orElseThrow();
        Vec toward = target.getPosition().sub(getPosition()).asVec().withY(0);
        Vec heading = toward.length() < 0.001 ? Vec.ZERO : toward.normalize().mul(HOP_SPEED);
        setVelocity(heading.withY(HOP_LIFT));
        fireball(target, getProfile().damage() * FIREBALL_DAMAGE_SCALE);
    }

    private void knockBackLandedOn(List<SkyBlockPlayer> fighters) {
        double reach = getBoundingBox().width() / 2 + 1;
        for (SkyBlockPlayer player : fighters) {
            if (player.getPosition().distance(getPosition()) > reach) continue;
            Vec away = player.getPosition().sub(getPosition()).asVec().withY(0);
            if (away.length() > 0.001) player.setVelocity(away.normalize().mul(CONTACT_KNOCKBACK).withY(6));
            strike(player, getProfile().damage());
        }
    }

    private CrimsonIsleMob summon(Supplier<CrimsonIsleMob> factory) {
        CrimsonIsleMob mob = factory.get();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Pos at = getPosition().add((random.nextDouble() - 0.5) * 2 * SUMMON_SPREAD, 1, (random.nextDouble() - 0.5) * 2 * SUMMON_SPREAD);
        mob.setInstance(getInstance(), at);
        return mob;
    }

    @Override
    protected void reward(SkyBlockPlayer player) {
        PetDropTable.of("BAL_PET")
                .rarity(Rarity.EPIC, 0.0126)
                .rarity(Rarity.LEGENDARY, 0.0054)
                .roll(player, this);
        claimTopazCrystal(player);
    }

    private void claimTopazCrystal(SkyBlockPlayer player) {
        if (CrystalNucleus.state(player, NucleusCrystal.TOPAZ) != CrystalNucleus.State.NOT_FOUND) return;
        CrystalNucleus.makeAvailable(player, NucleusCrystal.TOPAZ);
        player.sendMessage(Text.of("<d>A {}<d> has appeared in Khazad-dûm!", NucleusCrystal.TOPAZ.coloured()));
    }

    @Override
    protected void onDefeated() {
        sinkIntoLava();
        despawn(blazes);
        despawn(bats);
    }

    @Override
    public void remove() {
        despawn(blazes);
        despawn(bats);
        super.remove();
    }

    private void sinkIntoLava() {
        Instance instance = getInstance();
        if (instance == null) return;
        EntityCreature remains = new EntityCreature(EntityType.MAGMA_CUBE);
        remains.editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(SINK_START_SIZE));
        remains.setNoGravity(true);
        remains.setInstance(instance, getPosition());
        int steps = SINK_START_SIZE;
        for (int step = 1; step <= steps; step++) {
            int size = SINK_START_SIZE - step;
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (remains.isRemoved()) return;
                if (size <= 0) remains.remove();
                else remains.editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(size));
            }).delay(TaskSchedule.millis(SINK_MILLIS / steps * step)).schedule();
        }
    }

    private static void despawn(List<CrimsonIsleMob> mobs) {
        mobs.forEach(mob -> {
            if (!mob.isRemoved()) mob.remove();
        });
        mobs.clear();
    }
}
