package me.lucko.spark.common.util;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticFormatterTest {
    private static String plain(TextComponent component) {
        return flatten(component);
    }

    private static String flatten(Component component) {
        StringBuilder out = new StringBuilder();
        if (component instanceof TextComponent text) out.append(text.content());
        component.children().forEach(child -> out.append(flatten(child)));
        return out.toString();
    }

    @Test
    void tpsColorBandsRelativeToTarget() {
        assertEquals(NamedTextColor.GREEN, StatisticFormatter.formatTps(19.5, 20).color());
        assertEquals(NamedTextColor.YELLOW, StatisticFormatter.formatTps(17.0, 20).color());
        assertEquals(NamedTextColor.RED, StatisticFormatter.formatTps(10.0, 20).color());
    }

    @Test
    void tpsAboveTargetIsCappedAndStarred() {
        assertEquals("*20.0", StatisticFormatter.formatTps(25.0, 20).content());
        assertEquals("19.57", StatisticFormatter.formatTps(19.567, 20).content());
    }

    @Test
    void tickDurationColorBands() {
        assertEquals(NamedTextColor.GREEN, StatisticFormatter.formatTickDuration(10, 50).color());
        assertEquals(NamedTextColor.YELLOW, StatisticFormatter.formatTickDuration(40, 50).color());
        assertEquals(NamedTextColor.RED, StatisticFormatter.formatTickDuration(50, 50).color());
        assertEquals("12.3", StatisticFormatter.formatTickDuration(12.34, 50).content());
    }

    @Test
    void pingRttRoundsUpAndColors() {
        assertEquals("43", StatisticFormatter.formatPingRtt(42.1).content());
        assertEquals(NamedTextColor.GREEN, StatisticFormatter.formatPingRtt(99).color());
        assertEquals(NamedTextColor.YELLOW, StatisticFormatter.formatPingRtt(100).color());
        assertEquals(NamedTextColor.RED, StatisticFormatter.formatPingRtt(200).color());
    }

    @Test
    void pingRttsJoinFourValuesWithSlashes() {
        assertEquals("1/2/3/4", plain(StatisticFormatter.formatPingRtts(1, 2, 3, 4)));
    }

    @Test
    void cpuUsageColorBands() {
        assertEquals(NamedTextColor.GREEN, StatisticFormatter.formatCpuUsage(0.5).color());
        assertEquals(NamedTextColor.YELLOW, StatisticFormatter.formatCpuUsage(0.7).color());
        assertEquals(NamedTextColor.RED, StatisticFormatter.formatCpuUsage(0.95).color());
    }

    @Test
    void diskDiagramSplitsUsedAndFree() {
        assertEquals("[┃┃┃╻╻╻╻╻╻╻]", plain(StatisticFormatter.generateDiskUsageDiagram(30, 100, 10)));
    }

    @Test
    void memoryDiagramHasRequestedWidth() {
        var usage = new java.lang.management.MemoryUsage(0, 25, 50, 100);
        String bar = plain(StatisticFormatter.generateMemoryUsageDiagram(usage, 20));
        assertEquals(22, bar.length());
        assertEquals(5, bar.chars().filter(c -> c == '┃').count());
    }
}
