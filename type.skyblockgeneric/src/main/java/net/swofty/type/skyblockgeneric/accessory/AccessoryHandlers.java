package net.swofty.type.skyblockgeneric.accessory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Ids an accessory's {@code HANDLER} effect may name. Each id is a behaviour implemented in Java
 * that asks {@link AccessoryOwnership} whether the player owns the accessory. Add the id here in
 * the same change as the code that implements it.
 */
public final class AccessoryHandlers {
    private static final Map<String, String> HANDLERS = new TreeMap<>(Map.ofEntries(
            Map.entry("AXE_WOODLAND_DAMAGE", "item/events/AccessoryRunebladeDamageEvent"),
            Map.entry("BAIT_PRESERVATION", "accessory/AccessoryEconomyHooks"),
            Map.entry("CHOCOLATE_PER_SECOND", "accessory/AccessoryEconomyHooks"),
            Map.entry("COINS_PER_MINUTE", "accessory/AccessoryEconomyHooks"),
            Map.entry("COINS_PER_MOB_LEVEL", "accessory/AccessoryEconomyHooks"),
            Map.entry("DOUBLE_BITS_CHANCE", "accessory/AccessoryEconomyHooks"),
            Map.entry("FALL_DAMAGE_REDUCTION", "item/events/AccessoryFallDamageEvent"),
            Map.entry("GRAVITY_TALISMAN", "accessory/AccessoryHandlerStats"),
            Map.entry("HASTE", "accessory/AccessoryHandlerStats"),
            Map.entry("MAGIC_8_BALL", "accessory/AccessoryHandlerStats"),
            Map.entry("PIGGY_BANK", "accessory/PiggyBank"),
            Map.entry("POTATO_MINION_SPEED", "accessory/AccessoryEconomyHooks"),
            Map.entry("POTION_DURATION", "accessory/AccessoryEconomyHooks"),
            Map.entry("SAFE_FALL_BLOCKS", "item/events/AccessoryFallDamageEvent"),
            Map.entry("SEA_CREATURE_DAMAGE_REDUCTION", "item/events/AccessorySeaCreatureDamageEvent"),
            Map.entry("THUNDER_CHARGE", "accessory/AccessoryEconomyHooks"),
            Map.entry("TOTAL_INTELLIGENCE", "accessory/AccessoryHandlerStats"),
            Map.entry("WHILE_BURNING", "accessory/AccessoryHandlerStats"),
            Map.entry("ANITA_CONTEST_FORTUNE", "farmingcontest/FarmingContestStatistics"),
            Map.entry("BURSTSTOPPER", "slayer/inferno/BlazeSlayerGear"),
            Map.entry("CANDY_DROP_CHANCE", "event/actions/custom/spooky/SpookyFestivalCandyHandler"),
            Map.entry("GIFT_BOOST", "gifts/GiftBoosts"),
            Map.entry("HATCESSORY_PARTY_TIME", "accessory/hatcessory/Hatcessories"),
            Map.entry("INTIMIDATION", "entity/mob/ai/Intimidation"),
            Map.entry("PERSONAL_COMPACTOR", "compactor/PersonalCompactorService"),
            Map.entry("PERSONAL_DELETOR", "deletor/PersonalDeletorService"),
            Map.entry("SHARK_TOOTH_CHANCE", "fishing/festival/SharkToothNecklace"),
            Map.entry("SHOP_DISCOUNT", "shop/ShopDiscount"),
            Map.entry("SUNSHADE", "entity/mob/mobs/crimsonisle/MobDiveGhast"),
            Map.entry("VOTE_WEIGHT", "elections/ElectionManager")
    ));

    private AccessoryHandlers() {
    }

    public static boolean isKnown(String id) {
        return HANDLERS.containsKey(id);
    }

    public static Set<String> ids() {
        return Collections.unmodifiableSet(HANDLERS.keySet());
    }
}
