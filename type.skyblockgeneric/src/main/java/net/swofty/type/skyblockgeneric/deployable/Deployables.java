package net.swofty.type.skyblockgeneric.deployable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Deployables {
    private static final Map<UUID, Placed> PLACED = new ConcurrentHashMap<>();
    private static Task task;

    private static final class Placed {
        final UUID owner;
        final DeployableType type;
        final Instance instance;
        final Pos position;
        final long placedAt = System.currentTimeMillis();
        final long expiresAt;
        final LivingEntity head;
        final LivingEntity nameLine;
        final LivingEntity timerLine;

        Placed(UUID owner, DeployableType type, Instance instance, Pos position, long durationMs, String texture) {
            this.owner = owner;
            this.type = type;
            this.instance = instance;
            this.position = position;
            this.expiresAt = placedAt + durationMs;
            this.head = stand(instance, position, null, texture);
            this.nameLine = stand(instance, position.add(0, 1.0, 0), type.displayName(), null);
            this.timerLine = stand(instance, position.add(0, 0.75, 0), "<e><b>" + seconds(durationMs) + "s", null);
        }

        void remove() {
            head.remove();
            nameLine.remove();
            timerLine.remove();
        }

        boolean expired(long now) {
            return now >= expiresAt;
        }

        void refreshTimer(long now) {
            timerLine.setCustomName(Text.of("<e><b>" + seconds(expiresAt - now) + "s").asComponent());
        }
    }

    private Deployables() {
    }

    public static synchronized void deploy(SkyBlockPlayer owner, DeployableType type, String texture, boolean doubledDuration) {
        removeOwned(owner.getUuid());
        long durationMs = (long) (DeployableMath.durationSeconds(type.durationSeconds(), doubledDuration) * 1000);
        Placed placed = new Placed(owner.getUuid(), type, owner.getInstance(), owner.getPosition(), durationMs, texture);
        PLACED.put(owner.getUuid(), placed);
        owner.sendMessage("<a>You placed a " + type.displayName() + "<a>! It lasts <e>" + seconds(durationMs) + "s<a>.");
        ensureTicking();
        pulse(placed, System.currentTimeMillis());
    }

    public static void removeOwned(UUID owner) {
        Placed previous = PLACED.remove(owner);
        if (previous != null) previous.remove();
        DeployableBuffs.forget(owner);
    }

    public static void clear() {
        PLACED.values().forEach(Placed::remove);
        PLACED.clear();
    }

    public static boolean hasDeployed(UUID owner) {
        return PLACED.containsKey(owner);
    }

    private static void ensureTicking() {
        if (task != null && task.isAlive()) return;
        task = MinecraftServer.getSchedulerManager().buildTask(Deployables::tick)
                .repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        for (Placed placed : new ArrayList<>(PLACED.values())) {
            if (placed.expired(now) || !placed.instance.isRegistered() || placed.head.isRemoved()
                    || ownerOf(placed) == null) {
                removeOwned(placed.owner);
            }
        }
        if (PLACED.isEmpty()) {
            task.cancel();
            return;
        }
        Map<UUID, List<Offer>> offers = new HashMap<>();
        for (Placed placed : PLACED.values()) {
            placed.refreshTimer(now);
            particles(placed);
            collect(placed, offers);
        }
        offers.forEach((id, list) -> applyStrongest(id, list));
    }

    private static void pulse(Placed placed, long now) {
        Map<UUID, List<Offer>> offers = new HashMap<>();
        collect(placed, offers);
        offers.forEach((id, list) -> applyStrongest(id, list));
    }

    private record Offer(Placed placed, SkyBlockPlayer player, double distance) {
    }

    private static void collect(Placed placed, Map<UUID, List<Offer>> offers) {
        List<DeployableMath.Candidate> candidates = new ArrayList<>();
        Map<UUID, SkyBlockPlayer> byId = new HashMap<>();
        for (Player viewer : placed.instance.getPlayers()) {
            if (!(viewer instanceof SkyBlockPlayer player)) continue;
            byId.put(player.getUuid(), player);
            candidates.add(new DeployableMath.Candidate(player.getUuid(), player.getPosition().distance(placed.position)));
        }
        for (Object id : DeployableMath.recipients(candidates, placed.type.radius(), placed.type.maxPlayers())) {
            SkyBlockPlayer player = byId.get((UUID) id);
            offers.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>())
                    .add(new Offer(placed, player, player.getPosition().distance(placed.position)));
        }
    }

    private static void applyStrongest(UUID id, List<Offer> offers) {
        List<DeployableMath.Buff> buffs = offers.stream()
                .map(offer -> new DeployableMath.Buff(offer.placed.type.rank(), offer.placed.placedAt, offer.distance))
                .toList();
        Offer winner = offers.get(DeployableMath.strongest(buffs));
        SkyBlockPlayer player = winner.player;
        DeployableType type = winner.placed.type;
        var weather = player.getInstance().getWeather();
        DeployableBuffs.apply(player, type, weather.rainLevel() > 0 || weather.thunderLevel() > 0);
        double heal = player.getUuid().equals(winner.placed.owner) ? type.selfHealPerSecond() : type.otherHealPerSecond();
        if (heal > 0 && player.getHealth() < player.getMaxHealth()) {
            player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + heal));
        }
    }

    private static SkyBlockPlayer ownerOf(Placed placed) {
        for (Player viewer : placed.instance.getPlayers()) {
            if (viewer instanceof SkyBlockPlayer player && player.getUuid().equals(placed.owner)) return player;
        }
        return null;
    }

    private static void particles(Placed placed) {
        ParticlePacket packet = new ParticlePacket(Particle.HAPPY_VILLAGER, true, true,
                placed.position.x(), placed.position.y() + 1, placed.position.z(), 0.3f, 0.3f, 0.3f, 0.02f, 6);
        placed.instance.getPlayers().forEach(viewer -> viewer.sendPacket(packet));
    }

    private static long seconds(long millis) {
        return (millis + 999) / 1000;
    }

    private static LivingEntity stand(Instance instance, Pos position, String name, String texture) {
        LivingEntity stand = new LivingEntity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta) stand.getEntityMeta();
        meta.setInvisible(true);
        meta.setHasNoGravity(true);
        meta.setSmall(true);
        if (name != null) {
            stand.setCustomName(Text.of(name).asComponent());
            stand.setCustomNameVisible(true);
        }
        if (texture != null) stand.setEquipment(EquipmentSlot.HELMET, ItemStacks.head(texture, "").build());
        stand.setInstance(instance, position);
        return stand;
    }

}
