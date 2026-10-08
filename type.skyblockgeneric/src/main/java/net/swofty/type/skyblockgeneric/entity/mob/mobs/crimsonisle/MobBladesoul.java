package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.LavaBeam;
import net.swofty.type.skyblockgeneric.crimson.mob.RadialProjectiles;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossMob;
import net.swofty.type.skyblockgeneric.mythological.ability.MobProjectile;
import net.swofty.type.skyblockgeneric.mythological.ability.TemporaryBlocks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bladesoul (wiki: Bladesoul): swings a Ragnarock (3,000), throws wither skulls at everyone nearby (7,500), and
 * telegraphs a ring of skulls that kills anything it touches. He turns a block beside a player into Coal that
 * burns whoever stands on it; 10 s later a Wither Guard rises, and while it lives Bladesoul has 12.5M more
 * health. Above 100M max health he can't be hurt. Ability timings, the coal burn and the ring's 2 s telegraph
 * are estimates.
 */
public class MobBladesoul extends CrimsonMinibossMob {
    static final float GUARD_BONUS = 12_500_000;
    static final float INVINCIBLE_ABOVE = 100_000_000;
    private static final double ARENA = 20;
    private static final double SKULL_DAMAGE = 7_500;
    private static final double COAL_DAMAGE = 2_000;
    private static final int SKULL_INTERVAL = 160;
    private static final int RING_INTERVAL = 500;
    private static final int RING_TELEGRAPH = 40;
    private static final int COAL_INTERVAL = 600;
    private static final int COAL_TICKS = 200;

    private final TemporaryBlocks coal = new TemporaryBlocks();
    private @Nullable Pos coalBlock;
    private int coalTicks;
    private int ringTelegraph;
    private @Nullable MobBladesoulWitherGuard guard;

    public MobBladesoul() {
        super(CrimsonMobs.BLADESOUL);
    }

    @Override
    public CrimsonMiniboss miniboss() {
        return CrimsonMiniboss.BLADESOUL;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        getAttribute(Attribute.SCALE).setBaseValue(1.4);
        setItemInMainHand(ItemStack.of(Material.GOLDEN_AXE));
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return getMaxHealth() <= INVINCIBLE_ABOVE;
    }

    @Override
    protected void onAiTick(int ticks) {
        List<SkyBlockPlayer> fighters = fighters(ARENA);
        tickCoal(fighters);
        tickGuard();
        if (ringTelegraph > 0) {
            LavaBeam.ring(getInstance(), getPosition(), 2.5, Particle.SMOKE);
            if (--ringTelegraph == 0) {
                RadialProjectiles.fire(getInstance(), getPosition().add(0, 1.2, 0), 16, 0.6, ARENA,
                        ItemStack.of(Material.WITHER_SKELETON_SKULL), player -> trueStrike(player, player.getMaxHealth() * 10));
            }
            return;
        }
        if (fighters.isEmpty()) return;
        if (ticks % SKULL_INTERVAL == 0) {
            fighters.forEach(player -> MobProjectile.item(this, player, ItemStack.of(Material.WITHER_SKELETON_SKULL), 0.8,
                    hit -> strike(hit, SKULL_DAMAGE)));
        }
        if (ticks % RING_INTERVAL == 0) ringTelegraph = RING_TELEGRAPH;
        if (ticks % COAL_INTERVAL == 0 && coalBlock == null && guard == null) {
            placeCoal(fighters.get(ThreadLocalRandom.current().nextInt(fighters.size())));
        }
    }

    private void placeCoal(SkyBlockPlayer player) {
        Pos below = player.getPosition().sub(0, 1, 0);
        if (!coal.place(getInstance(), below, Block.COAL_BLOCK, true)) return;
        coalBlock = new Pos(below.blockX(), below.blockY(), below.blockZ());
        coalTicks = COAL_TICKS;
    }

    private void tickCoal(List<SkyBlockPlayer> fighters) {
        if (coalBlock == null) return;
        if (coalTicks % 20 == 0) {
            fighters.stream()
                    .filter(player -> player.getPosition().blockX() == coalBlock.blockX()
                            && player.getPosition().blockZ() == coalBlock.blockZ()
                            && Math.abs(player.getPosition().y() - (coalBlock.y() + 1)) < 0.6)
                    .forEach(player -> trueStrike(player, COAL_DAMAGE));
        }
        if (--coalTicks > 0) return;
        Pos rise = coalBlock.add(0.5, 1, 0.5);
        coal.restoreAll();
        coalBlock = null;
        guard = new MobBladesoulWitherGuard();
        guard.setInstance(getInstance(), rise);
        float health = getHealth();
        setMaxHealth(getMaxHealth() + GUARD_BONUS);
        setHealth(health + GUARD_BONUS);
        refreshName();
    }

    private void tickGuard() {
        if (guard == null || (!guard.isDead() && !guard.isRemoved())) return;
        guard = null;
        float health = getHealth();
        setMaxHealth(getMaxHealth() - GUARD_BONUS);
        setHealth(Math.max(1, health - GUARD_BONUS));
        refreshName();
    }

    @Override
    public void remove() {
        coal.restoreAll();
        if (guard != null) guard.remove();
        super.remove();
    }
}
