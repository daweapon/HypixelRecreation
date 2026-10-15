package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChest;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestStorage;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/** One run's two reward chests, at the end of the fight or from Vesuvius. Only one can be opened. */
public class GUIKuudraRun extends StatelessView {
    private static final int FREE_SLOT = 12;
    private static final int PAID_SLOT = 14;

    private final KuudraRun snapshot;

    public GUIKuudraRun(KuudraRun run) {
        this.snapshot = run;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Kuudra - " + snapshot.tier().getDisplayName(), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        KuudraRun run = KuudraChestStorage.find((SkyBlockPlayer) ctx.player(), snapshot.id());
        if (run == null) {
            layout.slot(13, ItemStacks.item(Material.BARRIER, Text.of("<c>Expired!"),
                    List.of(Text.of("<7>These chests were discarded after"), Text.of("<a>72 hours<7>."))));
            return;
        }
        chest(layout, run, KuudraChest.FREE, FREE_SLOT, Material.CHEST);
        chest(layout, run, KuudraChest.PAID, PAID_SLOT, Material.ENDER_CHEST);
    }

    private static void chest(ViewLayout<DefaultState> layout, KuudraRun run, KuudraChest chest, int slot, Material material) {
        layout.slot(slot, (_, _) -> {
                    List<Text> lore = KuudraChestLore.contentsAndCost(run, chest);
                    lore.add(KuudraChestLore.status(run, chest, "<e>Click to view!"));
                    return ItemStacks.item(material, Text.of(chest.getColour() + chest.getDisplayName()), lore);
                },
                (_, c) -> c.push(new GUIKuudraChest(run, chest)));
    }
}
