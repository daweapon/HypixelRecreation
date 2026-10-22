package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.foraging.SweepDetails;
import dev.daweapon.type.skyblockgeneric.foraging.TreeGiftMilestones;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUISwoop extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Swoop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        layout.slot(4, ItemStacks.item(Material.LEATHER_HELMET, """
                <a>Swoop's Milestones
                <7>Complete your Foraging Training by
                <7>increasing your <2>Tree Milestones<7>!
                <7>Collect <2>Tree Gifts <7>to increase your
                <7>milestones."""));
        tree(layout, 11, TreeMilestoneView.FIG);
        tree(layout, 13, TreeMilestoneView.MANGROVE);
        tree(layout, 15, TreeMilestoneView.HELIX);
        layout.slot(28, ItemStacks.item(Material.STONE_AXE, """
                <a>Axe Throwing
                <7>All Foraging Axes can be <c>thrown
                <7>while on <2>Galatea<7>.

                <7>However, throwing your axe reduces
                <7>the amount of logs you obtain by
                <c>half<7>! So only throw when needed."""));
        layout.slot(30, ItemStacks.item(Material.SHEARS, """
                <6>Sweep, Toughness, and Style
                <2>∮ Sweep <7>increases the amount of
                <7>logs you can cut in a row with your
                <7>axes.

                <7>Tree <c>toughness <7>HEAVILY reduces
                <7>the effect of <2>∮ Sweep<7>.

                <7>Each tree has a different <5>style<7>!
                <7>Cutting logs of a tree in the wrong
                <7>order will also reduce the amount of
                <7>logs you obtain by <c>half<7>!"""));
        layout.slot(32, ItemStacks.item(Material.CHEST, """
                <2>Tree Gifts
                <7>As long as you participate in cutting
                <7>down a tree, you will obtain rewards
                <7>from a Tree Gift.

                <7>Participation Requirements:
                <8>■ <c>10% <7>to get a <2>Tree Gift<7>.
                <8>■ <e>20% <7>to get <e>50% <7>more rewards.
                <8>■ <a>33% <7>to get <a>double <7>rewards!

                <e>Click to view Tree Gift loot tables!"""), (_, c) -> c.push(new GUITreeGifts()));
        layout.slot(50, (_, c) -> sweepDetails((SkyBlockPlayer) c.player()), (_, c) -> {
            SweepDetails.toggle((SkyBlockPlayer) c.player());
            c.session(Object.class).refresh();
        });
        layout.slot(34, ItemStacks.item(Material.SWEET_BERRIES, """
                <d>Berries
                <7>There are bushes throughout
                <2>Galatea <7>that periodically grow
                <7>Berries. Simply <e>click <7>the bushes to
                <7>collect them.

                <7>And who knows, you might even wake
                <7>up some creatures inside of them!"""));
    }

    private static ItemStack.Builder sweepDetails(SkyBlockPlayer player) {
        boolean on = SweepDetails.enabled(player);
        return ItemStacks.item(on ? Material.LIME_DYE : Material.GRAY_DYE, String.join("\n",
                "<c>Sweep Details",
                "<7>You want to know exactly what is",
                "<7>happening when you cut a Tree?",
                "<7>With this chat message you will see",
                "<7>all the details you need to",
                "<7>understand if you are cutting trees",
                "<7>correctly!",
                "",
                "<7>Toggled: " + (on ? "<a>ON" : "<c>OFF"),
                "",
                "<e>Click to toggle!"));
    }

    private static void tree(ViewLayout<DefaultState> layout, int slot, TreeMilestoneView tree) {
        layout.slot(slot, (_, c) -> tree.summary((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUITreeMilestones(tree)));
    }

    public enum TreeMilestoneView {
        FIG("FIG", "Fig", Material.JUNGLE_LOG, "<b>Tangleburg", 10, "<f>Trunk →  Branches <7>(5 ⧉)"),
        MANGROVE("MANGROVE", "Mangrove", Material.MANGROVE_LOG, "<2>Moonglade Marsh", 50, "<f>Branches <7>(25 ⧉) <f>→ Trunk → Roots"),
        HELIX("HELIX", "Helix", Material.CRIMSON_STEM, "<6>Torrhus Canyon", 150, "<f>Beige Logs <7>(150 ⧉) <f>→ Red Logs");

        final String type;
        final String display;
        final Material icon;
        final String location;
        final int toughness;
        final String style;

        TreeMilestoneView(String type, String display, Material icon, String location, int toughness, String style) {
            this.type = type;
            this.display = display;
            this.icon = icon;
            this.location = location;
            this.toughness = toughness;
            this.style = style;
        }

        ItemStack.Builder summary(SkyBlockPlayer player) {
            int gifts = TreeGiftMilestones.count(player, type);
            int reached = TreeGiftMilestones.milestones(gifts);
            String progress = reached >= TreeGiftMilestones.TIER_GIFTS.length ? "<a>Maxed out!"
                    : "<7>Progress to Milestone " + dev.daweapon.commons.StringUtility.getAsRomanNumeral(reached + 1) + ": <e>" + gifts + "<6>/<e>" + TreeGiftMilestones.TIER_GIFTS[reached];
            return ItemStacks.item(icon, String.join("\n",
                    "<a>" + display + " Trees",
                    "<7>Location: " + location,
                    "<7>Toughness: <c>" + toughness + " ⧉",
                    "<7>Style: " + style,
                    "",
                    progress,
                    "",
                    "<e>Click to view your milestones!"));
        }
    }
}
