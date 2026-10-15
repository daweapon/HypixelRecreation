package dev.daweapon.type.crimsonisle.dojo.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.crimsonisle.dojo.DojoBeltRewards;
import dev.daweapon.type.crimsonisle.dojo.DojoFormat;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoBelt;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** "Belt Rewards": one pane per belt upgrade from Yellow to Black, from the wiki's Master Tao/UI page. */
public class GUIDojoBeltRewards extends StatelessView {
    private static final int FIRST_SLOT = 20;
    private static final int BAR_LENGTH = 20;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Belt Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        DojoBelt[] belts = DojoBelt.values();
        for (int index = 1; index < belts.length; index++) {
            DojoBelt belt = belts[index];
            layout.slot(FIRST_SLOT + index - 1, (_, c) -> rewardItem((SkyBlockPlayer) c.player(), belt), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (DojoBeltRewards.claim(player, belt)) c.session(Object.class).refresh();
            });
        }
    }

    private static ItemStack.Builder rewardItem(SkyBlockPlayer player, DojoBelt belt) {
        long points = DojoProgress.points(player);
        boolean claimed = DojoProgress.hasClaimed(player, belt);
        boolean reached = points >= belt.getRequiredPoints();
        DojoBelt previous = belt.previous();
        boolean inLine = previous == null || DojoProgress.hasClaimed(player, previous);

        Material pane;
        String nameColour;
        if (claimed) {
            pane = Material.LIME_STAINED_GLASS_PANE;
            nameColour = "<a>";
        } else if (inLine) {
            pane = Material.YELLOW_STAINED_GLASS_PANE;
            nameColour = "<e>";
        } else {
            pane = Material.RED_STAINED_GLASS_PANE;
            nameColour = "<c>";
        }

        int percent = (int) Math.min(100, points * 100 / belt.getRequiredPoints());
        int filled = percent * BAR_LENGTH / 100;
        StringBuilder lore = new StringBuilder(nameColour + belt.getDisplayName() + " Belt Rewards\n\n")
                .append("<7>Progress: <e>").append(percent).append("<6>%\n")
                .append("<a>").append("-".repeat(filled)).append("<f>").append("-".repeat(BAR_LENGTH - filled))
                .append(" <e>").append(DojoFormat.compact(Math.min(points, belt.getRequiredPoints())))
                .append("<6>/<e>").append(DojoFormat.compact(belt.getRequiredPoints())).append("\n\n")
                .append("<7>Rewards:\n")
                .append("  <8>+<3>").append(StringUtility.commaify(belt.getCombatXP())).append(" <7>Combat Experience\n")
                .append("  ").append(belt.coloured()).append(" Belt Upgrade\n")
                .append("  <8>+<b>").append(belt.getSkyBlockXP()).append(" SkyBlock XP\n\n");
        if (claimed) {
            lore.append("<a>Claimed!");
        } else if (!reached) {
            lore.append("<c>Earn ").append(StringUtility.commaify(belt.getRequiredPoints())).append(" Dojo Points to unlock!");
        } else if (!inLine) {
            lore.append("<c>Claim the ").append(previous.coloured()).append(" Belt <c>rewards first!");
        } else {
            lore.append("<e>Click to claim!");
        }
        return ItemStacks.item(pane, lore.toString());
    }
}
