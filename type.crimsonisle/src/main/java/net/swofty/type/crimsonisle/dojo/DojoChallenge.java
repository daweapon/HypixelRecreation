package net.swofty.type.crimsonisle.dojo;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * One run of a Dojo test. The session ticks it every server tick while it runs and cleans up the arena
 * when it ends; subclasses build their platform in {@link #build()} and score in {@link #tick()}.
 */
public abstract class DojoChallenge {
    public static final int TICKS_PER_SECOND = 20;

    @Getter
    protected final SkyBlockPlayer player;
    @Getter
    private final DojoTest test;
    protected final DojoArena arena = new DojoArena();
    protected final List<Entity> entities = new ArrayList<>();
    protected long ticks;
    private double score;
    private boolean failed;

    protected DojoChallenge(SkyBlockPlayer player, DojoTest test) {
        this.player = player;
        this.test = test;
    }

    protected abstract void build();

    protected abstract Pos spawn();

    protected void tick() {
    }

    protected List<ItemStack> kit() {
        return List.of();
    }

    public void onAttack(Entity target) {
    }

    public void onArrowHit(Point block) {
    }

    public void onBowRelease(long useTicks) {
    }

    /** Timed tests send a fallen player back to the platform; survival tests end. */
    protected void onFall() {
        if (test.isTimed()) {
            player.teleport(spawn());
        } else {
            fail();
        }
    }

    public final Pos begin() {
        build();
        List<ItemStack> kit = kit();
        for (int slot = 0; slot < kit.size(); slot++) player.getInventory().setItemStack(slot, kit.get(slot));
        return spawn();
    }

    public final void update() {
        ticks++;
        if (DojoArena.isInLava(player.getPosition())) onFall();
        if (!failed) tick();
    }

    public void cleanup() {
        entities.forEach(entity -> {
            if (!entity.isRemoved()) entity.remove();
        });
        entities.clear();
        arena.restoreAll();
    }

    public int score() {
        return (int) Math.floor(score);
    }

    protected void addScore(double delta) {
        score += delta;
    }

    protected void setScore(double value) {
        score = value;
    }

    public boolean isFailed() {
        return failed;
    }

    protected void fail() {
        failed = true;
    }

    public boolean isOver() {
        return failed || (test.isTimed() && ticks >= (long) test.getTimeLimitSeconds() * TICKS_PER_SECOND);
    }

    public long elapsedMillis() {
        return ticks * 50L;
    }

    protected <T extends Entity> T track(T entity) {
        entities.add(entity);
        return entity;
    }

    protected void sound(String key, float pitch) {
        player.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, 1f, pitch), Sound.Emitter.self());
    }

    protected double seconds() {
        return ticks / (double) TICKS_PER_SECOND;
    }
}
