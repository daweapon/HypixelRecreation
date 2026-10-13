package net.swofty.type.thegarden.tab;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.garden.pest.PestData;
import net.swofty.type.skyblockgeneric.garden.pest.PestItems;
import net.swofty.type.skyblockgeneric.garden.pest.PestService;
import net.swofty.type.skyblockgeneric.garden.pest.PestTraps;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GardenPestsModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        GardenProfile profile = Gardens.profile(player);
        List<TablistEntry> entries = new ArrayList<>();
        if (PestService.unlocked(profile)) {
            PestData data = profile.pests();
            long now = System.currentTimeMillis();
            entries.add(new TablistEntry(Text.of("<a><l>Pests:"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Alive: <a>{}", data.aliveCount()), TablistSkinRegistry.GRAY));
            if (data.aliveCount() > 0) {
                TreeSet<Integer> plots = new TreeSet<>();
                data.getAlive().forEach(alive -> plots.add(alive.getPlot()));
                entries.add(new TablistEntry(Text.of(" <7>Plots: <a>{}", Text.parse(String.join("<7>, <a>", plots.stream().map(String::valueOf).toList()))), TablistSkinRegistry.GRAY));
            }
            entries.add(new TablistEntry(Text.of(" <7>Spray: <a>{}", Text.parse(spray(data, now))), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Repellent: <a>{}", data.getRepellentUntil() > now ? minutes(data.getRepellentUntil() - now) : "None"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Bonus: {}", Text.parse(data.getBoostUntil() > now ? "<a>+" + data.getBoostFortune() + "☘ (" + minutes(data.getBoostUntil() - now) + ")" : "<c>INACTIVE")), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Cooldown: {}", Text.parse(data.getCooldownUntil() > now ? "<c>" + clock(data.getCooldownUntil() - now) : "<a>READY")), TablistSkinRegistry.GRAY));
            int loss = PestService.fortuneLoss(player, profile);
            if (loss > 0) entries.add(new TablistEntry(Text.of(" <c>Fortune reduced by {}%", loss), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of("<a><l>Pest Traps:"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Placed: <a>{}/{}", data.getTraps().size(), PestTraps.MAX_TRAPS), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Full Traps: <a>{}", Text.parse(trapList(data, true))), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>No Bait: <a>{}", Text.parse(trapList(data, false))), TablistSkinRegistry.GRAY));
        }
        GreenhouseData greenhouse = profile.greenhouse();
        if (!greenhouse.getBeds().isEmpty()) {
            entries.add(new TablistEntry(Text.of("<a><l>Greenhouse:"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Next Stage: <a>{}", clock(GreenhouseService.nextStageIn(greenhouse))), TablistSkinRegistry.GRAY));
        }
        fillRestWithGray(entries);
        return entries;
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
                .map(entry -> PestItems.materialName(entry.getValue().material()) + " <7>(" + minutes(entry.getValue().until() - now) + " in " + entry.getKey() + ")")
                .findFirst().orElse("None");
    }

    private static String minutes(long millis) {
        return (millis / 60_000 + 1) + "m";
    }

    private static String clock(long millis) {
        long seconds = millis / 1000;
        if (seconds >= 3600) return seconds / 3600 + "h " + seconds % 3600 / 60 + "m";
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }
}
