package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// A two-stage Necron's Blade Scroll ability: first use creates a warp point 10 blocks ahead that
// pulls nearby enemies toward it; using it again within 5 seconds detonates the warp, dealing
// 10,000 base ability damage to enemies within 6 blocks of it.
public class ShadowWarpAbility extends RegisteredAbility {
    private static final long DETONATE_WINDOW_MS = 5000;
    private static final Map<UUID, ActiveWarp> ACTIVE_WARPS = new ConcurrentHashMap<>();

    public ShadowWarpAbility() {
        super("SHADOW_WARP", "Shadow Warp",
                (_, _) -> "<7>Creates a spatial distortion <a>10 blocks <7>ahead of you, pulling nearby enemies in. Use again within <e>5 <7>seconds to detonate it for <c>10,000 <7>damage in a <a>6 block <7>radius.",
                AbilityActivation.RIGHT_CLICK, 200, new RegisteredAbility.AbilityManaCost(300),
                (player, _, _, _) -> execute(player));
    }

    private static boolean execute(SkyBlockPlayer player) {
        ActiveWarp active = ACTIVE_WARPS.get(player.getUuid());
        if (active != null && System.currentTimeMillis() - active.createdAtMs <= DETONATE_WINDOW_MS) {
            detonate(player, active.position);
            ACTIVE_WARPS.remove(player.getUuid());
            return true;
        }

        Point warpPosition = player.getPosition().add(player.getPosition().direction().mul(10));
        ACTIVE_WARPS.put(player.getUuid(), new ActiveWarp(warpPosition, System.currentTimeMillis()));

        for (Entity entity : player.getInstance().getNearbyEntities(warpPosition, 8)) {
            if (!(entity instanceof SkyBlockMob mob)) continue;
            mob.setVelocity(warpPosition.sub(mob.getPosition()).asVec().normalize().mul(6));
        }

        return true;
    }

    private static void detonate(SkyBlockPlayer player, Point warpPosition) {
        AbilityDamage.hitArea(player, warpPosition, 6, "Shadow Warp", 10000, 0.3);
    }

    private record ActiveWarp(Point position, long createdAtMs) {
    }
}
