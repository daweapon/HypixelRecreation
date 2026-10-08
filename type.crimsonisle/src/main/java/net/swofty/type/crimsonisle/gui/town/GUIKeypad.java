package net.swofty.type.crimsonisle.gui.town;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BiConsumer;

/**
 * The 4-digit keypad on the Dragontail brewery door. The wiki documents the code, not the menu, so
 * this layout (a phone-style pad) is an estimate.
 */
public class GUIKeypad extends StatelessView {
    public static final int LENGTH = 4;
    private static final int[] DIGIT_SLOTS = {40, 12, 13, 14, 21, 22, 23, 30, 31, 32};

    private final String entered;
    private final BiConsumer<SkyBlockPlayer, String> onSubmit;

    public GUIKeypad(BiConsumer<SkyBlockPlayer, String> onSubmit) {
        this("", onSubmit);
    }

    private GUIKeypad(String entered, BiConsumer<SkyBlockPlayer, String> onSubmit) {
        this.entered = entered;
        this.onSubmit = onSubmit;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Keypad", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        String shown = (entered + "____").substring(0, LENGTH);
        layout.slot(4, ItemStacks.item(Material.PAPER, "<a>Code: <e>{}\n<7>Enter the 4-digit code.", shown));
        for (int digit = 0; digit <= 9; digit++) {
            String value = String.valueOf(digit);
            layout.slot(DIGIT_SLOTS[digit], (_, _) -> ItemStacks.item(Material.STONE_BUTTON, Math.max(1, Integer.parseInt(value)),
                    "<e>" + value), (_, c) -> {
                if (entered.length() >= LENGTH) return;
                c.player().openView(new GUIKeypad(entered + value, onSubmit));
            });
        }
        layout.slot(39, ItemStacks.item(Material.RED_TERRACOTTA, "<c>Clear"),
                (_, c) -> c.player().openView(new GUIKeypad(onSubmit)));
        layout.slot(41, ItemStacks.item(Material.LIME_TERRACOTTA, "<a>Enter"), (_, c) -> {
            if (entered.length() < LENGTH) return;
            c.player().closeInventory();
            onSubmit.accept((SkyBlockPlayer) c.player(), entered);
        });
    }
}
