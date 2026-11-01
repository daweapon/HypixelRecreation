package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.gems.Gemstone;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

public class GUIChiselGemstoneGuide extends StatelessView {
    private static final int[] PERCENTS = {30, 40, 50, 60, 100};

    private enum Effect {
        AQUAMARINE(Gemstone.AQUAMARINE, 11, "<a>{}% <7>chance to gain an additional\n<7>Chisel charge upon starting the\n<6>Fossil Excavator<7>."),
        CITRINE(Gemstone.CITRINE, 20, "<a>{}% <7>chance to highlight a piece of\n<7>treasure upon starting the <6>Fossil\n<6>Excavator<7>."),
        ONYX(Gemstone.ONYX, 29, "<a>{}% <7>chance to spawn an additional\n<7>piece of treasure upon starting the\n<6>Fossil Excavator<7>."),
        PERIDOT(Gemstone.PERIDOT, 38, "<7>Gain <a>+{}% <f>Fossil Essence <7>when using the\n<6>Fossil Excavator<7>.");

        private final Gemstone gemstone;
        private final int firstSlot;
        private final String lore;

        Effect(Gemstone gemstone, int firstSlot, String lore) {
            this.gemstone = gemstone;
            this.firstSlot = firstSlot;
            this.lore = lore;
        }
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Chisel Gemstone Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (Effect effect : Effect.values()) {
            for (int tier = 0; tier < PERCENTS.length; tier++) {
                ItemStack.Builder icon = new SkyBlockItem(effect.gemstone.item.get(tier)).getItemStack().builder();
                layout.slot(effect.firstSlot + tier, ItemStacks.lore(icon, effect.lore, PERCENTS[tier]));
            }
        }
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }
}
