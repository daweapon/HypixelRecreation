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
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestStorage;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewardChest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Map;

/**
 * One run's chests, from the end-of-run chests or from Croesus ("The Catacombs - Floor I"): Wood, Gold and
 * Diamond on the left of the second row, Emerald, Obsidian and Bedrock on the right, Go Back and Close below.
 */
public class GUIDungeonRunChests extends StatelessView {
    private static final Map<DungeonChestTier, Integer> SLOTS = Map.of(
            DungeonChestTier.WOOD, 10, DungeonChestTier.GOLD, 11, DungeonChestTier.DIAMOND, 12,
            DungeonChestTier.EMERALD, 14, DungeonChestTier.OBSIDIAN, 15, DungeonChestTier.BEDROCK, 16);

    private final DungeonChestRun snapshot;

    public GUIDungeonRunChests(DungeonChestRun run) {
        this.snapshot = run;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(snapshot.title(), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        DungeonChestRun run = DungeonChestStorage.find((SkyBlockPlayer) ctx.player(), snapshot.id());
        if (run == null) {
            layout.slot(13, ItemStacks.item(Material.BARRIER, Text.of("<c>Expired!"),
                    List.of(Text.of("<7>These chests were discarded after"), Text.of("<a>72 hours<7>."))));
            return;
        }
        for (DungeonRewardChest chest : run.chests()) {
            layout.slot(SLOTS.get(chest.tier()), (_, _) -> {
                        List<Text> lore = DungeonChestIcons.contentsAndCost(run, chest);
                        lore.add(DungeonChestIcons.status(run, chest, "<e>Click to open!"));
                        return DungeonChestIcons.chest(chest.tier(), Text.of(chest.tier().colour() + chest.tier().chestName()), lore);
                    },
                    (_, c) -> c.push(new GUIDungeonRewardChest(run, chest.tier())));
        }
    }
}
