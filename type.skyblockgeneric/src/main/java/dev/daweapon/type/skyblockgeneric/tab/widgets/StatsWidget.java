package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class StatsWidget implements TabWidget {
    private record Entry(String id, ItemStatistic statistic, boolean defaultShown) {
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry("speed", ItemStatistic.SPEED, true),
            new Entry("strength", ItemStatistic.STRENGTH, true),
            new Entry("crit_chance", ItemStatistic.CRITICAL_CHANCE, true),
            new Entry("crit_damage", ItemStatistic.CRITICAL_DAMAGE, true),
            new Entry("attack_speed", ItemStatistic.BONUS_ATTACK_SPEED, true),
            new Entry("health", ItemStatistic.HEALTH, false),
            new Entry("defense", ItemStatistic.DEFENSE, false),
            new Entry("intelligence", ItemStatistic.INTELLIGENCE, false),
            new Entry("ferocity", ItemStatistic.FEROCITY, false),
            new Entry("mining_speed", ItemStatistic.MINING_SPEED, false),
            new Entry("mining_fortune", ItemStatistic.MINING_FORTUNE, false),
            new Entry("farming_fortune", ItemStatistic.FARMING_FORTUNE, false),
            new Entry("foraging_fortune", ItemStatistic.FORAGING_FORTUNE, false),
            new Entry("magic_find", ItemStatistic.MAGIC_FIND, false));

    private static String label(Entry entry) {
        return entry.id().equals("attack_speed") ? "Attack Speed" : entry.statistic().getDisplayName();
    }

    @Override
    public String id() {
        return "stats";
    }

    @Override
    public String displayName() {
        return "Stats";
    }

    @Override
    public Material icon() {
        return Material.IRON_SWORD;
    }

    @Override
    public int defaultPriority() {
        return 40;
    }

    @Override
    public List<TabWidgetToggle> toggles() {
        return ENTRIES.stream()
                .map(entry -> new TabWidgetToggle(entry.id(), label(entry), entry.defaultShown()))
                .toList();
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        ItemStatistics statistics = player.getStatistics().allStatistics();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<a><l>Stats:"));
        for (Entry entry : ENTRIES) {
            if (!context.chosen(this, entry.id())) continue;
            lines.add(Text.of(" <7>{}: ", label(entry))
                    .append(Text.component(AccountInformationModule.stat(entry.statistic(), statistics))));
        }
        return lines;
    }
}
