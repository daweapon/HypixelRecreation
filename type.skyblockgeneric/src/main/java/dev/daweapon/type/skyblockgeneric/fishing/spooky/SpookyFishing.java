package dev.daweapon.type.skyblockgeneric.fishing.spooky;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalPerk;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition.SeaCreatureRoll;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

import java.util.List;
import java.util.Optional;

/**
 * The Spooky pool (wiki: Spooky_Festival, Fishing): while the Spooky Festival runs, a water Sea Creature catch on any
 * island comes from this pool 15% of the time, plus Spooky Savant, Spooky Bait, the Spooky (Phantom) Hook and the
 * Mythic Bat Pet's Sonar, capped at 75%.
 */
public final class SpookyFishing {
    static final double BASE_CHANCE = 15;
    static final double HOOK_BONUS = 20;
    static final double CAP = 75;
    static final String SPOOKY_HOOK_ID = "PHANTOM_HOOK";

    public static final List<SeaCreatureRoll> POOL = List.of(
            new SeaCreatureRoll("JUMPIN_JACK", 0, 5_000),
            new SeaCreatureRoll("SCARECROW", 0, 2_875),
            new SeaCreatureRoll("NIGHTMARE", 0, 1_500),
            new SeaCreatureRoll("WEREWOLF", 0, 500),
            new SeaCreatureRoll("PHANTOM_FISHER", 0, 100),
            new SeaCreatureRoll("GRIM_REAPER", 0, 25));

    private SpookyFishing() {
    }

    public static boolean isActive() {
        return SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SPOOKY_FESTIVAL);
    }

    public static Optional<List<SeaCreatureRoll>> override(FishingContext context) {
        if (context.medium() != FishingMedium.WATER || !isActive()) return Optional.empty();
        if (!LootRoll.roll(context.player().getUuid(), null, chance(context))) return Optional.empty();
        return Optional.of(POOL);
    }

    public static double chance(FishingContext context) {
        double chance = BASE_CHANCE + Carnivals.perkValue(context.player(), CarnivalPerk.SPOOKY_SAVANT);
        if (context.bait() != null) chance += context.bait().getTagBonuses().getOrDefault("SPOOKY", 0D);
        if (context.hook() != null && SPOOKY_HOOK_ID.equals(context.hook().getItemId())) chance += HOOK_BONUS;
        chance += PetAbilities.value(context.player(), ItemType.BAT_PET, "Sonar", 0);
        return Math.min(CAP, chance);
    }
}
