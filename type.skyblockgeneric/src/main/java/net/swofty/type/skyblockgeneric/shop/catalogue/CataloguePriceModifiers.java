package net.swofty.type.skyblockgeneric.shop.catalogue;

import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

/**
 * Per-player coin multipliers a shop YAML can name with {@code priceModifier: <id>}, e.g. the
 * faction Emissaries' reputation discount. Other systems register their own with {@link #register}.
 */
public final class CataloguePriceModifiers {
    private static final Map<String, ToDoubleFunction<SkyBlockPlayer>> MODIFIERS = new ConcurrentHashMap<>();

    static {
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            register(faction.name().toLowerCase() + "_emissary",
                    player -> 1 - CrimsonReputationService.emissaryDiscount(player, faction));
        }
    }

    private CataloguePriceModifiers() {
    }

    public static void register(String id, ToDoubleFunction<SkyBlockPlayer> coinMultiplier) {
        MODIFIERS.put(id.toLowerCase(), coinMultiplier);
    }

    public static double coinMultiplier(String id, SkyBlockPlayer player) {
        if (id == null) return 1;
        ToDoubleFunction<SkyBlockPlayer> modifier = MODIFIERS.get(id.toLowerCase());
        return modifier == null ? 1 : modifier.applyAsDouble(player);
    }
}
