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
            Map.entry("ANITA_CONTEST_FORTUNE", "farmingcontest/FarmingContestStatistics"),
            Map.entry("BURSTSTOPPER", "slayer/inferno/BlazeSlayerGear"),
            Map.entry("CANDY_DROP_CHANCE", "event/actions/custom/spooky/SpookyFestivalCandyHandler"),
            Map.entry("DEVOUR_RING", "accessory/handler/KillAccessories"),
            Map.entry("EXPERIENCE_ARTIFACT", "accessory/handler/UtilityAccessories"),
            Map.entry("HOTSPOT_BUFF", "accessory/handler/UtilityAccessories"),
            Map.entry("KING_COMMISSION_XP", "accessory/handler/UtilityAccessories"),
            Map.entry("NIGHT_VISION_CHARM", "accessory/handler/NightVisionCharm"),
            Map.entry("POTATO_MINION_SPEED", "accessory/handler/UtilityAccessories"),
            Map.entry("REAPER_ORB", "accessory/handler/KillAccessories"),
            Map.entry("RING_OF_LOVE", "accessory/handler/RingOfLove"),
            Map.entry("TARANTULA_TENTH_HIT", "accessory/handler/TarantulaAccessories"),
            Map.entry("GIFT_BOOST", "gifts/GiftBoosts"),
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
