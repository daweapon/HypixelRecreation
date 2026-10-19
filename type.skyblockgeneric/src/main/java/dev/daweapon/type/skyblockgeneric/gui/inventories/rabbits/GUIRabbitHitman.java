package dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.hoppity.EggLocation;
import dev.daweapon.type.skyblockgeneric.hoppity.EggLocationCatalogue;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityHitmanService;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitHitman;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitShardBonuses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUIRabbitHitman implements StatefulView<GUIRabbitHitman.State> {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final String HITMAN_TEXTURE = "b79e7f3341b672d9de6564cbaca052a6a723ea466a2e66af35ba1ba855f0d692";

    public record State() {
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Rabbit Hitman", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(viewer);
        long now = System.currentTimeMillis();
        double reduction = RabbitShardBonuses.hitmanCooldownPercent(viewer);
        int purchased = RabbitHitman.purchasedSlots(data);
        int stored = RabbitHitman.storedEggs(data);
        int onCooldown = RabbitHitman.slotsOnCooldown(data, now, reduction);

        for (int slot = 0; slot < 54; slot++) {
            layout.slot(slot, (s, c) -> ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        layout.slot(4, (s, c) -> header((SkyBlockPlayer) c.player()));

        for (int i = 0; i < SLOTS.length; i++) {
            int index = i;
            int slot = SLOTS[i];
            if (i < stored) {
                layout.slot(slot, (s, c) -> eggItem(data, index), (click, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (!HoppityHitmanService.open(player, index)) {
                        player.sendMessage("<c>Your Rabbit Hitman slots are all on cooldown!");
                    }
                    c.session(State.class).refresh();
                });
            } else if (i < stored + onCooldown && i < purchased) {
                long remaining = RabbitHitman.cooldownRemaining(data, now, reduction, i - stored);
                layout.slot(slot, (s, c) -> ItemStacks.item(Material.RED_STAINED_GLASS_PANE, Text.of("<c>Slot on cooldown"), List.of(
                        Text.of("<7>Ready in <e>{}", StringUtility.formatTimeLeft(remaining)))));
            } else if (i < purchased) {
                layout.slot(slot, (s, c) -> ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, Text.of("<a>Empty slot"), List.of(
                        Text.of("<7>Missed eggs are collected here"),
                        Text.of("<7>automatically."))));
            } else if (i == purchased && RabbitHitman.canBuyNextSlot(data)) {
                layout.slot(slot, (s, c) -> buyItem(data, index, (SkyBlockPlayer) c.player()), (click, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (HoppityHitmanService.buyNextSlot(player)) {
                        player.sendMessage("<a>You purchased a Rabbit Hitman slot!");
                    } else {
                        player.sendMessage("<c>You cannot afford this slot!");
                    }
                    c.session(State.class).refresh();
                });
            } else {
                layout.slot(slot, (s, c) -> ItemStacks.item(Material.BARRIER, Text.of("<c>Locked slot"), List.of(
                        Text.of("<7>Requires <6>Chocolate Factory {}<7>.", RabbitHitman.requiredFactoryLevel(index)))));
            }
        }

        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private ItemStack.Builder header(SkyBlockPlayer player) {
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        long now = System.currentTimeMillis();
        double reduction = RabbitShardBonuses.hitmanCooldownPercent(player);
        int onCooldown = RabbitHitman.slotsOnCooldown(data, now, reduction);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>The Rabbit Hitman collects the"));
        lore.add(Text.of("<c>Hitman Eggs <7>you miss during"));
        lore.add(Text.of("<d>Hoppity's Hunt<7>."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Slots: <a>{}<7>/<a>{}", RabbitHitman.purchasedSlots(data), RabbitHitman.MAX_SLOTS));
        lore.add(Text.of("<7>Available eggs: <a>{}", RabbitHitman.storedEggs(data)));
        lore.add(Text.of("<7>Slots on cooldown: <c>{}", onCooldown));
        if (onCooldown > 0) {
            lore.add(Text.of("<7>Next slot ready in: <e>{}", StringUtility.formatTimeLeft(
                    RabbitHitman.cooldownRemaining(data, now, reduction, 0))));
        }
        lore.add(Text.of("<7>Cooldown per slot: <e>{}", StringUtility.formatTimeLeft(RabbitHitman.cooldownPerSlot(reduction))));
        return ItemStacks.head(HITMAN_TEXTURE, 1, Text.of("<c>Rabbit Hitman"), lore);
    }

    private ItemStack.Builder eggItem(ChocolateFactoryData data, int index) {
        EggLocation location = RabbitHitman.locationOf(data.getHitmanEggs().get(index), EggLocationCatalogue.get());
        List<Text> lore = new ArrayList<>();
        if (location != null) {
            lore.add(Text.of("<7>Location: <a>{}", location.island().getName()));
            lore.add(Text.of("<7>{}", location.message()));
            lore.add(Text.empty());
        }
        lore.add(Text.of("<e>Click to open!"));
        return ItemStacks.item(Material.EGG, Text.of("<c>Hitman Egg"), lore);
    }

    private ItemStack.Builder buyItem(ChocolateFactoryData data, int index, SkyBlockPlayer player) {
        long price = RabbitHitman.slotPrice(index);
        boolean affordable = player.getCoins() >= price;
        return ItemStacks.item(Material.YELLOW_STAINED_GLASS_PANE, Text.of("<e>Purchase slot {}", index + 1), List.of(
                Text.of("<7>Cost: <6>{} Coins", StringUtility.commaify(price)),
                Text.empty(),
                affordable ? Text.of("<e>Click to purchase!") : Text.of("<c>Not enough coins!")));
    }
}
