package dev.daweapon.type.skyblockgeneric.trade;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * One side of a trade. Offered items leave the owner's inventory and are held here, as on Hypixel,
 * until the swap delivers them or a withdrawal, cancel or disconnect returns them. Inventory saves
 * taken mid-trade write held items back in (see {@link TradeManager#heldItems}), so a crash can't
 * lose them.
 */
@Getter
public final class TradeOffer {
    private final SkyBlockPlayer player;
    private final List<HeldItem> items = new ArrayList<>();
    @Setter private long coins;
    @Setter private boolean accepted;
    @Setter private boolean warned;
    @Setter private boolean awaitingCoinInput;
    @Setter private ViewSession<?> view;

    TradeOffer(SkyBlockPlayer player) {
        this.player = player;
    }

    public boolean isEmpty() {
        return items.isEmpty() && coins <= 0;
    }

    public List<ItemStack> stacks() {
        return items.stream().map(HeldItem::stack).toList();
    }

    void hold(int homeSlot, ItemStack stack) {
        items.add(new HeldItem(homeSlot, stack));
        player.getInventory().setItemStack(homeSlot, ItemStack.AIR);
    }

    boolean giveBack(HeldItem held) {
        if (!items.remove(held)) return false;
        restore(held);
        return true;
    }

    void giveBackAll() {
        List<HeldItem> held = List.copyOf(items);
        items.clear();
        held.forEach(this::restore);
    }

    private void restore(HeldItem held) {
        if (player.getInventory().getItemStack(held.homeSlot()).isAir()) {
            player.getInventory().setItemStack(held.homeSlot(), held.stack());
        } else {
            player.addAndUpdateItem(new SkyBlockItem(held.stack()));
        }
    }

    public record HeldItem(int homeSlot, ItemStack stack) {
    }
}
