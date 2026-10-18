package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoEruptionService;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoul;
import dev.daweapon.commons.StringUtility;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class GeneralInfoWidget implements TabWidget {
    private static final String GEMS = "gems";
    private static final String FAIRY_SOULS = "fairy_souls";

    @Override
    public String id() {
        return "general_info";
    }

    @Override
    public String displayName() {
        return "General Info";
    }

    @Override
    public Material icon() {
        return Material.NAME_TAG;
    }

    @Override
    public int defaultPriority() {
        return 0;
    }

    @Override
    public boolean alwaysOn() {
        return true;
    }

    @Override
    public List<TabWidgetToggle> toggles() {
        return List.of(
                new TabWidgetToggle(GEMS, "Gems", true),
                new TabWidgetToggle(FAIRY_SOULS, "Fairy Souls", true));
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<a><l>Area: <f>{}", context.location().getDisplayName()));
        lines.add(Text.of(" <7>Server: <8>{}", HypixelConst.getServerName()));
        if (context.shows(this, GEMS)) {
            lines.add(Text.of(" <7>Gems: <a>{:,}", player.getGems()));
        }
        if (context.shows(this, FAIRY_SOULS)) {
            lines.add(Text.of(" <7>Fairy Souls: <d>{}<7>/<d>{}",
                    player.getFairySoulHandler().getTotalFoundFairySouls(), FairySoul.getFairySouls().size()));
        }
        if (context.location() == TabLocation.GARDEN) addGarden(lines, player);
        if (context.location() == TabLocation.CRIMSON_ISLE) addVolcano(lines);
        return lines;
    }

    private void addGarden(List<Text> lines, SkyBlockPlayer player) {
        GardenProfile garden = Gardens.profile(player);
        int level = GardenProgress.level(garden);
        lines.add(Text.of(" <7>Garden Level: <a>{}", level > 0 ? StringUtility.getAsRomanNumeral(level) : "0"));
        lines.add(Text.of(" <7>Copper: <c>{:,}", Math.floor(garden.getCopper())));
        lines.add(Text.of(" <7>Sowdust: <2>{:,}", Math.floor(garden.getSowdust())));
    }

    private void addVolcano(List<Text> lines) {
        VolcanoExplosivity explosivity = VolcanoEruptionService.currentExplosivity();
        lines.add(Text.of(" <7>Volcano: {}", explosivity == null ? "<7>INACTIVE" : explosivity.coloured()));
    }
}
