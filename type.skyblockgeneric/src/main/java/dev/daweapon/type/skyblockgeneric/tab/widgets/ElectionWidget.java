package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.elections.ElectionData;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class ElectionWidget implements TabWidget {

    @Override
    public String id() {
        return "election";
    }

    @Override
    public String displayName() {
        return "Election";
    }

    @Override
    public Material icon() {
        return Material.PAPER;
    }

    @Override
    public int defaultPriority() {
        return 20;
    }

    @Override
    public boolean enabledByDefault(TabLocation location) {
        return location == TabLocation.HUB;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        ElectionData data = ElectionManager.getElectionData();
        if (data.isElectionOpen() && !data.getCandidates().isEmpty()) return electionLines(data);
        SkyBlockMayor mayor = data.getMayorEnum();
        if (mayor == null) return List.of();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<e><l>Election:"));
        lines.add(Text.of(" <7>Mayor: {}", ElectionData.colored(data.getCurrentMayorColor(), mayor.getDisplayName())));
        SkyBlockMayor minister = data.getMinisterEnum();
        if (minister != null) {
            lines.add(Text.of(" <7>Minister: {}", ElectionData.colored(data.getCurrentMinisterColor(), minister.getDisplayName())));
        }
        return lines;
    }

    private List<Text> electionLines(ElectionData data) {
        java.util.Map<String, Long> tally = data.tallyVotes();
        long total = tally.values().stream().mapToLong(Long::longValue).sum();
        List<ElectionData.CandidateData> ranked = new ArrayList<>(data.getCandidates());
        ranked.sort(java.util.Comparator.comparingLong(
                (ElectionData.CandidateData candidate) -> tally.getOrDefault(candidate.getMayorName(), 0L)).reversed());
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<e><l>Election:"));
        for (ElectionData.CandidateData candidate : ranked.stream().limit(3).toList()) {
            long votes = tally.getOrDefault(candidate.getMayorName(), 0L);
            long percent = total == 0 ? 0 : Math.round(votes * 100.0 / total);
            int bars = (int) Math.round(percent / 5.0);
            lines.add(Text.of(" {}: ", candidate.getColoredName())
                    .append(Text.of("<color:{0}>{1}", candidate.getColor(), "|".repeat(bars)))
                    .append(Text.of("<7>{} <f>({}%)", "|".repeat(20 - bars), percent)));
        }
        return lines;
    }
}
