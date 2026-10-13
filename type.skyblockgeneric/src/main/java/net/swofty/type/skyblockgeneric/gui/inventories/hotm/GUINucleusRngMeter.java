package net.swofty.type.skyblockgeneric.gui.inventories.hotm;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crystalhollows.BundleReward;
import net.swofty.type.skyblockgeneric.crystalhollows.BundleTable;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusRngMeter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
        Components.backOrClose(layout, 49, ctx);
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
        lore.add(Text.of("<7>Each Crystal Nucleus completion adds"));
        lore.add(Text.of("<9>{} Nucleus XP <7>to the meter. The selected", String.format("%,d", NucleusRngMeter.XP_PER_RUN)));
        lore.add(Text.of("<7>drop grows up to <a>3x <7>as likely per roll"));
        lore.add(Text.of("<7>and is guaranteed once the meter is full."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Stored XP: <9>{}", String.format("%,d", NucleusRngMeter.stored(player))));
        BundleReward selected = NucleusRngMeter.selected(player);
        lore.add(selected == null ? Text.of("<7>Selected Drop: <c>None") : Text.of("<7>Selected Drop: <f>{}", selected.name()));
        return ItemStacks.item(Material.HEART_OF_THE_SEA, Text.of("<d>Crystal Nucleus RNG Meter"), lore);
    }

    private static String format(double percent) {
        return percent >= 1 ? String.format("%.2f", percent) : String.format("%.4f", percent);
    }

    private static Text bar(double percent) {
        int filled = (int) Math.round(percent / 100D * BAR_LENGTH);
        return Text.of("<d>{}<f>{} <d>{}%", "─".repeat(filled), "─".repeat(BAR_LENGTH - filled), format(percent));
    }
}
