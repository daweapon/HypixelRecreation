package net.swofty.type.skyblockgeneric.trade;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One side of a trade. Offered items never leave the owner's inventory: the offer remembers each
 * inventory slot and the exact stack it held, and the swap only happens if every slot still holds
 * that stack. A disconnect, crash or closed menu therefore can't lose or duplicate anything.
 */
@Getter
public final class TradeOffer {
    private final SkyBlockPlayer player;
    private final Map<Integer, ItemStack> items = new LinkedHashMap<>();
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

    public boolean isOffered(int slot) {
        return items.containsKey(slot);
    }

    boolean stillHoldsOfferedItems() {
        return items.entrySet().stream()
                .allMatch(entry -> player.getInventory().getItemStack(entry.getKey()).equals(entry.getValue()));
    }

    void dropChangedItems() {
        items.entrySet().removeIf(entry -> !player.getInventory().getItemStack(entry.getKey()).equals(entry.getValue()));
    }
}
