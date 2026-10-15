package dev.daweapon.type.skyblockgeneric.entity.mob.ai;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.minestom.server.entity.Entity;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

/**
 * The Intimidation accessory line: monsters at or below the held tier's level no longer target
 * the player. Sea creatures are unaffected, as on Hypixel.
 */
public final class Intimidation {
    private static final Map<ItemType, Integer> HIGHEST_DETERRED_LEVEL = Map.of(
            ItemType.INTIMIDATION_TALISMAN, 1,
            ItemType.INTIMIDATION_RING, 5,
            ItemType.INTIMIDATION_ARTIFACT, 25,
            ItemType.INTIMIDATION_RELIC, 30);

    private Intimidation() {
    }

    public static boolean deters(Entity attacker, Entity target) {
        if (!(attacker instanceof SkyBlockMob mob) || mob instanceof SeaCreatureMob) return false;
        if (!(target instanceof SkyBlockPlayer player)) return false;
        Integer level = mob.getLevel();
        return level != null && level <= highestDeterredLevel(player);
    }

    static int highestDeterredLevel(SkyBlockPlayer player) {
        int highest = 0;
        for (SkyBlockItem accessory : AccessoryOwnership.activeItems(player)) {
            Integer deterred = HIGHEST_DETERRED_LEVEL.get(accessory.getAttributeHandler().getPotentialType());
            if (deterred != null) highest = Math.max(highest, deterred);
        }
        return highest > 0 ? highest + MiscPetHooks.intimidationBonusLevels(player) : 0;
    }
}
