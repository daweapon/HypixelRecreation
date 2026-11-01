package dev.daweapon.type.skyblockgeneric.gui.inventories.hotm;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crystalhollows.BundleReward;
import dev.daweapon.type.skyblockgeneric.crystalhollows.BundleTable;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusRngMeter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUINucleusRngMeter extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int BAR_LENGTH = 20;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crystal Nucleus RNG Meter", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(51, ItemStacks.item(Material.REDSTONE_BLOCK, Text.of("<d>Reset RNG Drop"), List.of(
                Text.of("<7>Resets the selected drop for this"),
                Text.of("<d>RNG Meter<7>. You still earn progress"),
                Text.of("<7>without a drop selected."),
                Text.empty(),
                Text.of("<e>Click to reset!"))), (_, c) -> {
            NucleusRngMeter.select((SkyBlockPlayer) c.player(), null);
            c.session(DefaultState.class).refresh();
        });
        List<BundleReward> rewards = BundleTable.meterRewards().stream()
                .sorted((a, b) -> Long.compare(a.meterXp(), b.meterXp())).toList();
        for (int i = 0; i < rewards.size() && i < SLOTS.length; i++) {
            BundleReward reward = rewards.get(i);
            layout.slot(SLOTS[i], (_, c) -> rewardItem((SkyBlockPlayer) c.player(), reward),
                    (_, c) -> select((SkyBlockPlayer) c.player(), reward, c));
        }
        layout.slot(4, (_, c) -> summary((SkyBlockPlayer) c.player()));
    }

    private void select(SkyBlockPlayer player, BundleReward reward, ViewContext ctx) {
        BundleReward selected = NucleusRngMeter.selected(player);
        if (selected != null && selected.id().equals(reward.id())) {
            NucleusRngMeter.select(player, null);
            player.sendMessage(Text.of("<e>Deselected <f>{}<e>.", reward.name()));
        } else {
            NucleusRngMeter.select(player, reward);
            player.sendMessage(Text.of("<a>Selected <f>{} <a>as your Crystal Nucleus RNG Meter drop!", reward.name()));
        }
        ctx.session(DefaultState.class).refresh();
    }

    private ItemStack.Builder rewardItem(SkyBlockPlayer player, BundleReward reward) {
        BundleReward selected = NucleusRngMeter.selected(player);
        boolean isSelected = selected != null && selected.id().equals(reward.id());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Base chance per roll: <a>{}%", format(reward.weight() * 100D / BundleTable.totalWeight())));
        lore.add(Text.of("<7>Your chance per roll: <a>{}%", format(NucleusRngMeter.chancePercent(player, reward))));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Required: <9>{} Nucleus XP", String.format("%,d", reward.meterXp())));
        lore.add(Text.empty());
        if (isSelected) {
            double percent = NucleusRngMeter.percent(player, reward);
            lore.add(Text.of("<7>Progress: <d>{}%", format(percent)));
            lore.add(bar(percent));
            lore.add(Text.empty());
            lore.add(Text.of("<a>SELECTED"));
            lore.add(Text.of("<e>Click to deselect!"));
        } else {
            lore.add(Text.of("<e>Click to select!"));
        }
        ItemStack.Builder icon = reward.item() == null
                ? ItemStacks.item(Material.DIAMOND, Text.empty(), List.of())
                : PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(reward.item(), 1).getItemStackBuilder().build());
        ItemStacks.name(icon, Text.of("<f>{}", reward.name()));
        ItemStacks.lore(icon, lore);
        return isSelected ? ItemStacks.enchanted(icon) : icon;
    }

    private ItemStack.Builder summary(SkyBlockPlayer player) {
        List<Text> lore = new ArrayList<>();
        BundleReward selected = NucleusRngMeter.selected(player);
        lore.add(Text.of("<8>Crystal Nucleus"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Your <d>Crystal Nucleus RNG Meter <7>fills"));
        lore.add(Text.of("<7>with <9>{} Nucleus XP <7>every time you", String.format("%,d", NucleusRngMeter.XP_PER_RUN)));
        lore.add(Text.of("<7>complete the <d>Crystal Nucleus<7>!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Your selected <d>RNG Drop <7>will be"));
        lore.add(Text.of("<7>guaranteed to appear in a Crystal"));
        lore.add(Text.of("<7>Nucleus Bundle once your RNG Meter"));
        lore.add(Text.of("<7>is full!"));
        lore.add(Text.empty());
        if (selected == null) {
            lore.add(Text.of("<c>You don't have an RNG drop"));
            lore.add(Text.of("<c>selected. Choose one to start"));
            lore.add(Text.of("<c>progressing towards it!"));
        } else {
            lore.add(Text.of("<7>Selected Drop: <f>{}", selected.name()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Stored Nucleus XP: <d>{}", String.format("%,d", NucleusRngMeter.stored(player))));
        return ItemStacks.item(Material.HEART_OF_THE_SEA, Text.of("<d>RNG Meter"), lore);
    }

    private static String format(double percent) {
        return percent >= 1 ? String.format("%.2f", percent) : String.format("%.4f", percent);
    }

    private static Text bar(double percent) {
        int filled = (int) Math.round(percent / 100D * BAR_LENGTH);
        return Text.of("<d>{}<f>{} <d>{}%", "─".repeat(filled), "─".repeat(BAR_LENGTH - filled), format(percent));
    }
}
