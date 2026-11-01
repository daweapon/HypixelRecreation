package dev.daweapon.type.crimsonisle.gui.town;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BiConsumer;

/**
 * The 4-digit keypad on the Dragontail brewery door. The wiki documents the code, not the menu, so
 * this layout (a phone-style pad) is an estimate.
 */
public class GUIKeypad extends StatelessView {
    public static final int LENGTH = 4;
    private static final int[] DIGIT_SLOTS = {31, 3, 4, 5, 12, 13, 14, 21, 22, 23};

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
        for (int digit = 0; digit <= 9; digit++) {
            String value = String.valueOf(digit);
            layout.slot(DIGIT_SLOTS[digit], (_, _) -> ItemStacks.item(Material.STONE_BUTTON, Math.max(1, Integer.parseInt(value)),
                    "<a>" + value), (_, c) -> {
                if (entered.length() >= LENGTH) return;
                String next = entered + value;
                if (next.length() < LENGTH) {
                    c.player().openView(new GUIKeypad(next, onSubmit));
                    return;
                }
                c.player().closeInventory();
                onSubmit.accept((SkyBlockPlayer) c.player(), next);
            });
        }
        layout.slot(39, ItemStacks.item(Material.RED_TERRACOTTA, "<c>Clear"),
                (_, c) -> c.player().openView(new GUIKeypad(onSubmit)));
    }
}
