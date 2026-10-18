package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestData;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestItems;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestService;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestTraps;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

final class PestsWidget extends AreaWidget {
    PestsWidget() {
        super("pests", "Pests", Material.SPIDER_EYE, 70, TabLocation.GARDEN);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        GardenProfile profile = Gardens.profile(player);
        List<Text> lines = new ArrayList<>();
        if (PestService.unlocked(profile)) {
            PestData data = profile.pests();
            long now = System.currentTimeMillis();
            lines.add(Text.of("<a><l>Pests:"));
            lines.add(Text.of(" <7>Alive: <a>{}", data.aliveCount()));
            if (data.aliveCount() > 0) {
                TreeSet<Integer> plots = new TreeSet<>();
                data.getAlive().forEach(alive -> plots.add(alive.getPlot()));
                lines.add(Text.of(" <7>Plots: <a>{}",
                        Text.parse(String.join("<7>, <a>", plots.stream().map(String::valueOf).toList()))));
            }
            lines.add(Text.of(" <7>Spray: <a>{}", Text.parse(spray(data, now))));
            lines.add(Text.of(" <7>Repellent: <a>{}",
                    data.getRepellentUntil() > now ? AreaTabClock.minutes(data.getRepellentUntil() - now) : "None"));
            lines.add(Text.of(" <7>Bonus: {}", Text.parse(data.getBoostUntil() > now
                    ? "<a>+" + data.getBoostFortune() + "☘ (" + AreaTabClock.minutes(data.getBoostUntil() - now) + ")"
                    : "<c>INACTIVE")));
            lines.add(Text.of(" <7>Cooldown: {}", Text.parse(data.getCooldownUntil() > now
                    ? "<c>" + AreaTabClock.compact(data.getCooldownUntil() - now) : "<a>READY")));
            int loss = PestService.fortuneLoss(player, profile);
            if (loss > 0) lines.add(Text.of(" <c>Fortune reduced by {}%", loss));
            lines.add(Text.of("<a><l>Pest Traps:"));
            lines.add(Text.of(" <7>Placed: <a>{}/{}", data.getTraps().size(), PestTraps.MAX_TRAPS));
            lines.add(Text.of(" <7>Full Traps: <a>{}", Text.parse(trapList(data, true))));
            lines.add(Text.of(" <7>No Bait: <a>{}", Text.parse(trapList(data, false))));
        }
        GreenhouseData greenhouse = profile.greenhouse();
        if (!greenhouse.getBeds().isEmpty()) {
            lines.add(Text.of("<a><l>Greenhouse:"));
            lines.add(Text.of(" <7>Next Stage: <a>{}", AreaTabClock.compact(GreenhouseService.nextStageIn(greenhouse))));
        }
        return lines;
    }

    private static String trapList(PestData data, boolean full) {
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < data.getTraps().size(); i++) {
            PestData.Trap trap = data.getTraps().get(i);
            PestTraps.Kind kind = PestTraps.KINDS.get(trap.getKind());
            boolean matches = full ? kind != null && trap.getCaught().size() >= kind.capacity() : trap.getBait() == 0;
            if (matches) numbers.add(String.valueOf(i + 1));
        }
        return numbers.isEmpty() ? "None" : String.join("<7>, <a>", numbers);
    }

    private static String spray(PestData data, long now) {
        return data.getSprays().entrySet().stream()
                .filter(entry -> entry.getValue().until() > now)
                .map(entry -> PestItems.materialName(entry.getValue().material()) + " <7>("
                        + AreaTabClock.minutes(entry.getValue().until() - now) + " in " + entry.getKey() + ")")
                .findFirst().orElse("None");
    }
}
