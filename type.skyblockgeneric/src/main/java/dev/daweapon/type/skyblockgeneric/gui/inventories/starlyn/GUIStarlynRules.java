package dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynBracket;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynResource;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynTree;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GUIStarlynRules extends StatelessView {
    private static final int[] BRACKET_SLOTS = {30, 31, 32, 33, 34};
    private static final int[] HIDDEN_SLOTS = {39, 40, 41};
    private final StarlynSister sister;

    public GUIStarlynRules(StarlynSister sister) {
        this.sister = sister;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Rules", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        StarlynConfig config = StarlynConfig.get(sister);
        StarlynConfig.Points points = config.points();

        layout.slot(10, ItemStacks.item(Material.IRON_AXE, "<a>Ways to earn points"));
        layout.slot(12, ItemStacks.item(Material.LIME_DYE, treeText(points)));
        layout.slot(13, ItemStacks.item(Material.LIME_DYE, huntingText(points)));
        layout.slot(14, ItemStacks.item(Material.LIME_DYE, fishingText(points)));
        layout.slot(15, ItemStacks.item(Material.LIME_DYE, "<a>Combat\n<8>Point Earning Method\n\n<b>Per Creature Killed <7>→ <9>"
                + points.combat() + " Points"));
        layout.slot(16, ItemStacks.item(Material.LIME_DYE, gatheringText(points)));
        layout.slot(28, ItemStacks.item(Material.CHEST, "<a>Bracket Rewards"));

        int main = 0;
        int hidden = 0;
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        for (StarlynConfig.BracketReward reward : config.brackets()) {
            StarlynBracket bracket = reward.bracket();
            if (bracket.isHidden()) {
                if (hidden < HIDDEN_SLOTS.length && StarlynContests.data(viewer, sister).reachedBracket(bracket)) {
                    layout.slot(HIDDEN_SLOTS[hidden++], ItemStacks.item(bracket.getPane(), bracketText(reward)));
                }
            } else if (main < BRACKET_SLOTS.length) {
                layout.slot(BRACKET_SLOTS[main++], ItemStacks.item(bracket.getPane(), bracketText(reward)));
            }
        }
    }

    private String bracketText(StarlynConfig.BracketReward reward) {
        StringBuilder text = new StringBuilder(reward.bracket().formatted() + " " + reward.bracket().getColor() + "Bracket\n");
        text.append("<8>").append(StringUtility.commaify(reward.points())).append(" Points Required\n\n");
        text.append(String.join("\n", StarlynGui.rewardLines(sister, reward)));
        return text.toString();
    }

    private String treeText(StarlynConfig.Points points) {
        StringBuilder text = new StringBuilder("<a>Cutting Trees\n<8>Point Earning Method\n\n");
        text.append("<b>Per Tree Gift <7>→ <9>").append(points.treeGift()).append(" Points");
        for (Map.Entry<StarlynTree, Integer> entry : points.logsPerHundred().entrySet()) {
            text.append("\n<b>Per 100 ").append(entry.getKey().getDisplayName()).append(" Logs <7>→ <9>").append(entry.getValue()).append(" Points");
        }
        return text.toString();
    }

    private String huntingText(StarlynConfig.Points points) {
        List<Integer> values = points.huntingByRarity().isEmpty() ? points.huntingByShard().values().stream().toList() : points.huntingByRarity().values().stream().toList();
        int low = Collections.min(values);
        int high = Math.max(Collections.max(values), points.huntingDefault());
        return "<a>Hunting Creatures\n<8>Point Earning Method\n\n<b>Per Creature Hunted <7>→ <9>" + low + " to " + high
                + "\n<9>Points <8>(per mob, not per Shard)";
    }

    private String fishingText(StarlynConfig.Points points) {
        StringBuilder text = new StringBuilder("<a>Fishing\n<8>Point Earning Method\n\n");
        text.append("<b>Per Sea Creature <7>→ <9>").append(points.seaCreature()).append(" Points\n");
        if (points.treasure() > 0) {
            text.append("<b>Per Treasure <7>→ <9>").append(points.treasure()).append(" Points\n");
        } else {
            points.treasureByQuality().forEach((quality, value) ->
                    text.append("<b>Per ").append(quality).append(" Treasure <7>→ <9>").append(value).append(" Points\n"));
        }
        text.append("<b>Per Catch <7>→ <9>").append(points.catchPoints()).append(" Points");
        return text.toString();
    }

    private String gatheringText(StarlynConfig.Points points) {
        StringBuilder text = new StringBuilder("<a>Gathering Resources\n<8>Point Earning Method\n");
        for (Map.Entry<StarlynResource, Integer> entry : points.gathering().entrySet()) {
            text.append("\n<b>Per ").append(StringUtility.toNormalCase(entry.getKey().name())).append(" <7>→ <9>").append(entry.getValue()).append(" Points");
        }
        return text.toString();
    }
}
