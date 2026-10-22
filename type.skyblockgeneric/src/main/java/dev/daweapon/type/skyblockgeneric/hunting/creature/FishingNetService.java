package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wiki Fishing Net: Galatea and Lotus Atoll only. A throw hits every swimming creature in front of the player within
 * the net's reach; Verdant and Birries are caught at once, others take Pull damage and are caught when it runs out.
 * Damage is one point per Pull (video: 125 Pull did 125 damage to a 1,000 HP creature); the 40 degree aim cone is an estimate.
 */
public final class FishingNetService {
    public static final long COOLDOWN_MS = 500;
    private static final double PULL_DAMAGE_PER_POINT = 1;
    private static final double CONE_COSINE = Math.cos(Math.toRadians(40));
    private static final Set<String> INSTANT = Set.of("VERDANT", "BIRRIES");
    private static final Set<ServerType> ALLOWED = Set.of(ServerType.SKYBLOCK_GALATEA, ServerType.SKYBLOCK_TORRHUS_CANYON);
    private static final Map<UUID, Long> LAST_USE = new ConcurrentHashMap<>();

    public static final Map<ItemType, CreatureSpec.NetTier> NETS = Map.of(
            ItemType.BASIC_FISHING_NET, CreatureSpec.NetTier.BASIC, ItemType.MEDIUM_FISHING_NET, CreatureSpec.NetTier.MEDIUM,
            ItemType.TURBO_FISHING_NET, CreatureSpec.NetTier.TURBO, ItemType.GIGANTIC_FISHING_NET, CreatureSpec.NetTier.GIGANTIC);

    private FishingNetService() {
    }

    public static void use(SkyBlockPlayer player, CreatureSpec.NetTier tier) {
        ServerType here = HypixelConst.getTypeLoader().getType();
        if (!ALLOWED.contains(here)) {
            player.sendMessage("<c>You need to be on Galatea or Lotus Atoll to use this item!");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - LAST_USE.getOrDefault(player.getUuid(), 0L) < COOLDOWN_MS) return;
        LAST_USE.put(player.getUuid(), now);
        List<HuntingCreatureMob> hit = targets(player, tier.range());
        if (hit.isEmpty()) {
            player.sendMessage("<7>Your net caught nothing but water.");
            return;
        }
        for (HuntingCreatureMob mob : hit) handle(player, mob, tier);
    }

    private static List<HuntingCreatureMob> targets(SkyBlockPlayer player, double range) {
        if (player.getInstance() == null) return List.of();
        Vec look = player.getPosition().direction();
        return player.getInstance().getNearbyEntities(player.getPosition(), range).stream()
                .filter(entity -> entity instanceof HuntingCreatureMob mob && !mob.isDead()
                        && mob.spec().captures(CreatureSpec.Capture.FISHING_NET))
                .map(entity -> (HuntingCreatureMob) entity)
                .filter(mob -> {
                    Vec toMob = Vec.fromPoint(mob.getPosition().add(0, 0.3, 0).sub(player.getPosition().add(0, player.getEyeHeight(), 0)));
                    return toMob.lengthSquared() > 0 && toMob.normalize().dot(look) >= CONE_COSINE;
                })
                .sorted(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(player.getPosition())))
                .limit(4)
                .toList();
    }

    private static void handle(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier) {
        CreatureSpec spec = mob.spec();
        if (tier.ordinal() < spec.minNet().ordinal()) {
            player.sendMessage("<c>Your net is too weak to catch a <f>{}<c>! <7>You need a {} Fishing Net or better.", spec.name(),
                    spec.minNet().name().charAt(0) + spec.minNet().name().substring(1).toLowerCase());
            return;
        }
        double distance = mob.getPosition().distance(player.getPosition());
        if (mob.special().map(special -> special.onNetHit(player, mob, tier, distance)).orElse(false)) return;
        if (INSTANT.contains(spec.id())) {
            mob.capture(player);
            return;
        }
        double pull = Math.max(tier.pull(), player.getStatistics().allStatistics().getOverall(ItemStatistic.PULL));
        double damage = spec.netDamage() != null ? spec.netDamage() : pull * PULL_DAMAGE_PER_POINT;
        damageOrCapture(player, mob, damage);
    }

    public static void damageOrCapture(SkyBlockPlayer player, HuntingCreatureMob mob, double damage) {
        if (damage >= mob.getHealth()) {
            mob.capture(player);
            return;
        }
        mob.applyToolDamage(player, damage);
    }

    public static @Nullable CreatureSpec.NetTier tierOf(ItemType type) {
        return NETS.get(type);
    }
}
