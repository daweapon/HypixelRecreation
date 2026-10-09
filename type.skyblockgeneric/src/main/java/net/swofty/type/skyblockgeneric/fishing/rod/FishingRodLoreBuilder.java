package net.swofty.type.skyblockgeneric.fishing.rod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.FishingRodMetadataComponent;
import net.swofty.type.skyblockgeneric.item.components.FishingRodPartComponent;
import net.swofty.type.skyblockgeneric.item.components.ReforgableComponent;
import net.swofty.type.skyblockgeneric.text.LoreText;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The fishing-rod parts of an item's lore, laid out as Hypixel's rods are: the medium under the name, rod stats
 * in their own colours, the gemstone row, the hook/line/sinker slots, Roddy's hint and the requirement lines.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingRodLoreBuilder {
    private static final String PARTS = "rod_parts";
    private static final String RODDY = "rod_roddy";
    private static final int PARTS_ORDER = 84;
    private static final int RODDY_ORDER = 86;

    private record RodStat(ItemStatistic statistic, String colour) {
    }

    private record PartSlot(FishingPartCategory category, String icon, String label) {
    }

    private static final List<RodStat> STATS = List.of(
            new RodStat(ItemStatistic.DAMAGE, "<c>"),
            new RodStat(ItemStatistic.STRENGTH, "<c>"),
            new RodStat(ItemStatistic.CRITICAL_CHANCE, "<9>"),
            new RodStat(ItemStatistic.CRITICAL_DAMAGE, "<9>"),
            new RodStat(ItemStatistic.FEROCITY, "<c>"),
            new RodStat(ItemStatistic.FISHING_SPEED, "<b>"),
            new RodStat(ItemStatistic.SEA_CREATURE_CHANCE, "<3>"),
            new RodStat(ItemStatistic.DOUBLE_HOOK_CHANCE, "<9>"),
            new RodStat(ItemStatistic.TROPHY_FISH_CHANCE, "<6>"),
            new RodStat(ItemStatistic.TREASURE_CHANCE, "<6>"));

    private static final List<PartSlot> SLOTS = List.of(
            new PartSlot(FishingPartCategory.HOOK, "ථ", "Hook"),
            new PartSlot(FishingPartCategory.LINE, "ꨃ", "Line"),
            new PartSlot(FishingPartCategory.SINKER, "࿉", "Sinker"));

    public static void fillHead(LoreText lore, Rarity rarity, @Nullable Text gemstoneSlots) {
        SkyBlockItem item = lore.item();
        FishingRodMetadataComponent metadata = item.getComponent(FishingRodMetadataComponent.class);
        if (metadata.getSubtitle() != null && !metadata.getSubtitle().isBlank()) {
            lore.section(LoreText.Sections.UNDER_NAME).line(Text.of("<8>{}", metadata.getSubtitle()));
        }

        TextBody.Section stats = lore.section(LoreText.Sections.STATS);
        statLines(item, rarity).forEach(stats::line);
        if (gemstoneSlots != null) {
            lore.section(LoreText.Sections.GEMSTONES).line(Text.of("<7>Gemstones: {}", gemstoneSlots));
        }
    }

    public static void fillParts(LoreText lore) {
        SkyBlockItem item = lore.item();
        FishingRodMetadataComponent metadata = item.getComponent(FishingRodMetadataComponent.class);
        if (metadata.getLegacyConversionTarget() != null) {
            lore.section(RODDY, RODDY_ORDER).separated()
                    .line(Text.of("<7>Talk to <2>Roddy <7>in the <2>Backwater"))
                    .line(Text.of("<2>Bayou <7>to convert this rod."));
            return;
        }
        if (!metadata.isRodPartsEnabled()) return;

        partLines(item).forEach(lore.section(PARTS, PARTS_ORDER).separated()::line);
        if (FishingRodPartService.getHook(item) == null && FishingRodPartService.getLine(item) == null
                && FishingRodPartService.getSinker(item) == null) {
            lore.section(RODDY, RODDY_ORDER).separated()
                    .line(Text.of("<7>Talk to <2>Roddy <7>in the <2>Backwater"))
                    .line(Text.of("<2>Bayou <7>to apply parts to this rod."));
        }
    }

    public static void fillRequirements(LoreText lore) {
        FishingRodMetadataComponent metadata = lore.item().getComponent(FishingRodMetadataComponent.class);
        TextBody.Section section = lore.section(LoreText.Sections.REFORGEABLE);
        if (metadata.getRequiredFishingLevel() > 0) {
            section.line(Text.of("<4>❣ <c>Requires <a>Fishing Skill {}<c>.", metadata.getRequiredFishingLevel()));
        }
        metadata.getExtraRequirements().forEach(requirement -> section.line(Text.parseLenient(requirement)));
    }

    static List<Text> statLines(SkyBlockItem item, Rarity rarity) {
        ItemStatistics base = item.getAttributeHandler().getStatistics();
        ItemStatistics parts = FishingRodPartService.getStatistics(item);
        Reforge reforge = item.hasComponent(ReforgableComponent.class) ? item.getAttributeHandler().getReforge() : null;
        ItemStatistics reforged = reforge == null ? ItemStatistics.empty()
                : reforge.getAfterCalculation(ItemStatistics.empty(), rarity.ordinal() + 1);

        List<Text> lines = new ArrayList<>();
        for (RodStat stat : STATS) {
            ItemStatistic statistic = stat.statistic();
            double reforgeValue = reforged.getOverall(statistic);
            double gemValue = Gemstone.getExtraStatisticFromGemstone(statistic, item);
            double total = base.getOverall(statistic) + parts.getOverall(statistic) + reforgeValue + gemValue;
            if (total == 0) continue;

            String suffix = statistic.getIsPercentage() ? "%" : "";
            Text line = Text.of("<7>{}: " + stat.colour() + "{}{}", StringUtility.toNormalCase(statistic.getDisplayName()),
                    signed(total), suffix);
            line = line.appendIf(reforgeValue != 0, "<9> ({}{})", signed(reforgeValue), suffix);
            line = line.appendIf(gemValue != 0, "<d> ({}{})", signed(gemValue), suffix);
            lines.add(line);
        }
        return lines;
    }

    static List<Text> partLines(SkyBlockItem item) {
        List<Text> lines = new ArrayList<>();
        boolean previousFilled = false;
        for (PartSlot slot : SLOTS) {
            FishingRodPartComponent part = partIn(item, slot.category());
            if (!lines.isEmpty() && (part != null || previousFilled)) lines.add(Text.empty());
            if (part == null) {
                lines.add(Text.of("<9>{} {} <8><l>NONE", slot.icon(), slot.label()));
            } else {
                lines.add(Text.of("<9>{0} <color:{1}>{2}", slot.icon(), partRarity(part).getColor(), Text.parseLenient(part.getDisplayName())));
                lines.addAll(partDescription(part));
            }
            previousFilled = part != null;
        }
        return lines;
    }

    private static @Nullable FishingRodPartComponent partIn(SkyBlockItem rod, FishingPartCategory category) {
        return switch (category) {
            case HOOK -> FishingRodPartService.getHook(rod);
            case LINE -> FishingRodPartService.getLine(rod);
            case SINKER -> FishingRodPartService.getSinker(rod);
        };
    }

    private static Rarity partRarity(FishingRodPartComponent part) {
        SkyBlockItem item = FishingItemSupport.getItem(part.getItemId());
        return item == null ? Rarity.COMMON : item.getAttributeHandler().getRarity();
    }

    private static List<Text> partDescription(FishingRodPartComponent part) {
        List<Text> lines = new ArrayList<>();
        SkyBlockItem item = FishingItemSupport.getItem(part.getItemId());
        if (item != null) {
            ItemStatistics statistics = item.getAttributeHandler().getStatistics();
            for (RodStat stat : STATS) {
                double value = statistics.getOverall(stat.statistic());
                if (value == 0) continue;
                lines.add(Text.of("<7>Grants " + stat.colour() + "{}{} {}<7>.", signed(value),
                        stat.statistic().getIsPercentage() ? "%" : "",
                        StringUtility.toNormalCase(stat.statistic().getDisplayName())));
            }
        }
        if (part.getBaitPreservationChance() > 0) {
            lines.add(Text.of("<7>Grants a <a>{}% <7>chance to not consume", format(part.getBaitPreservationChance())));
            lines.add(Text.of("<7>Bait."));
        }
        return lines;
    }

    private static String signed(double value) {
        return (value < 0 ? "-" : "+") + format(Math.abs(value));
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) return String.valueOf((long) Math.rint(value));
        return StringUtility.decimalify(value, 1);
    }
}
