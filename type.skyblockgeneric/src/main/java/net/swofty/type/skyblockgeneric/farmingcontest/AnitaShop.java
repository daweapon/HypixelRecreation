package net.swofty.type.skyblockgeneric.farmingcontest;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Anita's full stock in her menu's order, with prices from the wiki's Anita shop UI. Stock this
 * server can't give out yet keeps its real price and says why it's unavailable.
 */
public final class AnitaShop {
    private static final String FARMING_TOOLS = "Anita's farming tools aren't in this server yet.";
    private static final String FARMING_ENCHANTS = "Farming enchantments aren't in this server yet.";
    private static final String GARDEN_CHIPS = "Garden Chips need the Garden, which isn't in this server yet.";

    public enum Kind { ITEM, LEVEL_CAP, EXTRA_FORTUNE, PERSONAL_BESTS, UNAVAILABLE }

    public record Offer(String name, String color, Material icon, Kind kind, @Nullable ItemType item, int amount,
                        MedalCost cost, @Nullable ItemType tradesIn, @Nullable String unavailableReason) {

        static Offer unavailable(String name, String color, Material icon, int amount, MedalCost cost, String reason) {
            return new Offer(name, color, icon, Kind.UNAVAILABLE, null, amount, cost, null, reason);
        }

        static Offer item(ItemType item, MedalCost cost, @Nullable ItemType tradesIn) {
            return new Offer(item.getDisplayName(), "", Material.PLAYER_HEAD, Kind.ITEM, item, 1, cost, tradesIn, null);
        }

        static Offer upgrade(String name, Material icon, Kind kind, MedalCost firstCost) {
            return new Offer(name, "<e>", icon, kind, null, 1, firstCost, null, null);
        }
    }

    public static final List<MedalCost> EXTRA_FORTUNE_TIERS = List.of(
            MedalCost.gold(1, 0), MedalCost.gold(1, 50), MedalCost.gold(1, 50), MedalCost.gold(2, 100),
            MedalCost.gold(2, 100), MedalCost.gold(3, 150), MedalCost.gold(3, 150), MedalCost.gold(4, 200),
            MedalCost.gold(4, 200), MedalCost.gold(5, 250), MedalCost.gold(6, 300), MedalCost.gold(7, 350),
            MedalCost.gold(8, 400), MedalCost.gold(9, 450), MedalCost.gold(10, 1_000));

    public static final List<MedalCost> LEVEL_CAP_TIERS = List.of(
            MedalCost.gold(1, 0), MedalCost.gold(1, 50), MedalCost.gold(1, 50), MedalCost.gold(1, 100),
            MedalCost.gold(1, 100), MedalCost.gold(2, 150), MedalCost.gold(2, 150), MedalCost.gold(2, 200),
            MedalCost.gold(2, 250), MedalCost.gold(3, 500));

    public static final MedalCost PERSONAL_BESTS_COST = MedalCost.gold(2, 64);
    public static final int UPGRADE_TIER_XP = 10;

    public static final List<Offer> OFFERS = List.of(
            Offer.unavailable("InfiniDirt™ Wand", "<a>", Material.STICK, 1, new MedalCost(0, 0, 0, 1), FARMING_TOOLS),
            Offer.unavailable("Prismapump", "<9>", Material.PRISMARINE, 4, MedalCost.bronze(1, 2), FARMING_TOOLS),
            Offer.unavailable("Hoe of Great Tilling", "<a>", Material.STONE_HOE, 1, MedalCost.bronze(1, 5), FARMING_TOOLS),
            Offer.unavailable("Hoe of Greater Tilling", "<9>", Material.IRON_HOE, 1, MedalCost.silver(1, 10), FARMING_TOOLS),
            Offer.unavailable("Hoe of Greatest Tilling", "<5>", Material.DIAMOND_HOE, 1, MedalCost.silver(2, 20), FARMING_TOOLS),
            Offer.unavailable("Basket of Seeds", "<5>", Material.WHEAT_SEEDS, 1, MedalCost.silver(2, 30), FARMING_TOOLS),
            Offer.unavailable("Nether Wart Basket", "<5>", Material.NETHER_WART, 1, MedalCost.silver(2, 30), FARMING_TOOLS),
            Offer.unavailable("Enchanted Book (Delicate V)", "<a>", Material.ENCHANTED_BOOK, 1, MedalCost.silver(2, 32), FARMING_ENCHANTS),
            Offer.upgrade("+1 Farming Level Cap", Material.HAY_BLOCK, Kind.LEVEL_CAP, LEVEL_CAP_TIERS.getFirst()),
            Offer.upgrade("Extra Farming Fortune", Material.WHEAT, Kind.EXTRA_FORTUNE, EXTRA_FORTUNE_TIERS.getFirst()),
            Offer.upgrade("Personal Bests", Material.PAPER, Kind.PERSONAL_BESTS, PERSONAL_BESTS_COST),
            Offer.item(ItemType.ANITA_TALISMAN, MedalCost.bronze(2, 32), null),
            Offer.item(ItemType.ANITA_RING, MedalCost.silver(2, 64), ItemType.ANITA_TALISMAN),
            Offer.item(ItemType.ANITA_ARTIFACT, MedalCost.gold(2, 100), ItemType.ANITA_RING),
            Offer.unavailable("Mechamind Chip", "<9>", Material.REPEATER, 1, MedalCost.gold(1, 0), GARDEN_CHIPS),
            Offer.unavailable("Overdrive Chip", "<9>", Material.COMPARATOR, 1, MedalCost.gold(2, 0), GARDEN_CHIPS));

    private AnitaShop() {}

    public static @Nullable MedalCost nextTierCost(List<MedalCost> tiers, int purchased) {
        return purchased >= tiers.size() ? null : tiers.get(purchased);
    }

    public static int goldCropsRequiredForLevelCap(int purchased) {
        return purchased + 1;
    }
}
