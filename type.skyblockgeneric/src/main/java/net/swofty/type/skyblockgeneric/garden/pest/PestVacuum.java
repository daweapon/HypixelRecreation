package net.swofty.type.skyblockgeneric.garden.pest;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PestVacuum {
    public static final long HOLD_WINDOW_MILLIS = 450;
    public static final long TRACKER_COOLDOWN_MILLIS = 1000;
    private static final double TICK_SECONDS = 0.1;
    private static final double AIM_COSINE = 0.8;
    private static final double PULL_SPEED = 6;

    public record Tier(String id, double damage, double range) {
    }

    private static final Map<String, Tier> TIERS = Map.of(
            "SKYMART_VACUUM", new Tier("SKYMART_VACUUM", 100, 5),
            "SKYMART_TURBO_VACUUM", new Tier("SKYMART_TURBO_VACUUM", 150, 7.5),
            "SKYMART_HYPER_VACUUM", new Tier("SKYMART_HYPER_VACUUM", 200, 10),
            "INFINI_VACUUM", new Tier("INFINI_VACUUM", 300, 12.5),
            "INFINI_VACUUM_HOOVERIUS", new Tier("INFINI_VACUUM_HOOVERIUS", 400, 15));
    private static final Map<UUID, Held> HOLDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> TRACKER = new ConcurrentHashMap<>();

    private record Held(Tier tier, long until) {
    }

    private PestVacuum() {
    }

    public static @Nullable Tier tier(SkyBlockItem item) {
        return TIERS.get(item.getAttributeHandler().getTypeAsString());
    }

    public static boolean inGarden(SkyBlockPlayer player) {
        return player.getInstance() != null && player.getInstance() == SkyBlockGarden.of(player).getGardenInstance();
    }

    public static void hold(SkyBlockPlayer player, SkyBlockItem item) {
        Tier tier = tier(item);
        if (tier == null || !inGarden(player)) return;
        if (!PestService.unlocked(Gardens.profile(player))) {
            player.sendMessage(Text.of("<c>You need <a>Garden Level 5 <c>to use this!"));
            return;
        }
        HOLDING.put(player.getUuid(), new Held(tier, System.currentTimeMillis() + HOLD_WINDOW_MILLIS));
    }

    public static void tick(Instance instance) {
        long now = System.currentTimeMillis();
        for (Player online : instance.getPlayers()) {
            if (!(online instanceof SkyBlockPlayer player)) continue;
            Held held = HOLDING.get(player.getUuid());
            if (held == null) continue;
            if (held.until() < now) {
                HOLDING.remove(player.getUuid());
                continue;
            }
            vacuum(player, instance, held.tier());
        }
    }

    private static void vacuum(SkyBlockPlayer player, Instance instance, Tier tier) {
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec look = player.getPosition().direction();
        PestEntity target = null;
        double best = Double.MAX_VALUE;
        for (PestEntity pest : PestWorld.entities(instance)) {
            if (pest.getInstance() != instance) continue;
            Vec offset = pest.getPosition().add(0, 0.3, 0).sub(eye).asVec();
            double distance = offset.length();
            if (distance > tier.range() || distance < 0.01) continue;
            if (offset.normalize().dot(look) < AIM_COSINE) continue;
            if (distance < best) {
                best = distance;
                target = pest;
            }
        }
        if (target == null) return;
        beam(player, eye, target.getPosition().add(0, 0.3, 0));
        target.pullToward(eye, PULL_SPEED);
        if (target.hurt(tier.damage() * TICK_SECONDS)) PestService.kill(player, target);
    }

    private static void beam(SkyBlockPlayer player, Pos from, Pos to) {
        Vec step = to.sub(from).asVec();
        int points = Math.max(2, (int) (step.length() * 1.5));
        for (int i = 1; i < points; i++) {
            Vec at = from.asVec().add(step.mul((double) i / points));
            player.sendPacket(new ParticlePacket(Particle.CLOUD, false, false, (float) at.x(), (float) at.y(), (float) at.z(),
                    0, 0, 0, 0, 1));
        }
    }

    public static void track(SkyBlockPlayer player, SkyBlockItem item) {
        if (tier(item) == null || !inGarden(player)) return;
        long now = System.currentTimeMillis();
        Long last = TRACKER.get(player.getUuid());
        if (last != null && now - last < TRACKER_COOLDOWN_MILLIS) return;
        TRACKER.put(player.getUuid(), now);
        Instance instance = player.getInstance();
        PestEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (PestEntity pest : PestWorld.entities(instance)) {
            if (pest.getInstance() != instance) continue;
            double distance = pest.getPosition().distance(player.getPosition());
            if (distance < best) {
                best = distance;
                nearest = pest;
            }
        }
        if (nearest == null) {
            player.sendMessage(Text.of("<c>There are no <2>Pests <c>nearby!"));
            return;
        }
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = nearest.getPosition().add(0, 1, 0).sub(eye).asVec().normalize();
        for (int i = 2; i <= 24; i += 2) {
            Vec at = eye.asVec().add(direction.mul(i * 0.7));
            player.sendPacket(new ParticlePacket(Particle.HAPPY_VILLAGER, false, false, (float) at.x(), (float) at.y(),
                    (float) at.z(), 0, 0, 0, 0, 1));
        }
        player.sendActionBar(Text.of("<a>Nearest <2>Pest<a>: <f>{} <7>(<a>{} blocks<7>)", nearest.pest().name(),
                (int) Math.round(best)).asComponent());
    }

    public static int bag(GardenProfile profile) {
        return profile.pests().getStored();
    }
}
