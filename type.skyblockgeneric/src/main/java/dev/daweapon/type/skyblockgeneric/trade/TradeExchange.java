package dev.daweapon.type.skyblockgeneric.trade;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.data.datapoints.DatapointDouble;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Optional;

/**
 * Validates and performs a trade in one step. It runs on the scheduler thread, between instance
 * ticks, so neither inventory can change while it works. Coins are changed without their usual
 * write-through and both profiles are then saved whole, so each profile's coins and inventory
 * reach storage together.
 */
final class TradeExchange {
    private static final int MENU_SLOT = 8;
    private static final int MAIN_INVENTORY_SIZE = 36;

    private TradeExchange() {}

    static Optional<String> validate(TradeSession session) {
        Optional<String> refusal = validateSide(session.getFirst(), session.getSecond());
        return refusal.isPresent() ? refusal : validateSide(session.getSecond(), session.getFirst());
    }

    private static Optional<String> validateSide(TradeOffer offer, TradeOffer other) {
        SkyBlockPlayer player = offer.getPlayer();
        String name = player.getUsername();
        if (!player.isOnline() || player.getSkyblockDataHandler() == null) {
            return Optional.of("<c>The trade could not be completed!");
        }
        Optional<String> refusal = TradeRules.refuseProfile(player)
                .or(() -> TradeRules.refuseDistance(player, other.getPlayer()));
        if (refusal.isPresent()) return refusal;
        for (ItemStack stack : offer.stacks()) {
            refusal = TradeRules.refuseItem(stack, player, other.getPlayer());
            if (refusal.isPresent()) return refusal;
        }
        if (offer.getCoins() > player.getCoins()) {
            return Optional.of("<c>" + name + " <c>no longer has enough coins for this trade!");
        }
        if (offer.getCoins() > TradeAllowance.remaining(player)) {
            return Optional.of("<c>" + name + " <c>has reached their daily trading limit!");
        }
        if (freeSlots(player) < other.getItems().size()) {
            return Optional.of("<c>" + name + " <c>doesn't have enough inventory space for this trade!");
        }
        return Optional.empty();
    }

    static void swap(TradeSession session) {
        TradeOffer first = session.getFirst();
        TradeOffer second = session.getSecond();
        List<ItemStack> toSecond = first.stacks();
        List<ItemStack> toFirst = second.stacks();
        first.getItems().clear();
        second.getItems().clear();

        deliver(first.getPlayer(), toFirst);
        deliver(second.getPlayer(), toSecond);

        setCoinsWithoutWrite(first.getPlayer(), first.getPlayer().getCoins() - first.getCoins() + second.getCoins());
        setCoinsWithoutWrite(second.getPlayer(), second.getPlayer().getCoins() - second.getCoins() + first.getCoins());
        TradeAllowance.recordWithoutWrite(first.getPlayer(), first.getCoins());
        TradeAllowance.recordWithoutWrite(second.getPlayer(), second.getCoins());

        persist(first.getPlayer(), second.getPlayer());
    }

    private static void deliver(SkyBlockPlayer receiver, List<ItemStack> stacks) {
        int slot = 0;
        for (ItemStack stack : stacks) {
            while (slot == MENU_SLOT || !receiver.getInventory().getItemStack(slot).isAir()) slot++;
            receiver.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(receiver, stack, true).build());
        }
    }

    private static int freeSlots(SkyBlockPlayer player) {
        int free = 0;
        for (int slot = 0; slot < MAIN_INVENTORY_SIZE; slot++) {
            if (slot != MENU_SLOT && player.getInventory().getItemStack(slot).isAir()) free++;
        }
        return free;
    }

    private static void setCoinsWithoutWrite(SkyBlockPlayer player, double coins) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class)
                .setFrom(new DatapointDouble(SkyBlockDataHandler.Data.COINS.getKey(), coins));
    }

    private static void persist(SkyBlockPlayer first, SkyBlockPlayer second) {
        ServerType type = HypixelConst.getTypeLoader().getType();
        Thread.startVirtualThread(() -> {
            PlayerDataService.saveAll(type, first);
            PlayerDataService.saveAll(type, second);
        });
    }
}
