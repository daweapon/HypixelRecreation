package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUITreeGifts extends StatelessView {
    private static final String HEADER = String.join("\n",
            "<2>Tree Gifts",
            "<7>As long as you participate in cutting",
            "<7>down a tree, you will obtain rewards",
            "<7>from a Tree Gift.",
            "",
            "<7>Participation Requirements:",
            "<8>■ <c>10% <7>to get a <2>Tree Gift<7>.",
            "<8>■ <e>20% <7>to get <e>50% <7>more rewards.",
            "<8>■ <a>33% <7>to get <a>double <7>rewards!");

    private static final List<String> FIG = List.of(
            "<7>Rewards will scale based on the size",
            "<7>of the Tree.",
            "",
            "<f>Guaranteed Loot",
            "<8>■ <2>Forest Essence<8> x2",
            "<8>■ <3>Foraging Experience <8>x100",
            "<8>■ <a>HOTF Experience <8>x10",
            "<8>■ <a>Tender Wood <8>x0-1",
            "<8>■ <3>Forest Whispers <8>x20",
            "<8>■ <3>Forest Whispers <8>x40",
            "",
            "<9>Rare Loot",
            "<8>■ <f>Phanpyre <8>(<a>11.8%<8>)",
            "<8>■ <a>Stretching Sticks <8>(<a>10%<8>)",
            "<8>■ <f>Phanflare <8>(<a>7.1%<8>)",
            "",
            "<6>Legendary Loot",
            "<8>■ <9>Dreadwing <8>(<a>1.2%<8>)",
            "<8>■ <f>Common Sweep Booster <8>(<a>0.5%<8>)",
            "<8>■ <f>Common Foraging Wisdom Booster <8>(<a>0.3%<8>)",
            "<8>■ <f>Enchanted Book (<d><l>First Impression I<f>) <8>(<a>0.25%<8>)",
            "<8>■ <f>Enchanted Book (<d><l>Missile I<f>) <8>(<a>0.15%<8>)",
            "<8>■ <9>Hummingbird Shard <8>(<a>0.15%<8>)",
            "<8>■ <6>Chameleon Shard <8>(<a>0.08%<8>)",
            "<8>■ <c>Tree the Fish <8>(<a>0.05%<8>)",
            "<8>■ <f>◆ Fading White Rune I <8>(<a>0.02%<8>)");

    private static final List<String> MANGROVE = List.of(
            "<7>Rewards will scale based on the size",
            "<7>of the Tree.",
            "",
            "<f>Guaranteed Loot",
            "<8>■ <2>Forest Essence<8> x3",
            "<8>■ <3>Foraging Experience <8>x500",
            "<8>■ <a>HOTF Experience <8>x20",
            "<8>■ <a>Vinesap <8>x0-1",
            "<8>■ <3>Forest Whispers <8>x20",
            "<8>■ <3>Forest Whispers <8>x40",
            "",
            "<9>Rare Loot",
            "<8>■ <f>Phanpyre <8>(<a>11.8%<8>)",
            "<8>■ <f>Phanflare <8>(<a>7.1%<8>)",
            "<8>■ <a>Deep Root <8>(<a>4%<8>)",
            "",
            "<6>Legendary Loot",
            "<8>■ <9>Dreadwing <8>(<a>1.2%<8>)",
            "<8>■ <f>Common Sweep Booster <8>(<a>1%<8>)",
            "<8>■ <f>Common Foraging Wisdom Booster <8>(<a>0.5%<8>)",
            "<8>■ <f>Enchanted Book (<d><l>First Impression I<f>) <8>(<a>0.4%<8>)",
            "<8>■ <9>Hummingbird Shard <8>(<a>0.25%<8>)",
            "<8>■ <f>Enchanted Book (<d><l>Missile I<f>) <8>(<a>0.2%<8>)",
            "<8>■ <6>Chameleon Shard <8>(<a>0.08%<8>)",
            "<8>■ <c>Tree the Fish <8>(<a>0.05%<8>)",
            "<8>■ <a>◆ Fading Green Rune I <8>(<a>0.02%<8>)");

    private static final List<String> HELIX = List.of(
            "<7>Rewards will scale based on the size",
            "<7>of the Tree.",
            "",
            "<f>Guaranteed Loot",
            "<8>■ <2>Forest Essence<8> x4",
            "<8>■ <3>Foraging Experience <8>x1,500",
            "<8>■ <a>HOTF Experience <8>x30",
            "<8>■ <6>Desert Whispers <8>x75",
            "<8>■ <6>Desert Whispers <8>x150",
            "",
            "<9>Rare Loot",
            "<8>■ <f>Firefox <8>(<a>15%<8>)",
            "<8>■ <f>Groundhog <8>(<a>15%<8>)",
            "<8>■ <9>Drybark <8>(<a>5%<8>)",
            "<8>■ <9>Puck <8>(<a>5%<8>)",
            "",
            "<6>Legendary Loot",
            "<8>■ <6>Grizzly Bear <8>(<a>1%<8>)",
            "<8>■ <9>Hummingbird Shard <8>(<a>0.3%<8>)",
            "<8>■ <f>Enchanted Book (Karma I) <8>(<a>0.2%<8>)",
            "<8>■ <6>Chameleon Shard <8>(<a>0.1%<8>)",
            "<8>■ <5>Foraging Exp Boost <8>(<a>0.025%<8>)",
            "<8>■ <6>Forest's Favor <8>(<a>0.02%<8>)",
            "<8>■ <9>◆ Fading Blue Rune I <8>(<a>0.02%<8>)");

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Tree Gifts", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        layout.slot(4, ItemStacks.item(Material.CHEST, HEADER));
        layout.slot(20, ItemStacks.item(Material.JUNGLE_LOG, tree("<a>Fig Trees", FIG)));
        layout.slot(22, ItemStacks.item(Material.MANGROVE_LOG, tree("<a>Mangrove Trees", MANGROVE)));
        layout.slot(24, ItemStacks.item(Material.CRIMSON_STEM, tree("<a>Helix Trees", HELIX)));
    }

    private static String tree(String title, List<String> lines) {
        return title + "\n" + String.join("\n", lines);
    }
}
