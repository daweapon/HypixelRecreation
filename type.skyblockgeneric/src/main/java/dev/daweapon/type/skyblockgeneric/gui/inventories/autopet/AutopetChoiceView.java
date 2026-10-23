package dev.daweapon.type.skyblockgeneric.gui.inventories.autopet;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

class AutopetChoiceView extends StatelessView {
    record Choice(String id, String label, Material icon, @Nullable String unavailable) {
    }

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final String title;
    private final List<Choice> choices;
    private final BiConsumer<SkyBlockPlayer, String> onPick;
    private final Runnable back;
    private final int page;

    AutopetChoiceView(String title, List<Choice> choices, BiConsumer<SkyBlockPlayer, String> onPick, Runnable back, int page) {
        this.title = title;
        this.choices = choices;
        this.onPick = onPick;
        this.back = back;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, "<a>Go Back"), (_, _) -> back.run());
        int start = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && start + i < choices.size(); i++) {
            Choice choice = choices.get(start + i);
            if (choice.unavailable() != null) {
                layout.slot(SLOTS[i], ItemStacks.item(Material.BARRIER, 1, "<c>" + choice.label() + "\n<7>Not available yet:\n<7>" + choice.unavailable()));
                continue;
            }
            layout.slot(SLOTS[i], ItemStacks.item(choice.icon(), 1, "<a>" + choice.label() + "\n\n<e>Click to select!"),
                    (_, c) -> onPick.accept((SkyBlockPlayer) c.player(), choice.id()));
        }
        if (page > 0) {
            layout.slot(45, ItemStacks.item(Material.ARROW, 1, "<a>Previous Page"),
                    (_, c) -> c.player().openView(new AutopetChoiceView(title, choices, onPick, back, page - 1)));
        }
        if (start + SLOTS.length < choices.size()) {
            layout.slot(53, ItemStacks.item(Material.ARROW, 1, "<a>Next Page"),
                    (_, c) -> c.player().openView(new AutopetChoiceView(title, choices, onPick, back, page + 1)));
        }
    }
}
