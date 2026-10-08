package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Fire Pillar: every 20 seconds the Demonlord charges a fireball for 3.5 seconds and throws it. It
 * lands as a 4-block pillar that must be hit 8 times within 7 seconds, shrinking a block every two
 * hits. If it survives, it deals 1,000 times their max health as true damage to everyone fighting.
 */
final class InfernoFirePillar implements SlayerMinionMob.Hooks {
    private static final int CYCLE_TICKS = 400;
    private static final int CHARGE_TICKS = 70;
    private static final int LIFETIME_TICKS = 140;
    private static final int HITS = 8;
    private static final double HEIGHT = 4;
    private static final double WIPE_MULTIPLIER = 1_000;

    private final InfernoDemonlordAbility ability;
    private final SlayerBossMob boss;
    private SlayerMinionMob hitbox;
    private Entity display;
    private int hitsTaken;
    private boolean resolved;

    private InfernoFirePillar(InfernoDemonlordAbility ability, SlayerBossMob boss) {
        this.ability = ability;
        this.boss = boss;
    }

    static void startCycle(InfernoDemonlordAbility ability, SlayerBossMob boss) {
        ability.every(boss, 20, CYCLE_TICKS, () -> {
            if (boss.isInvisible()) return;
            ability.announce(boss, "<6>The Demonlord is charging a <c>Fire Pillar<6>!");
            for (int tick = 0; tick < CHARGE_TICKS; tick += 10) {
                ability.schedule(boss, tick, () -> ability.particles(boss, Particle.LAVA, 0.4f, 6));
            }
            ability.schedule(boss, CHARGE_TICKS, () -> new InfernoFirePillar(ability, boss).land());
        });
    }

    private void land() {
        Instance instance = boss.getInstance();
        if (instance == null) return;
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        double distance = 3 + ThreadLocalRandom.current().nextDouble(3);
        Pos at = boss.getPosition().add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);

        display = new Entity(EntityType.BLOCK_DISPLAY);
        display.setNoGravity(true);
        BlockDisplayMeta meta = (BlockDisplayMeta) display.getEntityMeta();
        meta.setBlockState(Block.ORANGE_STAINED_GLASS);
        meta.setTranslation(new Vec(-0.5, 0, -0.5));
        meta.setScale(new Vec(1, HEIGHT, 1));
        display.setInstance(instance, at);

        hitbox = SlayerMinionMob.create(boss.getOwnerUuid(), new SlayerMinionMob.SlayerMinionProfile(
            "Fire Pillar", boss.getLevel(), EntityType.ARMOR_STAND, HITS, 0, 0, List.of(MobType.INFERNAL), this));
        hitbox.setInstance(instance, at);
        ability.announce(boss, "<c>A Fire Pillar erupted! <e>Hit it 8 times in 7 seconds!");
        ability.schedule(boss, LIFETIME_TICKS, this::explode);
    }

    private void explode() {
        if (resolved) return;
        resolved = true;
        cleanup();
        ability.announce(boss, "<4><l>The Fire Pillar exploded!");
        for (SkyBlockPlayer player : ability.fighters(boss)) {
            ability.hurt(boss, player, player.getMaxHealth() * WIPE_MULTIPLIER, "Fire Pillar", true);
        }
    }

    private void cleanup() {
        if (display != null && !display.isRemoved()) display.remove();
        if (hitbox != null && !hitbox.isRemoved() && !hitbox.isDead()) hitbox.remove();
    }

    @Override
    public boolean stationary() {
        return true;
    }

    @Override
    public boolean canAttack(SlayerMinionMob minion) {
        return false;
    }

    @Override
    public long damageCooldownMillis() {
        return 0L;
    }

    @Override
    public String nameHeader(SlayerMinionMob minion) {
        return "<6>" + Math.max(0, HITS - hitsTaken) + " hits left";
    }

    @Override
    public float modifyIncomingDamage(SlayerMinionMob minion, Damage damage, float amount) {
        if (resolved || !(damage.getAttacker() instanceof SkyBlockPlayer player)) return 0F;
        int hits = MiscPetHooks.breaksFirePillarsFaster(player) ? 2 : 1;
        if (hitsTaken < HITS && hitsTaken + hits >= HITS) MiscPetHooks.destroyedFirePillar(player);
        hitsTaken += hits;
        if (display != null) {
            double height = HEIGHT - Math.floor(hitsTaken / 2D);
            ((BlockDisplayMeta) display.getEntityMeta()).setScale(new Vec(1, Math.max(0.1, height), 1));
        }
        return minion.getMaxHealth() * hits / HITS;
    }

    @Override
    public void onDeath(SlayerMinionMob minion) {
        if (resolved) return;
        resolved = true;
        if (display != null && !display.isRemoved()) display.remove();
        ability.announce(boss, "<a>You destroyed the Fire Pillar!");
    }
}
