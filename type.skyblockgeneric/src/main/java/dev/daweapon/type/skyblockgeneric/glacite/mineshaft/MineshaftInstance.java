package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MineshaftInstance {
    private final UUID owner;
    private final ShaftLayout layout;
    private final MineshaftWorld world;
    private final Instance origin;
    private final long createdAt = System.currentTimeMillis();
    private final Set<UUID> departed = ConcurrentHashMap.newKeySet();
    private final Set<UUID> dead = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> scraps = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> corpses = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> announcedSteps = new ConcurrentHashMap<>();
    private final Map<UUID, MayhemBuff> buffs = new ConcurrentHashMap<>();
    private final Map<UUID, Long> enteredAt = new ConcurrentHashMap<>();
    private final List<Entity> decorations = new CopyOnWriteArrayList<>();

    MineshaftInstance(UUID owner, ShaftLayout layout, MineshaftWorld world, @Nullable Instance origin) {
        this.owner = owner;
        this.layout = layout;
        this.world = world;
        this.origin = origin;
    }

    public UUID owner() {
        return owner;
    }

    public ShaftLayout layout() {
        return layout;
    }

    public MineshaftWorld world() {
        return world;
    }

    public Instance instance() {
        return world.instance();
    }

    public @Nullable Instance origin() {
        return origin;
    }

    public long createdAt() {
        return createdAt;
    }

    public long elapsedMillis(long now) {
        return now - createdAt;
    }

    public boolean isDead(UUID player) {
        return dead.contains(player);
    }

    void markDead(UUID player) {
        dead.add(player);
    }

    void markDeparted(UUID player) {
        departed.add(player);
    }

    void markEntered(UUID player, long now) {
        enteredAt.put(player, now);
        departed.remove(player);
    }

    boolean hasEntered(UUID player) {
        return enteredAt.containsKey(player);
    }

    long lastEnteredAt() {
        return enteredAt.values().stream().mapToLong(Long::longValue).max().orElse(createdAt);
    }

    public int scrapsFound(UUID player) {
        return scraps.getOrDefault(player, 0);
    }

    int addScrap(UUID player) {
        return scraps.merge(player, 1, Integer::sum);
    }

    public int corpsesLooted(UUID player) {
        return corpses.getOrDefault(player, 0);
    }

    int addCorpse(UUID player) {
        return corpses.merge(player, 1, Integer::sum);
    }

    int announcedSteps(UUID player) {
        return announcedSteps.getOrDefault(player, 0);
    }

    void setAnnouncedSteps(UUID player, int steps) {
        announcedSteps.put(player, steps);
    }

    public @Nullable MayhemBuff buff(UUID player) {
        return buffs.get(player);
    }

    void setBuff(UUID player, MayhemBuff buff) {
        buffs.put(player, buff);
    }

    void addDecoration(Entity entity) {
        decorations.add(entity);
    }

    void removeDecorations() {
        decorations.forEach(Entity::remove);
        decorations.clear();
    }
}
