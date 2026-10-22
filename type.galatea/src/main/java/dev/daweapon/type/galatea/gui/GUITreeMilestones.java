package dev.daweapon.type.galatea.gui;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.foraging.TreeGiftMilestones;
import dev.daweapon.type.skyblockgeneric.foraging.TreeMilestoneRewards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUITreeMilestones extends StatelessView {
    private static final int BAR_SEGMENTS = 20;
    private final GUISwoop.TreeMilestoneView tree;

    public GUITreeMilestones(GUISwoop.TreeMilestoneView tree) {
        this.tree = tree;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(tree.display + " Tree Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(4, ItemStacks.item(tree.icon, """
                <a>{} Tree Milestones
                <7>Obtaining <2>Tree Gifts <7>from <a>{} Trees
                <7>will grant you extra rewards!""", tree.display, tree.display));
        for (int index = 0; index < TreeGiftMilestones.TIER_GIFTS.length; index++) {
            int tier = index + 1;
            layout.slot(19 + index, (_, c) -> milestone((SkyBlockPlayer) c.player(), tier));
        }
    }

    private ItemStack.Builder milestone(SkyBlockPlayer player, int tier) {
        int gifts = TreeGiftMilestones.count(player, tree.type);
        int required = TreeGiftMilestones.TIER_GIFTS[tier - 1];
        int reached = TreeGiftMilestones.milestones(gifts);
        Material pane = tier <= reached ? Material.LIME_STAINED_GLASS_PANE
                : tier == reached + 1 ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String color = tier <= reached ? "<a>" : tier == reached + 1 ? "<e>" : "<c>";
        String roman = StringUtility.getAsRomanNumeral(tier);
        String status = tier <= reached ? "<a>Unlocked!" : "<7>Progress to Milestone " + roman + ": <e>" + Math.min(gifts, required) + "<6>/<e>" + required
                + "\n" + bar(Math.min(gifts, required), required);
        return ItemStacks.item(pane, String.join("\n",
                color + tree.display + " Tree Milestone " + roman,
                "<7>Collect <a>" + StringUtility.commaify(required) + " <2>Tree Gifts <7>from <6>" + tree.display + " Trees",
                "<7>to unlock this milestone!",
                "",
                status,
                "",
                "<7>Rewards:",
                " <2>+" + TreeMilestoneRewards.sweepPerTier(tree.type) + " Sweep",
                " <8>+<3>" + StringUtility.commaify((int) TreeMilestoneRewards.foragingXp(tree.type, tier)) + " <7>Foraging Experience",
                " <8>+<b>" + TreeGiftMilestones.XP_PER_MILESTONE + " SkyBlock XP"));
    }

    private static String bar(int value, int total) {
        int filled = (int) Math.round(BAR_SEGMENTS * (double) value / total);
        return "<a>" + "▮".repeat(filled) + "<7>" + "▮".repeat(BAR_SEGMENTS - filled);
    }
}
