package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

final class ToyBoxGrid {
    static final int COLUMNS = 7;
    static final int ROWS = 4;
    static final int CAPACITY = COLUMNS * ROWS;
    private static final int FIRST_SLOT = 10;
    private static final int ROW_STRIDE = 9;

    private ToyBoxGrid() {}

    static int slot(int index) {
        if (index < 0 || index >= CAPACITY) throw new IndexOutOfBoundsException("Toy Box grid has " + CAPACITY + " cells, not " + index);
        return FIRST_SLOT + index / COLUMNS * ROW_STRIDE + index % COLUMNS;
    }

    static <S> int place(ViewLayout<S> layout, List<ToyBoxButton> buttons, int startIndex) {
        int index = startIndex;
        for (ToyBoxButton button : buttons) layout.slot(slot(index++), (s, c) -> render(button), (click, c) -> claim(button, (SkyBlockPlayer) c.player()));
        return index;
    }

    private static net.minestom.server.item.ItemStack.Builder render(ToyBoxButton button) {
        return GUICentauri.item(button.name(), button.material(), button.flavor(), " ", button.description(), " ", "<e>Click to claim!");
    }

    private static void claim(ToyBoxButton button, SkyBlockPlayer player) {
        if (!button.isAvailable()) {
            player.sendMessage("<c>This progression system is not available on this server type yet.");
            return;
        }
        button.action().accept(player);
        player.sendMessage("<a>Centauri granted your request!");
    }
}
