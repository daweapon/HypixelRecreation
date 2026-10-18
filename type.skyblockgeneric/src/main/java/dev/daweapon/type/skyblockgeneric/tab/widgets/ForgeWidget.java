package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.forge.ForgeRecipe;
import dev.daweapon.type.skyblockgeneric.forge.ForgeRegistry;
import dev.daweapon.type.skyblockgeneric.forge.ForgeService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmRegistry;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ForgeWidget implements TabWidget {
    private static final int MAX_HOTM_TIER = 10;

    @Override
    public String id() {
        return "forge";
    }

    @Override
    public String displayName() {
        return "Forges";
    }

    @Override
    public Material icon() {
        return Material.ANVIL;
    }

    @Override
    public int defaultPriority() {
        return 100;
    }

    @Override
    public Set<TabLocation> locations() {
        return Set.of(TabLocation.HUB, TabLocation.DWARVEN_MINES, TabLocation.CRYSTAL_HOLLOWS, TabLocation.GOLD_MINE,
                TabLocation.DEEP_CAVERNS);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        int unlocked = ForgeService.slots(player);
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<e><l>Forges:"));
        for (int slot = 1; slot <= ForgeService.MAX_SLOTS; slot++) {
            if (slot > unlocked) {
                lines.add(Text.of(" <8>{}) <c>LOCKED <7>(Needs HotM {})", slot, tierForSlot(slot)));
                continue;
            }
            DatapointHOTM.ForgeProcess process = ForgeService.processIn(player, slot);
            if (process == null) {
                lines.add(Text.of(" <8>{}) <7>Empty", slot));
                continue;
            }
            ForgeRecipe recipe = ForgeRegistry.get().recipe(process.recipeId());
            String name = recipe == null ? process.recipeId() : recipe.name();
            String time = ForgeService.isFinished(process) ? "<a>Done" : "<e>" + TabTime.format(ForgeService.remainingMillis(process));
            lines.add(Text.of(" <8>{}) <f>{}: {}", slot, name, Text.of(time)));
        }
        return lines;
    }

    private static int tierForSlot(int slot) {
        for (int tier = 1; tier <= MAX_HOTM_TIER; tier++) {
            if (HotmRegistry.get().tierReward(tier).forgeSlots() >= slot) return tier;
        }
        return MAX_HOTM_TIER;
    }
}
