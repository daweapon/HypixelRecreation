package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestService;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestStorage;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonReward;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewardChest;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources.XPTaskLayout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

/**
 * A single reward chest ("Wood Chest"): its contents across the second row, the open button in the middle,
 * Go Back under it and the Kismet Feather reroll beside it.
 */
public class GUIDungeonRewardChest extends StatelessView {
    private static final int OPEN_SLOT = 22;
    private static final int BACK_SLOT = 31;
    private static final int REROLL_SLOT = 32;

    private final DungeonChestRun snapshot;
    private final DungeonChestTier tier;

    public GUIDungeonRewardChest(DungeonChestRun run, DungeonChestTier tier) {
        this.snapshot = run;
        this.tier = tier;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(tier.chestName(), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.backOrClose(layout, BACK_SLOT, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DungeonChestRun found = DungeonChestStorage.find(player, snapshot.id());
        DungeonChestRun run = found == null ? snapshot : found;
        DungeonRewardChest chest = run.chest(tier);
        if (chest == null) return;

        List<DungeonReward> rewards = chest.rewards();
        int[] slots = XPTaskLayout.slots(Math.min(rewards.size(), XPTaskLayout.capacity(1)), 1, 1);
        for (int i = 0; i < slots.length; i++) {
            DungeonReward reward = rewards.get(i);
            layout.slot(slots[i], (_, c) -> reward.icon((SkyBlockPlayer) c.player()));
        }

        layout.slot(OPEN_SLOT, (_, _) -> {
                    List<Text> lore = DungeonChestIcons.contentsAndCost(run, chest);
                    lore.add(DungeonChestIcons.status(run, chest, "<e>Click to open!"));
                    return DungeonChestIcons.chest(tier, Text.of("<a>Open Reward Chest"), lore);
                },
                (_, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    if (DungeonChestService.open(clicker, run.id(), tier) == DungeonChestService.OpenResult.OPENED) {
                        clicker.closeInventory();
                    }
                });

        if (!chest.opened()) {
            layout.slot(REROLL_SLOT, (_, c) -> ItemStacks.item(Material.FEATHER, Text.of("<9>Reroll Chest"), rerollLore(run, (SkyBlockPlayer) c.player())),
                    (_, c) -> {
                        SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                        if (DungeonChestService.reroll(clicker, run.id(), tier) == DungeonChestService.RerollResult.REROLLED) {
                            DungeonChestRun updated = DungeonChestStorage.find(clicker, run.id());
                            if (updated != null) c.replace(new GUIDungeonRewardChest(updated, tier));
                        }
                    });
        }
    }

    private List<Text> rerollLore(DungeonChestRun run, SkyBlockPlayer player) {
        String status;
        if (run.rerolled()) status = "<c>You already rerolled a chest!";
        else if (!run.canReroll(tier)) status = "<c>You can't reroll this chest!";
        else if (!DungeonChestService.hasFeather(player)) status = "<c>You don't have a Kismet Feather!";
        else status = "<e>Click to reroll!";
        return List.of(
                Text.of("<7>Use a <9>Kismet Feather <7>to reroll the"),
                Text.of("<7>contents of this chest, as if you"),
                Text.of("<7>finished another run with the same"),
                Text.of("<7>score."),
                Text.empty(),
                Text.of("<7>You can only reroll <a>once <7>per run."),
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<9>Kismet Feather"),
                Text.empty(),
                Text.of(status));
    }
}
