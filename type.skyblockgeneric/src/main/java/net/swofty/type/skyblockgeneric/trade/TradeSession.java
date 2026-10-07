package net.swofty.type.skyblockgeneric.trade;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

public final class TradeSession {
    @Getter private final TradeOffer first;
    @Getter private final TradeOffer second;
    private long lastChangeMillis = System.currentTimeMillis();
    @Getter private volatile boolean finished;
    private boolean completing;

    TradeSession(SkyBlockPlayer first, SkyBlockPlayer second) {
        this.first = new TradeOffer(first);
        this.second = new TradeOffer(second);
    }

    public TradeOffer offerOf(SkyBlockPlayer player) {
        return first.getPlayer() == player ? first : second;
    }

    public TradeOffer otherOffer(SkyBlockPlayer player) {
        return first.getPlayer() == player ? second : first;
    }

    public boolean involves(SkyBlockPlayer player) {
        return first.getPlayer() == player || second.getPlayer() == player;
    }

    public synchronized long cooldownRemainingMillis() {
        return Math.max(0, TradeRules.DEAL_COOLDOWN.toMillis() - (System.currentTimeMillis() - lastChangeMillis));
    }

    public synchronized Optional<String> offerItem(SkyBlockPlayer player, int slot) {
        if (locked()) return Optional.empty();
        TradeOffer offer = offerOf(player);
        if (offer.isOffered(slot)) return Optional.empty();
        if (offer.getItems().size() >= TradeRules.OFFER_SLOTS) return Optional.of("<c>There is no more room in this trade!");
        ItemStack stack = player.getInventory().getItemStack(slot);
        Optional<String> refusal = TradeRules.refuseItem(stack, player, otherOffer(player).getPlayer());
        if (refusal.isPresent()) return refusal;
        offer.getItems().put(slot, stack);
        changed();
        return Optional.empty();
    }

    public synchronized void withdrawItem(SkyBlockPlayer player, int slot) {
        if (locked()) return;
        if (offerOf(player).getItems().remove(slot) != null) changed();
    }

    public synchronized Optional<String> addCoins(SkyBlockPlayer player, long coins) {
        if (locked()) return Optional.of("<c>The trade is being completed!");
        TradeOffer offer = offerOf(player);
        long total = offer.getCoins() + coins;
        if (total > player.getCoins()) return Optional.of("<c>You don't have that many coins in your purse!");
        if (total > TradeAllowance.remaining(player)) {
            return Optional.of("<c>That would go over your daily trading limit of <6>"
                    + StringUtility.shortenNumber(TradeAllowance.dailyLimit(player)) + " coins<c>!");
        }
        offer.setCoins(total);
        changed();
        return Optional.empty();
    }

    public synchronized void withdrawCoins(SkyBlockPlayer player) {
        if (locked()) return;
        TradeOffer offer = offerOf(player);
        if (offer.getCoins() <= 0) return;
        offer.setCoins(0);
        changed();
    }

    public synchronized DealResult clickDeal(SkyBlockPlayer player) {
        if (locked()) return DealResult.IGNORED;
        TradeOffer offer = offerOf(player);
        if (offer.isAccepted()) return DealResult.IGNORED;
        if (cooldownRemainingMillis() > 0) return DealResult.COOLING_DOWN;
        if (otherOffer(player).isEmpty() && !offer.isEmpty() && !offer.isWarned()) {
            offer.setWarned(true);
            refreshViews();
            return DealResult.WARNED;
        }
        offer.setAccepted(true);
        if (first.isAccepted() && second.isAccepted()) {
            completing = true;
            return DealResult.BOTH_ACCEPTED;
        }
        refreshViews();
        return DealResult.ACCEPTED;
    }

    private boolean locked() {
        return finished || completing;
    }

    synchronized boolean bothAccepted() {
        return first.isAccepted() && second.isAccepted();
    }

    synchronized void abortCompletion(String reason) {
        completing = false;
        first.dropChangedItems();
        second.dropChangedItems();
        changed();
        first.getPlayer().sendMessage(reason);
        second.getPlayer().sendMessage(reason);
    }

    synchronized boolean finish() {
        if (finished) return false;
        finished = true;
        return true;
    }

    private void changed() {
        first.setAccepted(false);
        second.setAccepted(false);
        first.setWarned(false);
        second.setWarned(false);
        lastChangeMillis = System.currentTimeMillis();
        refreshViews();
    }

    void refreshViews() {
        refresh(first.getView());
        refresh(second.getView());
    }

    private static void refresh(ViewSession<?> view) {
        if (view != null && !view.isClosed()) view.refresh();
    }

    public enum DealResult {
        IGNORED, COOLING_DOWN, WARNED, ACCEPTED, BOTH_ACCEPTED
    }
}
