package dev.daweapon.type.skyblockgeneric.accessory;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffectKind;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code HANDLER} effects a player's counted accessories carry, for Java behaviour that reads its
 * numbers ({@code percent}, {@code statistics}) from the accessory YAML.
 */
public final class AccessoryHandlerEffects {
    private AccessoryHandlerEffects() {
    }

    public record Active(OwnedAccessory accessory, AccessoryEffect effect) {
    }

    public static List<Active> of(SkyBlockPlayer player, String handler) {
        if (player.getSkyblockDataHandler() == null) return List.of();
        List<Active> found = new ArrayList<>();
        for (OwnedAccessory owned : AccessoryOwnership.active(player)) {
            if (!owned.definition().getRealm().isActive()) continue;
            for (AccessoryEffect effect : owned.definition().getEffects()) {
                if (isHandler(effect, handler) && effect.appliesTo(player)) found.add(new Active(owned, effect));
            }
        }
        return found;
    }

    public static boolean has(SkyBlockPlayer player, String handler) {
        return !of(player, handler).isEmpty();
    }

    public static double percent(SkyBlockPlayer player, String handler) {
        return of(player, handler).stream().mapToDouble(active -> active.effect().percent()).sum();
    }

    public static List<ItemType> types(SkyBlockPlayer player, String handler) {
        return of(player, handler).stream().map(active -> active.accessory().type()).distinct().toList();
    }

    static boolean isHandler(AccessoryEffect effect, String handler) {
        return effect.kind() == AccessoryEffectKind.HANDLER && handler.equals(effect.handler());
    }
}
