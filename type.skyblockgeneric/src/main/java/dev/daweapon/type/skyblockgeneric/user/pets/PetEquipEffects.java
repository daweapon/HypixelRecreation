package dev.daweapon.type.skyblockgeneric.user.pets;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.consumable.MushroomSoupFlight;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standing effects of the summoned pet that live on the player rather than in a stat: High Stride's permanent Jump
 * Boost and Magic Bird's flight on the private island. {@link #apply} computes the whole state from the active pets,
 * so it can run on every equip, unequip and tick.
 */
public final class PetEquipEffects {
    private static final Set<UUID> FLIGHT_GRANTED = ConcurrentHashMap.newKeySet();

    private PetEquipEffects() {
    }

    public static void apply(SkyBlockPlayer player) {
        if (player == null || player.getSkyblockDataHandler() == null) return;
        applyJumpBoost(player);
        applyFlight(player);
    }

    public static void release(UUID player) {
        FLIGHT_GRANTED.remove(player);
    }

    public static int jumpBoostLevel(SkyBlockPlayer player) {
        double horse = PetAbilities.value(player, ItemType.HORSE_PET, "High Stride", 0);
        double skeletonHorse = PetAbilities.value(player, ItemType.SKELETON_HORSE_PET, "High Stride", 0);
        return (int) Math.floor(Math.max(horse, skeletonHorse));
    }

    public static boolean canFly(SkyBlockPlayer player) {
        if (PetAbilities.has(player, ItemType.PHOENIX_PET, "Magic Bird") && PetConditions.onPrivateIsland()) return true;
        if (!HypixelConst.isPrivateWorldServer()) return false;
        return player.getBoosterCookieExpirationDate() > System.currentTimeMillis() || MushroomSoupFlight.grantsFlight(player);
    }

    private static void applyJumpBoost(SkyBlockPlayer player) {
        AttributeInstance jump = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jump.getBaseValue() <= 0) return;
        double target = PetPerkMath.jumpStrength(jumpBoostLevel(player));
        if (Math.abs(jump.getBaseValue() - target) > 1e-9) jump.setBaseValue(target);
    }

    private static void applyFlight(SkyBlockPlayer player) {
        boolean managed = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
        if (managed) return;
        boolean granted = FLIGHT_GRANTED.contains(player.getUuid());
        if (canFly(player)) {
            if (granted) return;
            FLIGHT_GRANTED.add(player.getUuid());
            player.setAllowFlying(true);
        } else if (granted) {
            FLIGHT_GRANTED.remove(player.getUuid());
            player.setFlying(false);
            player.setAllowFlying(false);
        }
    }
}
