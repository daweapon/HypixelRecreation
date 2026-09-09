package net.swofty.type.skyblockgeneric.gui.inventories.deletor;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// GUI for toggling which junk materials the personal deletor auto-destroys
public class GUIPersonalDeletor implements View<GUIPersonalDeletor.State> {
    public record State() {
    }

    private record Entry(int slot, Material material, DatapointToggles.Toggles.ToggleType toggle, String displayName) {
    }

    private static final Entry[] ENTRIES = new Entry[]{
            new Entry(10, Material.COBBLESTONE, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_COBBLESTONE, "Cobblestone"),
            new Entry(11, Material.DIRT, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_DIRT, "Dirt"),
            new Entry(12, Material.NETHERRACK, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_NETHERRACK, "Netherrack"),
            new Entry(13, Material.ROTTEN_FLESH, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_ROTTEN_FLESH, "Rotten Flesh"),
            new Entry(14, Material.GRAVEL, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_GRAVEL, "Gravel"),
            new Entry(15, Material.SAND, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_SAND, "Sand"),
            new Entry(16, Material.STRING, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_STRING, "String"),
            new Entry(19, Material.BONE, DatapointToggles.Toggles.ToggleType.PERSONAL_DELETOR_BONE, "Bone"),
    };

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, context) -> "Personal Deletor Settings", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext context) {
        Components.fill(layout);

        for (Entry entry : ENTRIES) {
            layout.slot(entry.slot(), (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                boolean enabled = p.getToggles().get(entry.toggle());
                String template = enabled
                        ? "<e>{}\n<a>Status: ENABLED\n\n<7>Click to toggle whether this\n<7>item is automatically deleted!"
                        : "<e>{}\n<c>Status: DISABLED\n\n<7>Click to toggle whether this\n<7>item is automatically deleted!";
                return ItemStacks.item(entry.material(), template, entry.displayName());
            }, (click, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                p.getToggles().inverse(entry.toggle());
                c.player().openView(new GUIPersonalDeletor(), new State());
            });
        }

        Components.close(layout, 22);
    }

    public static void open(SkyBlockPlayer player) {
        player.openView(new GUIPersonalDeletor(), new State());
    }
}