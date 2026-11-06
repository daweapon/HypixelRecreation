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
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * A single reward chest, laid out as both captured runs showed it: a six-row menu titled with the chest's name
 * ("Wood"), black glass all round, the rewards across slots 11-15 (gaps in grey glass) each ending in "Click the chest
 * below to purchase these rewards!", Open Reward Chest in slot 31, Close (or Go Back from Croesus) in 49 and the
 * Kismet Feather reroll in 50.
 */
public class GUIDungeonRewardChest extends StatelessView {
    public static final int OPEN_SLOT = 31;
    public static final int CLOSE_SLOT = 49;
    public static final int REROLL_SLOT = 50;
    static final int FIRST_REWARD_SLOT = 11;
    static final int LAST_REWARD_SLOT = 15;

    private final DungeonChestRun snapshot;
    private final DungeonChestTier tier;

    public GUIDungeonRewardChest(DungeonChestRun run, DungeonChestTier tier) {
        this.snapshot = run;
        this.tier = tier;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(tier.displayName(), InventoryType.CHEST_6_ROW);
    }

    /** Which of slots 11-15 hold the rewards: 3 items in 11, 13, 15; 4 in 11, 12, 14, 15; 5 in all (captured). */
    public static int[] rewardSlots(int count) {
        return switch (Math.min(count, 5)) {
            case 0 -> new int[0];
            case 1 -> new int[]{13};
            case 2 -> new int[]{12, 14};
            case 3 -> new int[]{11, 13, 15};
            case 4 -> new int[]{11, 12, 14, 15};
            default -> new int[]{11, 12, 13, 14, 15};
        };
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, CLOSE_SLOT, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DungeonChestRun found = DungeonChestStorage.find(player, snapshot.id());
        DungeonChestRun run = found == null ? snapshot : found;
        DungeonRewardChest chest = run.chest(tier);
        if (chest == null) return;

        for (int slot = FIRST_REWARD_SLOT; slot <= LAST_REWARD_SLOT; slot++) {
            layout.slot(slot, (_, _) -> ItemStacks.filler(Material.GRAY_STAINED_GLASS_PANE));
        }
        List<DungeonReward> rewards = chest.rewards();
        int[] slots = rewardSlots(rewards.size());
        for (int i = 0; i < slots.length; i++) {
            DungeonReward reward = rewards.get(i);
            layout.slot(slots[i], (_, c) -> withPurchaseHint(reward.icon((SkyBlockPlayer) c.player())));
        }

        layout.slot(OPEN_SLOT, (_, _) -> ItemStacks.item(Material.CHEST, Text.of("<a>Open Reward Chest"), openLore(run, chest)),
                (_, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    if (DungeonChestService.open(clicker, run.id(), tier) == DungeonChestService.OpenResult.OPENED) {
                        clicker.closeInventory();
                    }
                });

        layout.slot(REROLL_SLOT, (_, c) -> ItemStacks.item(Material.FEATHER, Text.of("<a>Reroll Chest"), rerollLore(run, tier, DungeonChestService.hasFeather((SkyBlockPlayer) c.player()))),
                (_, c) -> {
                    SkyBlockPlayer clicker = (SkyBlockPlayer) c.player();
                    if (DungeonChestService.reroll(clicker, run.id(), tier) == DungeonChestService.RerollResult.REROLLED) {
                        DungeonChestRun updated = DungeonChestStorage.find(clicker, run.id());
                        if (updated != null) c.replace(new GUIDungeonRewardChest(updated, tier));
                    }
                });
    }

    static ItemStack.Builder withPurchaseHint(ItemStack.Builder icon) {
        List<Text> lore = new ArrayList<>(ItemStacks.loreText(icon));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click the chest below to purchase"));
        lore.add(Text.of("<e>these rewards!"));
        return ItemStacks.lore(icon, lore);
    }

    /** Contents, Cost, the bank note for a paid chest, and the status line (captured order). */
    public static List<Text> openLore(DungeonChestRun run, DungeonRewardChest chest) {
        List<Text> lore = DungeonChestIcons.contentsAndCost(run, chest);
        if (chest.cost() > 0) {
            lore.add(Text.of("<c>NOTE: Coins are withdrawn from your"));
            lore.add(Text.of("<c>bank if you don't have enough in"));
            lore.add(Text.of("<c>your purse."));
            lore.add(Text.empty());
        }
        lore.add(DungeonChestIcons.status(run, chest, "<e>Click to open!"));
        return lore;
    }

    static List<Text> rerollLore(DungeonChestRun run, DungeonChestTier tier, boolean hasFeather) {
        String status;
        if (run.rerolled()) status = "<c>You already rerolled a chest!";
        else if (!run.canReroll(tier)) status = "<c>You can't reroll this chest!";
        else if (!hasFeather) status = "<e>Bring a Kismet Feather";
        else status = "<e>Click to reroll!";
        return List.of(
                Text.of("<7>Consume a <9>Kismet Feather <7>to reroll"),
                Text.of("<7>the loot in this chest."),
                Text.empty(),
                Text.of("<7>You may only use a feather once"),
                Text.of("<7>per run/chest where applicable."),
                Text.empty(),
                Text.of(status));
    }

}
