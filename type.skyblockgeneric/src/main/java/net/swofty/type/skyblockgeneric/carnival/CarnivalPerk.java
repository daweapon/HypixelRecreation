package net.swofty.type.skyblockgeneric.carnival;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Every event perk Doug sells for Carnival Tokens, with the wiki's per-tier values and costs. A
 * perk with an {@code unavailableReason} can't be bought until its effect is wired; delete the
 * reason once the effect reads {@link CarnivalPerks#value}.
 */
@Getter
public enum CarnivalPerk {
    FISHING_PRODIGY(CarnivalPerkEvent.FISHING_FESTIVAL, "Fishing Prodigy", Material.WRITABLE_BOOK, Tiers.FIVE,
            new double[]{2, 4, 6, 8, 10},
            "<7>Gain <3>+%s☯ Fishing Wisdom <7>during the\n<b>Fishing Festival<7>.", null),
    SHARK_SONAR(CarnivalPerkEvent.FISHING_FESTIVAL, "Shark Sonar", Material.TROPICAL_FISH, Tiers.FIVE,
            new double[]{1, 2, 3, 4, 5},
            "<7>Increases your chance to catch\n<b>Sharks <7>by <a>+%s% <7>during the <b>Fishing\n<b>Festival<7>.", null),
    DENTURE_COLLECTOR(CarnivalPerkEvent.FISHING_FESTIVAL, "Denture Collector", Material.GHAST_TEAR, Tiers.THREE,
            new double[]{10, 20, 30},
            "<7>Increase your chance of dropping\n<6>Shark Teeth <7>by <a>+%s% <7>during the\n<b>Fishing Festival<7>.", null),
    ELASMOBRANCHII_AFFINITY(CarnivalPerkEvent.FISHING_FESTIVAL, "Elasmobranchii Affinity", Material.PRISMARINE_SHARD, Tiers.TWO,
            new double[]{50, 100},
            "<7>Grants a <a>+%s% <7>chance for <b>Sharks <7>to\n<7>drop <a>+1 <9>Shark Fin <7>during the <b>Fishing\n<b>Festival<7>.", null),

    REFINED_MIND(CarnivalPerkEvent.MINING_FIESTA, "Refined Mind", Material.LIGHT_BLUE_DYE, Tiers.FIVE,
            new double[]{2, 4, 6, 8, 10},
            "<7>Grants a <a>+%s% <7>higher chance to find\n<9>Refined Mineral <7>and <5>Glossy Gemstone\n<7>during the <6>Mining Fiesta<7>.", null),
    FORTUNATE_FESTIVITY(CarnivalPerkEvent.MINING_FIESTA, "Fortunate Festivity", Material.GOLDEN_PICKAXE, Tiers.FIVE,
            new double[]{5, 10, 15, 20, 25},
            "<7>Gain <6>+%s☘ Mining Fortune <7>during the\n<6>Mining Fiesta<7>.", null),
    POWDER_HOARDER(CarnivalPerkEvent.MINING_FIESTA, "Powder Hoarder", Material.LIME_DYE, Tiers.THREE,
            new double[]{2, 4, 6},
            "<7>Gain <a>+%s% <2>Mithril Powder<7>, <d>Gemstone\n<d>Powder<7>, and <b>Glacite Powder <7>during\n<7>the <6>Mining Fiesta<7>.", null),
    TITANIUM_TENACITY(CarnivalPerkEvent.MINING_FIESTA, "Titanium Tenacity", Material.IRON_NUGGET, Tiers.TWO,
            new double[]{3, 6},
            "<7>Increases your chance of finding\n<9>Titanium <7>by <a>+%s% <7>during the <6>Mining\n<6>Fiesta<7>.", null),

    STORIED_STINGER(CarnivalPerkEvent.MYTHOLOGICAL_RITUAL, "Storied Stinger", Material.GOLDEN_SWORD, Tiers.FIVE,
            new double[]{5, 10, 15, 20, 25},
            "<7>Increases damage dealt to <2>Mythological\n<7>mobs by <a>+%s%<7>.", null),
    DEADLY_GREED(CarnivalPerkEvent.MYTHOLOGICAL_RITUAL, "Deadly Greed", Material.GOLD_NUGGET, Tiers.FIVE,
            new double[]{2, 4, 6, 8, 10},
            "<7>Gain <a>+%s% <7>more <6>Coins <7>from <e>Griffin\n<e>Burrows<7>.", null),
    DIANAS_FAVOR(CarnivalPerkEvent.MYTHOLOGICAL_RITUAL, "Diana's Favor", Material.NETHER_STAR, Tiers.THREE,
            new double[]{1, 2, 3},
            "<7>Grants <b>+%s✯ Magic Find <7>on <2>Mythological\n<7>mobs.", null),
    ELUSIVE_HUNTER(CarnivalPerkEvent.MYTHOLOGICAL_RITUAL, "Elusive Hunter", Material.LILY_PAD, Tiers.TWO,
            new double[]{2.5, 5},
            "<7>Grants <d>+%s Tracking <7>while digging\n<7>up <e>Griffin Burrows<7>.", null),

    CANDY_SCAVENGER(CarnivalPerkEvent.SPOOKY_FESTIVAL, "Candy Scavenger", Material.BUNDLE, Tiers.FIVE,
            new double[]{2, 4, 6, 8, 10},
            "<7>Gain <a>+%s% <7>more <d>Candy <7>from all\n<7>sources during the <6>Spooky Festival<7>.", null),
    SPOOKY_SAVANT(CarnivalPerkEvent.SPOOKY_FESTIVAL, "Spooky Savant", Material.WITHER_SKELETON_SKULL, Tiers.FIVE,
            new double[]{1, 2, 3, 4, 5},
            "<7>Grants a <a>+%s% <7>chance to catch\n<6>Spooky Sea Creatures <7>during the\n<6>Spooky Festival<7>.",
            "Spooky Sea Creatures aren't in this server yet."),
    PURPLE_VOGUE(CarnivalPerkEvent.SPOOKY_FESTIVAL, "Purple Vogue", Material.PURPLE_DYE, Tiers.THREE,
            new double[]{1, 2, 3},
            "<7>Grants <a>+%s% <7>chance for <a>Green Candy\n<7>drops to be <5>Purple Candy <7>instead.", null),
    TRICK_OR_TREAT_ENTHUSIAST(CarnivalPerkEvent.SPOOKY_FESTIVAL, "Trick or Treat Enthusiast", Material.CHEST, Tiers.TWO,
            new double[]{0.5, 1},
            "<7>Increases your chances of finding\n<6>Trick or Treat Chests <7>and <d>Party\n<d>Chests <7>by <a>+%s%<7>.",
            "Trick or Treat Chests and Party Chests aren't in this server yet."),

    ICY_HOOK(CarnivalPerkEvent.SEASON_OF_JERRY, "Icy Hook", Material.SNOW_BLOCK, Tiers.FIVE,
            new double[]{0.5, 1, 1.5, 2, 2.5},
            "<7>Grants <3>+%sα Sea Creature Chance\n<7>while on <c>Jerry's Workshop<7>.", null),
    PRESENT_CONNOISSEUR(CarnivalPerkEvent.SEASON_OF_JERRY, "Present Connoisseur", Material.WHITE_WOOL, Tiers.FIVE,
            new double[]{2, 4, 6, 8, 10},
            "<7>Gain <a>+%s% <7>more <c>Gifts <7>from all sources\n<7>during the <c>Season of Jerry<7>.", null),
    PROFESSIONAL_GIFTER(CarnivalPerkEvent.SEASON_OF_JERRY, "Professional Gifter", Material.GOLD_NUGGET, Tiers.TWO,
            new double[]{5, 10},
            "<7>Gain <a>+%s% <7>more <6>Coins <7>and <3>Skill EXP\n<7>from <c>Gifts<7>.", null),
    RED_FAD(CarnivalPerkEvent.SEASON_OF_JERRY, "Red Fad", Material.RED_WOOL, Tiers.THREE,
            new double[]{1, 2, 3},
            "<7>Grants a <a>+%s% <7>chance for <c>Gift <7>drops\n<7>to be <c>Red Gifts<7>.", null),

    NATURAL_TALENT(CarnivalPerkEvent.HARVEST_FEAST, "Natural Talent", Material.WHEAT_SEEDS, Tiers.FIVE,
            new double[]{1, 2, 3, 4, 5},
            "<7>Grants a <a>+%s% <7>chance to find\n<2>Seasonings <7>during a <6>Harvest Feast<7>.", null),
    FORTUNATE_FEASTING(CarnivalPerkEvent.HARVEST_FEAST, "Fortunate Feasting", Material.GOLDEN_HOE, Tiers.FIVE,
            new double[]{5, 10, 15, 20, 25},
            "<7>Grants <6>+%s☘ Farming Fortune <7>during\n<7>a <6>Harvest Feast<7>.", null),
    FEAST_CRASHERS(CarnivalPerkEvent.HARVEST_FEAST, "Feast Crashers", Material.TNT_MINECART, Tiers.THREE,
            new double[]{2, 4, 6},
            "<7>Grants <2>+%sഊ Bonus Pest Chance\n<7>during a <6>Harvest Feast<7>.", null),
    LEFTOVERS(CarnivalPerkEvent.HARVEST_FEAST, "Leftovers", Material.POTION, Tiers.TWO,
            new double[]{1, 2},
            "<7>Earn <a>+%s <5>Feast Flask <7>at the final\n<7>Donation Tier of each <6>Harvest Feast<7>.", null);

    private final CarnivalPerkEvent event;
    private final String displayName;
    private final Material icon;
    private final Tiers tiers;
    private final double[] values;
    private final String descriptionTemplate;
    private final @Nullable String unavailableReason;

    CarnivalPerk(CarnivalPerkEvent event, String displayName, Material icon, Tiers tiers, double[] values,
                 String descriptionTemplate, @Nullable String unavailableReason) {
        this.event = event;
        this.displayName = displayName;
        this.icon = icon;
        this.tiers = tiers;
        this.values = values;
        this.descriptionTemplate = descriptionTemplate;
        this.unavailableReason = unavailableReason;
    }

    public int maxTier() {
        return tiers.costs.size();
    }

    public boolean isAvailable() {
        return unavailableReason == null;
    }

    /** The perk's bonus at a tier, 0 when not bought. */
    public double valueAt(int tier) {
        return tier <= 0 ? 0 : values[Math.min(tier, values.length) - 1];
    }

    public int costOf(int tier) {
        return tiers.costs.get(tier - 1);
    }

    public int xpOf(int tier) {
        return Tiers.XP.get(tier - 1);
    }

    public String description(int tier) {
        return String.format(descriptionTemplate.replace("%s%", "%s%%"), formatValue(valueAt(tier)));
    }

    static String formatValue(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    public String tierName(int tier) {
        return displayName + " " + StringUtility.getAsRomanNumeral(tier);
    }

    public static List<CarnivalPerk> of(CarnivalPerkEvent event) {
        return java.util.Arrays.stream(values()).filter(perk -> perk.event == event).toList();
    }

    public enum Tiers {
        FIVE(List.of(50, 100, 150, 200, 250)),
        THREE(List.of(100, 200, 400)),
        TWO(List.of(100, 1_000));

        /** SkyBlock XP for buying each tier; the four perks of one event add up to 49 XP. */
        static final List<Integer> XP = List.of(2, 2, 3, 5, 7);

        private final List<Integer> costs;

        Tiers(List<Integer> costs) {
            this.costs = costs;
        }
    }

    private static final class Reasons {
        static final String HARVEST_FEAST = "The Harvest Feast isn't in this server yet.";
    }
}
