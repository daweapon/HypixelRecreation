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
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityHitmanService;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryLevel;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
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
        int onCooldown = Math.min(RabbitHitman.slotsOnCooldown(data, now, reduction), Math.max(0, purchased - stored));
        int ready = Math.max(0, purchased - stored - onCooldown);

        for (int slot = 0; slot < 54; slot++) {
            layout.slot(slot, (s, c) -> ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        layout.slot(4, (s, c) -> header((SkyBlockPlayer) c.player()));

        for (int i = 0; i < SLOTS.length; i++) {
            int index = i;
            int slot = SLOTS[i];
            if (i < stored) {
                layout.slot(slot, (s, c) -> eggItem(), (click, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (!HoppityHitmanService.open(player, index)) {
                        player.sendMessage("<c>Your Rabbit Hitman slots are all on cooldown!");
                    }
                    c.session(State.class).refresh();
                });
            } else if (i < stored + ready) {
                layout.slot(slot, (s, c) -> ItemStacks.item(Material.LIME_DYE, 1, Text.of("<a>Egg Slot"), List.of(
                        Text.of("<7>Once you miss your next egg, your"),
                        Text.of("<7>Rabbit Hitman will snipe it for you."),
                        Text.empty(),
                        Text.of("<a><l>READY"))));
            } else if (i < purchased) {
                long remaining = RabbitHitman.cooldownRemaining(data, now, reduction, i - stored - ready);
                layout.slot(slot, (s, c) -> ItemStacks.item(Material.RED_DYE, 1, Text.of("<c>Egg Slot"), List.of(
                        Text.of("<7>Once you miss your next egg, your"),
                        Text.of("<7>Rabbit Hitman will snipe it for you."),
                        Text.empty(),
                        Text.of("<c>On cooldown: {}", StringUtility.formatTimeLeft(remaining)))));
            } else {
                boolean next = i == purchased;
                layout.slot(slot, (s, c) -> lockedItem(index, next, RabbitHitman.canBuyNextSlot(data)), (click, c) -> {
                    if (!next) return;
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (!RabbitHitman.canBuyNextSlot(ChocolateFactoryHelper.getData(player))) {
                        player.sendMessage("<c>Requires Chocolate Factory {:roman}!", RabbitHitman.requiredFactoryLevel(index));
                    } else if (HoppityHitmanService.buyNextSlot(player)) {
                        player.sendMessage("<a>You purchased a Rabbit Hitman slot!");
                    } else {
                        player.sendMessage("<c>You cannot afford this slot!");
                    }
                    c.session(State.class).refresh();
                });
            }
        }

        layout.slot(50, (s, c) -> ItemStacks.item(Material.CHEST, 1, Text.of("<a>Claim All"), List.of(
                Text.of("<7>Open the eggs collected by the"),
                Text.of("<c>Rabbit Hitman<7>."),
                Text.empty(),
                Text.of("<e>Click to claim all!"))), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            while (RabbitHitman.storedEggs(ChocolateFactoryHelper.getData(player)) > 0 && HoppityHitmanService.open(player, 0)) {
                continue;
            }
            c.session(State.class).refresh();
        });

        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private ItemStack.Builder header(SkyBlockPlayer player) {
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        long now = System.currentTimeMillis();
        double reduction = RabbitShardBonuses.hitmanCooldownPercent(player);
        int onCooldown = RabbitHitman.slotsOnCooldown(data, now, reduction);
        String slotCooldown = onCooldown > 0 ? StringUtility.formatTimeLeft(RabbitHitman.cooldownRemaining(data, now, reduction, 0)) : "NOW";
        String allSlots = onCooldown > 0 ? StringUtility.formatTimeLeft(RabbitHitman.cooldownRemaining(data, now, reduction, onCooldown - 1)) : "NOW";
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Hire this private rabbit to hunt eggs"));
        lore.add(Text.of("<7>for you, they will collect eggs you"));
        lore.add(Text.of("<7>missed!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Available eggs: <a>{}", RabbitHitman.storedEggs(data)));
        lore.add(Text.of("<7>Purchased slots: <e>{}<7>/<a>{}", RabbitHitman.purchasedSlots(data), RabbitHitman.MAX_SLOTS));
        lore.add(Text.of("<7>Slot cooldown: <a>{}", slotCooldown));
        lore.add(Text.of("<7>All slots in: <a>{}", allSlots));
        return ItemStacks.item(Material.BOW, 1, Text.of("<c>Rabbit Hitman"), lore);
    }

    private ItemStack.Builder eggItem() {
        return ItemStacks.head(HoppityEggType.BREAKFAST.getTextureHash(), 1, Text.of("<e>Chocolate Egg"), List.of(
                Text.of("<7>Rabbit Hitman collected an egg you"),
                Text.of("<7>missed!"),
                Text.empty(),
                Text.of("<e>Click to open!")));
    }

    private ItemStack.Builder lockedItem(int index, boolean next, boolean canBuyNext) {
        int required = RabbitHitman.requiredFactoryLevel(index);
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Purchase more Egg Slots so the"),
                Text.of("<7>Hitman can store more eggs you miss!"),
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<6>{} Coins", StringUtility.commaify(RabbitHitman.slotPrice(index))),
                Text.empty()));
        if (next && canBuyNext) {
            lore.add(Text.of("<e>Click to purchase!"));
        } else if (required > 1 && index >= levelFloor(required)) {
            lore.add(Text.of("<c>Requires <c>Chocolate Factory {:roman}", required));
        } else {
            lore.add(Text.of("<c>Requires previous unlock!"));
        }
        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>Egg Slot"), lore);
    }

    private static int levelFloor(int level) {
        return level == 1 ? 0 : ChocolateFactoryLevel.values()[level - 2].getMaxHitmanSlots();
    }
}
