package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.region.RegionType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The "NEW AREA DISCOVERED!" tips Hypixel shows for each Crimson Isle zone (wiki Module:Zone/Data ZoneText). */
public final class CrimsonZoneTips {
    private static final Map<RegionType, List<String>> TIPS = new EnumMap<>(RegionType.class);

    static {
        tips(RegionType.CRIMSON_ISLE, "Battle against brutal mobs!",
                "Collect exotic resources like Spores, Thornleaf, Nether Wart, and more!",
                "Visit the <e>Dojo<f>.", "Defeat <c>Kuudra<f>.");
        tips(RegionType.STRONGHOLD, "Collect Nether Wart.", "Talk to Elle.", "Defeat Bladesoul.");
        tips(RegionType.CRIMSON_FIELDS, "Slay Magma Cubes.", "Collect Netherrack.");
        tips(RegionType.BLAZING_VOLCANO, "Fish for rare loot and find epic secrets.", "Watch out for the lava.");
        tips(RegionType.ODGERS_HUT, "Try to communicate with Odger.");
        tips(RegionType.MAGMA_CHAMBER, "Watch out for fireballs!", "Fight the Magma Cube Boss.", "Beware of lava.");
        tips(RegionType.AURAS_LAB, "Speak to Aura.");
        tips(RegionType.PLHLEGBLAST_POOL, "Lava fish.");
        tips(RegionType.BARBARIAN_OUTPOST, "Kill Barbarians to neutralize your Reputation with the Mage Faction.");
        tips(RegionType.BURNING_DESERT, "Visit <2>Dragontail<f>.", "Collect Red Sand.", "Survive the wildlife.");
        tips(RegionType.DRAGONTAIL, "Meet the Piglins, members of the Barbarian Faction.",
                "Talk to <4><l>Chief Scorn<f>.",
                "Earn <a>Reputation <f>through the daily <a>Town Board <f>missions.");
        tips(RegionType.DRAGONTAIL_TOWNSQUARE, "Spend your coins wisely.");
        tips(RegionType.CHIEFS_HUT, "Meet <4><l>Chief Scorn<f>.");
        tips(RegionType.DRAGONTAIL_BLACKSMITH);
        tips(RegionType.DRAGONTAIL_AUCTION_HOUSE, "Access the <6>Auction House<f>.");
        tips(RegionType.DRAGONTAIL_BANK, "Deposit and withdraw your coins.");
        tips(RegionType.DRAGONTAIL_BAZAAR);
        tips(RegionType.MINION_SHOP);
        tips(RegionType.THE_BASTION, "Don't anger the guards.");
        tips(RegionType.THE_DUKEDOM, "Duel the <8><l>Barbarian Duke X<f>.");
        tips(RegionType.MATRIARCHS_LAIR, "Beware of the monster!");
        tips(RegionType.BELLY_OF_THE_BEAST, "Collect Heavy Pearls!", "Find a way to escape!", "Don't get digested!");
        tips(RegionType.RUINS_OF_ASHFANG, "Defeat <8><l>Ashfang<f>.");
        tips(RegionType.THE_WASTELAND, "Look up.");
        tips(RegionType.FORGOTTEN_SKULL, "An unknown skull structure.");
        tips(RegionType.SMOLDERING_TOMB, "<e>Slay really mean blazes.");
        tips(RegionType.MYSTIC_MARSH, "Visit <5>Scarleton<f>.", "Collect Mycelium.", "Don't let the bulls catch up.");
        tips(RegionType.MAGE_OUTPOST, "Kill Mages to neutralize your Reputation with the Barbarian Faction.");
        tips(RegionType.DOJO, "Master the trainings.", "Compare your skills with other players.", "Earn your <6>Black Belt<f>.");
        tips(RegionType.COURTYARD, "Fight the <5><l>Mage Outlaw<f>.");
        tips(RegionType.SCARLETON, "Meet the Taika, members of the Mage Faction.",
                "Talk to <d><l>Queen Nyx<f>.",
                "Earn <a>Reputation <f>through the daily <a>Town Board <f>missions.");
        tips(RegionType.SCARLETON_PLAZA, "Heart of the City of Magic.");
        tips(RegionType.SCARLETON_AUCTION_HOUSE, "Access the <6>Auction House<f>.");
        tips(RegionType.SCARLETON_BANK, "Deposit and withdraw your coins.");
        tips(RegionType.SCARLETON_BAZAAR);
        tips(RegionType.SCARLETON_MINION_SHOP, "Buy Tier XII minions!");
        tips(RegionType.SCARLETON_BLACKSMITH, "Talk to Alwin.", "Upgrade and salvage your gear.");
        tips(RegionType.SCARLETON_COMMUNITY_CENTER, "Fill the <a>Town Board <f>requests.");
        tips(RegionType.THRONE_ROOM, "Meet <d><l>Queen Nyx<f>.");
        tips(RegionType.MAGE_COUNCIL, "Where decisions are made.");
        tips(RegionType.IGRUPANS_HOUSE, "A surprisingly lavish home for a humble chicken farmer...");
        tips(RegionType.IGRUPANS_CHICKEN_COOP, "Home to 5 very naughty chickens.");
        tips(RegionType.CATHEDRAL, "Find the Dean's office.", "Pray.");
    }

    private CrimsonZoneTips() {
    }

    private static void tips(RegionType zone, String... lines) {
        TIPS.put(zone, List.of(lines));
    }

    /** The zone's tips, an empty array for a Crimson zone without any, or null for zones elsewhere. */
    public static @Nullable Text[] of(RegionType zone) {
        List<String> lines = TIPS.get(zone);
        return lines == null ? null : lines.stream().map(Text::of).toArray(Text[]::new);
    }
}
