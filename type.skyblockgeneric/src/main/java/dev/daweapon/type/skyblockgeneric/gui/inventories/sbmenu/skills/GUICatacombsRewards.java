package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.skills;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsSkillDisplay;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUICatacombsRewards extends StatelessView {
    private static final int[] DISPLAY_SLOTS = {
            9, 18, 27, 28, 29, 20, 11, 2, 3, 4, 13, 22, 31, 32, 33, 24, 15, 6, 7, 8, 17, 26, 35, 44, 53
    };

    private final int page;

    public GUICatacombsRewards(int page) {
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Catacombs Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(0, (s, c) -> GUIDungeoneering.catacombsIcon((SkyBlockPlayer) c.player(), false));

        int pageSize = DISPLAY_SLOTS.length;
        int pageCount = (CatacombsLevels.MAXIMUM_LEVEL + pageSize - 1) / pageSize;
        int targetPage = (page + 1) % pageCount;
        int firstTarget = targetPage * pageSize + 1;
        int lastTarget = Math.min(CatacombsLevels.MAXIMUM_LEVEL, (targetPage + 1) * pageSize);
        layout.slot(50, ItemStacks.item(Material.ARROW, 1, Text.of("<a>Levels {} - {}", firstTarget, lastTarget), List.of()),
                (click, c) -> c.replace(new GUICatacombsRewards(targetPage)));

        int firstLevel = page * pageSize + 1;
        for (int i = 0; i < pageSize; i++) {
            int level = firstLevel + i;
            if (level > CatacombsLevels.MAXIMUM_LEVEL) break;
            layout.slot(DISPLAY_SLOTS[i], (s, c) -> levelIcon((SkyBlockPlayer) c.player(), level));
        }
    }

    private static net.minestom.server.item.ItemStack.Builder levelIcon(SkyBlockPlayer player, int level) {
        long xp = CatacombsProgress.xp(player);
        int current = CatacombsLevels.levelForXp(xp);
        List<Text> lore = new ArrayList<>();
        CatacombsSkillDisplay.rewardLines(level).forEach(line -> lore.add(Text.parse(line)));
        lore.add(Text.empty());

        Material icon;
        String colour;
        if (current >= level) {
            icon = Material.LIME_STAINED_GLASS_PANE;
            colour = "<a>";
            lore.add(Text.of("<a><l>UNLOCKED"));
        } else if (current + 1 == level) {
            icon = Material.YELLOW_STAINED_GLASS_PANE;
            colour = "<e>";
            CatacombsSkillDisplay.progressLines(xp, "<7>Progress: ").forEach(line -> lore.add(Text.parse(line)));
        } else {
            icon = Material.RED_STAINED_GLASS_PANE;
            colour = "<c>";
        }
        return ItemStacks.item(icon, level, Text.of(colour + "Catacombs Level {}", level), lore);
    }
}
