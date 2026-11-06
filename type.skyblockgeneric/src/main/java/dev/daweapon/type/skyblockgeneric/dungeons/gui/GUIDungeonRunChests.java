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
 * One run's chests at Croesus ("Catacombs - Floor I"), as captured: Wood and Gold in slots 11 and 12, Diamond and
 * Emerald in 14 and 15 (Obsidian and Bedrock, never captured, in 16 and 10), Go Back and Close below.
 */
public class GUIDungeonRunChests extends StatelessView {
    private static final Map<DungeonChestTier, Integer> SLOTS = Map.of(
            DungeonChestTier.WOOD, 11, DungeonChestTier.GOLD, 12, DungeonChestTier.DIAMOND, 14,
            DungeonChestTier.EMERALD, 15, DungeonChestTier.OBSIDIAN, 16, DungeonChestTier.BEDROCK, 10);

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
                        return DungeonChestIcons.listIcon(chest.tier(), GUIDungeonRewardChest.openLore(run, chest));
                    },
                    (_, c) -> c.push(new GUIDungeonRewardChest(run, chest.tier())));
        }
    }
}
