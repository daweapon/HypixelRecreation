package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Where the Wisps' Extinguish pools live: every 8 seconds of combat on the Crimson Isle a pool appears under the
 * player, and bathing in it heals {0}% of max health at once and {1}% per second for 8 seconds.
 */
public final class ExtinguishPools {
    private static final ItemType[] WISPS = {ItemType.FROST_WISP_PET, ItemType.GLACIAL_WISP_PET, ItemType.SUBZERO_WISP_PET};
    private static final long POOL_LIFETIME_MS = 8_000;
    private static final long REGEN_MS = 8_000;
    private static final double REGEN_TICK_SECONDS = 0.5;
    private static final double POOL_HEIGHT = 2;

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private ExtinguishPools() {
    }

    private static final class State {
        private final List<Pool> pools = new ArrayList<>();
        private long lastSpawn;
        private long regenUntil;
        private double regenPercent;
    }

    private static final class Pool {
        private final Instance instance;
        private final Pos center;
        private final long expiresAt;
        private boolean bathed;

        private Pool(Instance instance, Pos center, long expiresAt) {
            this.instance = instance;
            this.center = center;
            this.expiresAt = expiresAt;
        }
    }

    public record Strength(double instantPercent, double perSecondPercent) {
        public static final Strength NONE = new Strength(0, 0);

        public double regenAmount(double maxHealth) {
            return maxHealth * perSecondPercent / 100D * REGEN_TICK_SECONDS;
        }

        public double instantAmount(double maxHealth) {
            return maxHealth * instantPercent / 100D;
        }
    }

    public static Strength strength(SkyBlockPlayer player) {
        double instant = 0;
        double perSecond = 0;
        for (ItemType wisp : WISPS) {
            double value = PetAbilities.value(player, wisp, "Extinguish", 0);
            if (value <= instant) continue;
            instant = value;
            perSecond = PetAbilities.value(player, wisp, "Extinguish", 1);
        }
        return new Strength(instant, perSecond);
    }

    public static boolean isBathing(Pos pool, Pos player) {
        double dx = pool.x() - player.x();
        double dz = pool.z() - player.z();
        return Math.sqrt(dx * dx + dz * dz) <= PetPerkMath.EXTINGUISH_RADIUS
                && player.y() >= pool.y() - 1 && player.y() <= pool.y() + POOL_HEIGHT;
    }

    public static void tick(SkyBlockPlayer player, long now) {
        Strength strength = strength(player);
        if (strength.instantPercent() <= 0 || player.getInstance() == null || !PetConditions.onCrimsonIsle()) {
            STATES.remove(player.getUuid());
            return;
        }
        State state = STATES.computeIfAbsent(player.getUuid(), _ -> new State());
        state.pools.removeIf(pool -> pool.expiresAt <= now || pool.instance != player.getInstance());
        if (CombatState.inCombat(player.getUuid(), now, PetPerkMath.EXTINGUISH_COMBAT_MS)
                && now - state.lastSpawn >= PetPerkMath.EXTINGUISH_INTERVAL_MS) {
            state.lastSpawn = now;
            state.pools.add(new Pool(player.getInstance(), player.getPosition(), now + POOL_LIFETIME_MS));
        }
        for (Pool pool : state.pools) {
            show(pool);
            if (!isBathing(pool.center, player.getPosition())) continue;
            if (!pool.bathed) {
                pool.bathed = true;
                heal(player, strength.instantAmount(player.getMaxHealth()));
                state.regenUntil = now + REGEN_MS;
                state.regenPercent = strength.perSecondPercent();
            }
        }
        if (now < state.regenUntil) heal(player, new Strength(0, state.regenPercent).regenAmount(player.getMaxHealth()));
    }

    public static void release(UUID player) {
        STATES.remove(player);
    }

    private static void heal(SkyBlockPlayer player, double amount) {
        if (amount > 0) player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + amount));
    }

    private static void show(Pool pool) {
        ParticlePacket packet = new ParticlePacket(Particle.SPLASH, true, true, pool.center.x(), pool.center.y() + 0.1,
                pool.center.z(), (float) PetPerkMath.EXTINGUISH_RADIUS / 2, 0.05f, (float) PetPerkMath.EXTINGUISH_RADIUS / 2,
                0.1f, 25);
        pool.instance.getPlayers().forEach(viewer -> viewer.sendPacket(packet));
    }
}
