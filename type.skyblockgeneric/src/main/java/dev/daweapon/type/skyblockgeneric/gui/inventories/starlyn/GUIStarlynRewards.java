package dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynRewards;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIStarlynRewards extends StatelessView {
    private final StarlynSister sister;

    public GUIStarlynRewards(StarlynSister sister) {
        this.sister = sister;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(sister.getDisplayName() + "'s Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        List<StarlynProfile.Entry> entries = StarlynContests.data(viewer, sister).claimable().stream()
                .sorted(Comparator.comparingLong(StarlynProfile.Entry::getDay).reversed()).toList();
        for (int index = 0; index < entries.size() && index < StarlynGui.GRID.length; index++) {
            StarlynProfile.Entry entry = entries.get(index);
            layout.slot(StarlynGui.GRID[index], (_, c) -> entryItem((SkyBlockPlayer) c.player(), entry), (_, c) -> {
                StarlynContests.claim((SkyBlockPlayer) c.player(), sister, List.of(entry.getDay()));
                c.session(DefaultState.class).refresh();
            });
        }
        layout.slot(50, ItemStacks.item(Material.GOLD_BLOCK, """
                <6>Bulk Claim
                <7>Claim multiple contest rewards at
                <7>once.

                <e>Click to claim!"""), (_, c) -> {
            StarlynContests.claimAll((SkyBlockPlayer) c.player(), sister);
            c.session(DefaultState.class).refresh();
        });
    }

    private ItemStack.Builder entryItem(SkyBlockPlayer player, StarlynProfile.Entry entry) {
        StarlynConfig.BracketReward reward = StarlynConfig.get(sister).reward(entry.getBracket());
        List<String> lines = new ArrayList<>();
        lines.add("<a>" + StarlynContests.dateLabel(entry.getDay()));
        lines.add("<8>" + sister.contestName());
        lines.add("");
        lines.add("<7>Your score: <9>" + StringUtility.commaify(entry.score()) + " points!");
        lines.add("<7>You reached the " + entry.getBracket().formatted() + " " + entry.getBracket().getColor() + "Bracket<7>!");
        lines.add("");
        lines.addAll(StarlynGui.rewardLines(sister, reward));
        if (StarlynRewards.dailyBonusesLeft(player, sister) > 0) lines.add(StarlynRewards.dailyBonusLine(player, sister));
        lines.add("");
        lines.add("<e>Click to claim!");
        return ItemStacks.head(sister.getHeadTexture(), String.join("\n", lines));
    }
}
