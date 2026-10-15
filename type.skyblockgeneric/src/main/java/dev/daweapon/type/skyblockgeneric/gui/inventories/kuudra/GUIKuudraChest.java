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
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestService;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraChestStorage;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraReward;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources.XPTaskLayout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/** A Free or Paid Chest: its rewards on show and the open button below them (slot 31). */
public class GUIKuudraChest extends StatelessView {
    private static final int OPEN_SLOT = 31;

    private final KuudraRun snapshot;
    private final KuudraChest chest;

    public GUIKuudraChest(KuudraRun run, KuudraChest chest) {
        this.snapshot = run;
        this.chest = chest;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(chest.getDisplayName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        KuudraRun run = KuudraChestStorage.find(player, snapshot.id());
        if (run == null) run = snapshot;
        KuudraRun shown = run;

        List<KuudraReward> rewards = shown.rewards(chest);
        int[] slots = XPTaskLayout.slots(rewards.size(), 1, 2);
        for (int i = 0; i < slots.length; i++) {
            KuudraReward reward = rewards.get(i);
            layout.slot(slots[i], (_, c) -> reward.icon((SkyBlockPlayer) c.player()));
        }

        layout.slot(OPEN_SLOT, (_, _) -> {
                    List<Text> lore = KuudraChestLore.contentsAndCost(shown, chest);
                    lore.add(KuudraChestLore.status(shown, chest, "<e>Click to open!"));
                    return ItemStacks.item(Material.CHEST, Text.of("<a>Open Reward Chest"), lore);
                },
                (_, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    if (KuudraChestService.open(clicker, shown.id(), chest) == KuudraChestService.OpenResult.OPENED) {
                        clicker.closeInventory();
                    }
                });
    }
}
