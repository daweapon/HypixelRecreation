package dev.daweapon.type.skyblockgeneric.gui.inventories.compactor;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// GUI for toggling which raw materials the personal compactor auto-compacts
public class GUIPersonalCompactor implements View<GUIPersonalCompactor.State> {
    public record State() {
    }

    private record Entry(int slot, Material raw, DatapointToggles.Toggles.ToggleType toggle, String displayName) {
    }

    private static final Entry[] ENTRIES = new Entry[]{
            new Entry(10, Material.IRON_INGOT, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_IRON, "Iron"),
            new Entry(11, Material.GOLD_INGOT, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_GOLD, "Gold"),
            new Entry(12, Material.DIAMOND, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_DIAMOND, "Diamond"),
            new Entry(13, Material.EMERALD, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_EMERALD, "Emerald"),
            new Entry(14, Material.COAL, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_COAL, "Coal"),
            new Entry(15, Material.REDSTONE, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_REDSTONE, "Redstone"),
            new Entry(16, Material.LAPIS_LAZULI, DatapointToggles.Toggles.ToggleType.PERSONAL_COMPACTOR_LAPIS, "Lapis Lazuli"),
    };

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, context) -> "Personal Compactor Settings", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext context) {
        Components.fill(layout);

        for (Entry entry : ENTRIES) {
            layout.slot(entry.slot(), (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean enabled = p.getToggles().get(entry.toggle());
                String template = enabled
                        ? "<e>{}\n<a>Status: ENABLED\n\n<7>Automatically compacts every 9\n<7>into 1 block form!"
                        : "<e>{}\n<c>Status: DISABLED\n\n<7>Automatically compacts every 9\n<7>into 1 block form!";
                return ItemStacks.item(entry.raw(), template, entry.displayName());
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                p.getToggles().inverse(entry.toggle());
                c.player().openView(new GUIPersonalCompactor(), new State());
            });
        }

        Components.close(layout, 22);
    }

    public static void open(SkyBlockPlayer player) {
        player.openView(new GUIPersonalCompactor(), new State());
    }
}