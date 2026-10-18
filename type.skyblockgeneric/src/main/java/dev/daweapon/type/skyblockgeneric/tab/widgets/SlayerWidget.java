package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerQuest;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerService;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerTierDefinition;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

public final class SlayerWidget implements TabWidget {

    @Override
    public String id() {
        return "slayer";
    }

    @Override
    public String displayName() {
        return "Slayer";
    }

    @Override
    public Material icon() {
        return Material.IRON_SWORD;
    }

    @Override
    public int defaultPriority() {
        return 180;
    }

    @Override
    public boolean enabledByDefault(TabLocation location) {
        return false;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        SlayerService.QuestStatus status = SlayerService.status(player);
        SlayerQuest quest = status.quest();
        Text header = Text.of("<4><l>Slayer:");
        if (quest == null) return List.of(header, Text.of(" <7>None"));
        Text boss = Text.of("{} {}", quest.type().coloredName(), quest.tier().numeral());
        SlayerTierDefinition tier = status.tier();
        if (quest.bossSpawned()) return List.of(header, Text.of(" {} <c>Slay the boss!", boss));
        int required = tier == null ? 0 : tier.requiredCombatXp();
        if (required <= 0) return List.of(header, Text.of(" {}", boss));
        return List.of(header, Text.of(" {} <e>{}<7>/<c>{}", boss, quest.combatXp(), required));
    }
}
