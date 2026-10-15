package net.swofty.type.skyblockgeneric.commission;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.gems.GemstoneColours;
import net.swofty.type.skyblockgeneric.region.RegionType;

// A single commission task, pairing an objective with a display description.
public class Commission {
    public final String name;
    public final CommissionCategory category;
    public final Objective objective;
    public final boolean oneTimeOnly;
    public final CommissionZone zone;
    public final boolean crystalGroup;

    public boolean isGlacite() {
        return objective.target.isGlacite();
    }

    public int requiredHotmTier() {
        return isGlacite() ? 7 : 0;
    }

    public Commission(
            String name,
            CommissionCategory category,
            Objective objective,
            boolean oneTimeOnly
    ) {
        this(name, category, objective, oneTimeOnly, CommissionZone.DWARVEN_MINES, false);
    }

    public Commission(
            String name,
            CommissionCategory category,
            Objective objective,
            boolean oneTimeOnly,
            CommissionZone zone,
            boolean crystalGroup
    ) {
        this.zone = zone;
        this.crystalGroup = crystalGroup;
        this.name = name;
        this.category = category;
        this.objective = objective;
        this.oneTimeOnly = oneTimeOnly;
    }

    public String nameColourTag() {
        Gemstone gemstone = GemstoneColours.of(objective.target);
        return gemstone == null ? "<9>" : GemstoneColours.tag(gemstone);
    }

    public Text generateDescription() {
        return switch (objective.type) {
            case MINE -> withSuffix(Text.of(isOre(objective.target) ? "Mine <a>{} <7>{} Ore" : "Mine <a>{} <7>{}",
                    objective.amount, getTargetName(objective.target)), true, false);
            case OPEN -> withSuffix(Text.of("Open <a>{} <7>chests", objective.amount), true, false);
            case SLAY -> withSuffix(Text.of("Slay <a>{} <7>{}",
                    objective.amount, getTargetNamePlural(objective.target)), true, true);
            case DAMAGE -> withSuffix(Text.of("Damage {} <7>{} times",
                    getTargetNamePlural(objective.target), objective.amount), true, false);
            case PARTICIPATE -> Text.of("Participate in the {}.", getEventName(objective.event));
            case COLLECT -> withSuffix(collectText(), false, true);
            case DEPOSIT -> withSuffix(Text.of("Deposit {} Tickets", objective.amount), false, true);
        };
    }

    private Text collectText() {
        Gemstone gemstone = GemstoneColours.of(objective.target);
        if (gemstone == null || !objective.target.isGemstone()) {
            return Text.of("Collect {} {}", objective.amount, getCollectibleName(objective.target));
        }
        return Text.of("Collect {} " + GemstoneColours.tag(gemstone) + "{}<7>", objective.amount, getCollectibleName(objective.target));
    }

    private static boolean isOre(Objective.BlockTarget target) {
        return target == Objective.BlockTarget.MITHRIL || target == Objective.BlockTarget.TITANIUM;
    }

    private Text withSuffix(Text base, boolean allowLocation, boolean allowEvent) {
        RegionType region = allowLocation && !objective.location.isAny()
                ? objective.location.getRegion().orElse(null)
                : null;

        if (allowEvent && objective.event != EventType.NONE) {
            Text located = region == null ? base : base.append(" in <b>{}", region.getName());
            return located.append(" during the {}.", getEventName(objective.event));
        }
        if (region != null) {
            return base.append(" in <b>{}.", region.getName());
        }
        return base.append(".");
    }

    private String getTargetName(Objective.BlockTarget target) {
        return switch (target) {
            case MITHRIL -> "Mithril";
            case TITANIUM -> "Titanium";
            case GOBLIN -> "Goblin";
            case GLACITE_WALKER -> "Glacite Walker";
            case TREASURE_HOARDER -> "Treasure Hoarder";
            case GOLDEN_GOBLIN -> "Golden Goblin";
            case STAR_SENTRY -> "Star Sentry";
            case UMBER -> "Umber";
            case TUNGSTEN -> "Tungsten";
            case GLACITE -> "Glacite";
            case AQUAMARINE -> "Aquamarine";
            case CITRINE -> "Citrine";
            case ONYX -> "Onyx";
            case PERIDOT -> "Peridot";
            case HARD_STONE -> "Hard Stone";
            case HOLLOWS_AUTOMATON -> "Automaton";
            case HOLLOWS_SLUDGE -> "Sludge";
            case HOLLOWS_TREASURITE_MEMBER -> "Team Treasurite Member";
            case HOLLOWS_GOBLIN -> "Goblin";
            case HOLLOWS_YOG -> "Yog";
            case HOLLOWS_THYST -> "Thyst";
            case HOLLOWS_BOSS_CORLEONE -> "Boss Corleone";
            case HOLLOWS_CRYSTAL -> "Crystal";
            case CHEST -> "Chest";
            case GEM_AMBER -> "Amber";
            case GEM_AMETHYST -> "Amethyst";
            case GEM_JADE -> "Jade";
            case GEM_RUBY -> "Ruby";
            case GEM_SAPPHIRE -> "Sapphire";
            case GEM_TOPAZ -> "Topaz";
            case NONE -> "";
        };
    }

    private Text getTargetNamePlural(Objective.BlockTarget target) {
        return switch (target) {
            case MITHRIL -> Text.of("Mithril Ore");
            case TITANIUM -> Text.of("Titanium Ore");
            case GOBLIN -> Text.of("<c>Goblins");
            case GLACITE_WALKER -> Text.of("<b>Glacite Walkers");
            case TREASURE_HOARDER -> Text.of("<c>Treasure Hoarders");
            case GOLDEN_GOBLIN -> Text.of("<6>Golden Goblin");
            case STAR_SENTRY -> Text.of("Star Sentrys");
            case UMBER, TUNGSTEN, GLACITE, AQUAMARINE, CITRINE, ONYX, PERIDOT -> Text.of(getTargetName(target));
            case HOLLOWS_AUTOMATON -> Text.of("<c>Automatons");
            case HOLLOWS_SLUDGE -> Text.of("<a>Sludges");
            case HOLLOWS_TREASURITE_MEMBER -> Text.of("<c>Team Treasurite Members");
            case HOLLOWS_GOBLIN -> Text.of("<c>Goblins");
            case HOLLOWS_YOG -> Text.of("<c>Yogs");
            case HOLLOWS_THYST -> Text.of("<5>Thysts");
            case HOLLOWS_BOSS_CORLEONE -> Text.of("<6>Boss Corleone");
            case HOLLOWS_CRYSTAL -> Text.of("Crystals");
            case HARD_STONE, CHEST, GEM_AMBER, GEM_AMETHYST, GEM_JADE, GEM_RUBY, GEM_SAPPHIRE, GEM_TOPAZ ->
                    Text.of(getTargetName(target));
            case NONE -> Text.empty();
        };
    }

    private String getCollectibleName(Objective.BlockTarget target) {
        return switch (target) {
            case MITHRIL -> "Mithril Powder";
            case HOLLOWS_CRYSTAL -> "Gemstone Crystal";
            case GEM_AMBER, GEM_AMETHYST, GEM_JADE, GEM_RUBY, GEM_SAPPHIRE, GEM_TOPAZ -> getTargetName(target) + " Gemstones";
            default -> target.name();
        };
    }

    private Text getEventName(EventType event) {
        return switch (event) {
            case GOBLIN_RAID -> Text.of("<c>Goblin Raid <7>Event");
            case RAFFLE -> Text.of("<e>Raffle <7>Event");
            case DOUBLE_POWDER -> Text.of("2x Powder Event");
            case NONE -> Text.empty();
        };
    }
}
