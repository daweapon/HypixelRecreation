package dev.daweapon.type.crimsonisle.dojo.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.crimsonisle.dojo.DojoFormat;
import dev.daweapon.type.crimsonisle.dojo.DojoService;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoBelt;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

/** Master Tao's "Challenges" menu, laid out from the wiki's Master Tao/UI page. */
public class GUIDojoChallenges extends StatelessView {
    private static final Map<DojoTest, Integer> SLOTS = Map.of(
            DojoTest.FORCE, 11,
            DojoTest.STAMINA, 12,
            DojoTest.MASTERY, 13,
            DojoTest.DISCIPLINE, 14,
            DojoTest.SWIFTNESS, 15,
            DojoTest.CONTROL, 21,
            DojoTest.TENACITY, 23);
    private static final int RANK_SLOT = 41;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Challenges", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        SLOTS.forEach((test, slot) -> layout.slot(slot,
                (_, c) -> testItem((SkyBlockPlayer) c.player(), test),
                (_, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (!DojoProgress.isUnlocked(player, test)) return;
                    DojoService.request(player, test);
                }));
        layout.slot(RANK_SLOT, (_, c) -> rankItem((SkyBlockPlayer) c.player()),
                (_, c) -> c.push(new GUIDojoBeltRewards()));
    }

    private static ItemStack.Builder testItem(SkyBlockPlayer player, DojoTest test) {
        StringBuilder lore = new StringBuilder("<9>" + test.fullName() + "\n");
        if (!DojoProgress.isUnlocked(player, test)) {
            DojoTest previous = test.previous();
            lore.append("<7>Get at least a <a>D <7>rank in the\n")
                    .append("<9>").append(previous == null ? "" : previous.fullName()).append(" <7>to unlock this\n")
                    .append("<7>challenge.\n\n<c>Locked!");
            return ItemStacks.item(Material.GRAY_DYE, lore.toString());
        }
        lore.append(DojoFormat.rankLine(player, test)).append("\n")
                .append(DojoFormat.timeLine(player, test)).append("\n\n")
                .append("<b>Challenge Rules:\n\n");
        test.getRules().forEach(rule -> lore.append(rule).append("\n"));
        lore.append("\n<e>Click to start challenge!");
        return ItemStacks.item(test.getIcon(), lore.toString());
    }

    private static ItemStack.Builder rankItem(SkyBlockPlayer player) {
        long points = DojoProgress.points(player);
        StringBuilder lore = new StringBuilder("<6>Rank\n")
                .append("<7>Your Points: <e>").append(StringUtility.commaify(points)).append("\n")
                .append("<7>Your Rank: ").append(DojoBelt.reached(points).coloured()).append("\n\n");
        for (DojoBelt belt : DojoBelt.values()) {
            lore.append(belt.coloured()).append("<f>: <e>").append(belt.getRequiredPoints()).append("\n");
        }
        lore.append("\n<e>Click to view rewards.");
        return ItemStacks.item(Material.GOLD_BLOCK, lore.toString());
    }
}
