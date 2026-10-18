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
        return 150;
    }

    @Override
    public boolean enabledByDefault(TabLocation location) {
        return location == TabLocation.HUB;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        ElectionData data = ElectionManager.getElectionData();
        SkyBlockMayor mayor = data.getMayorEnum();
        if (mayor == null) return List.of();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<b><l>Election:"));
        lines.add(Text.of(" <7>Mayor: {}", ElectionData.colored(data.getCurrentMayorColor(), mayor.getDisplayName())));
        SkyBlockMayor minister = data.getMinisterEnum();
        if (minister != null) {
            lines.add(Text.of(" <7>Minister: {}", ElectionData.colored(data.getCurrentMinisterColor(), minister.getDisplayName())));
        }
        if (data.isElectionOpen()) lines.add(Text.of(" <a>Voting is open!"));
        return lines;
    }
}
