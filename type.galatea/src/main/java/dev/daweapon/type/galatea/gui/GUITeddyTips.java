package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
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

public class GUITeddyTips extends StatelessView {
    private static final int[] GRID = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

    public record Tip(String key, String name, Material icon) {
    }

    public static final List<Tip> TIPS = List.of(
            new Tip("mossybit", "Mossybit", Material.SLIME_BALL),
            new Tip("mudworm", "Mudworm", Material.DIRT),
            new Tip("coralot", "Coralot", Material.AXOLOTL_BUCKET),
            new Tip("hideonleaf", "Hideonleaf", Material.SHULKER_SHELL),
            new Tip("lumisquid", "Lumisquid", Material.GLOW_INK_SAC),
            new Tip("joydive", "Joydive", Material.HEART_OF_THE_SEA),
            new Tip("cod", "Cod", Material.COD),
            new Tip("pandas", "Bambuleaf and Mochibear", Material.BAMBOO),
            new Tip("salmon", "Salmon", Material.SALMON),
            new Tip("invisibug", "Invisibug", Material.SPIDER_EYE),
            new Tip("shellwise", "Shellwise", Material.NAUTILUS_SHELL),
            new Tip("spike", "Spike", Material.PUFFERFISH));

    private final HypixelNPC teddy;

    public GUITeddyTips(HypixelNPC teddy) {
        this.teddy = teddy;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Teddy", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        for (int index = 0; index < TIPS.size(); index++) {
            Tip tip = TIPS.get(index);
            layout.slot(GRID[index], ItemStacks.item(tip.icon(), "<a>" + tip.name() + "\n<7>Ask the legendary hunter how to\n<7>catch this one.\n\n<e>Click to listen!"),
                    (_, c) -> {
                        c.player().closeInventory();
                        teddy.setDialogue(c.player(), "tip-" + tip.key());
                    });
        }
    }
}
