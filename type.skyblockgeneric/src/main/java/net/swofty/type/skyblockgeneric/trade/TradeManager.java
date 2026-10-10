package net.swofty.type.skyblockgeneric.trade;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.gui.inventories.trade.TradeView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trade requests and live trades on this server. A request is accepted by the other player
 * sending one back, the same way {@code /trade} and shift-right-click work on Hypixel.
 */
public final class TradeManager {
    private static final Map<RequestKey, Long> requests = new ConcurrentHashMap<>();
    private static final Map<UUID, TradeSession> sessions = new ConcurrentHashMap<>();

    private TradeManager() {}

    public static Optional<TradeSession> sessionOf(SkyBlockPlayer player) {
        return Optional.ofNullable(sessions.get(player.getUuid()));
    }

    public static List<TradeOffer.HeldItem> heldItems(SkyBlockPlayer player) {
        return sessionOf(player).map(session -> {
            synchronized (session) {
                return List.copyOf(session.offerOf(player).getItems());
            }
        }).orElse(List.<TradeOffer.HeldItem>of());
    }

    public static void request(SkyBlockPlayer sender, SkyBlockPlayer target) {
        if (sender == target) {
            sender.sendMessage("<c>You can't trade with yourself!");
            return;
        }
        Optional<String> refusal = TradeRules.refuseProfile(sender)
                .or(() -> TradeRules.refuseProfile(target)
                        .map(_ -> "<c>" + target.getUsername() + " <c>is on a profile that can't trade!"))
                .or(() -> TradeRules.refuseDistance(sender, target));
        if (refusal.isPresent()) {
            sender.sendMessage(refusal.get());
            return;
        }
        if (sessions.containsKey(sender.getUuid())) {
            sender.sendMessage("<c>You are already in a trade!");
            return;
        }
        if (sessions.containsKey(target.getUuid())) {
            sender.sendMessage("<c>" + target.getUsername() + " <c>is already in a trade!");
            return;
        }

        if (requests.remove(new RequestKey(target.getUuid(), sender.getUuid())) != null) {
            open(target, sender);
            return;
        }

        RequestKey key = new RequestKey(sender.getUuid(), target.getUuid());
        if (requests.containsKey(key)) {
            sender.sendMessage("<c>You have already sent a trade request to " + target.getUsername() + "<c>!");
            return;
        }
        long sentAt = System.currentTimeMillis();
        requests.put(key, sentAt);
        sender.sendMessage("<a>You have sent a trade request to " + target.getUsername() + "<a>.");
        target.sendMessage(Text.of("<a>{} <a>has sent you a trade request. <b>Click here <a>to accept!", sender.getUsername())
                .asComponent()
                .clickEvent(ClickEvent.runCommand("/trade " + sender.getUsername()))
                .hoverEvent(HoverEvent.showText(Text.of("<e>Click to trade with {}!", sender.getUsername()).asComponent())));

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!requests.remove(key, sentAt)) return;
            if (sender.isOnline()) sender.sendMessage("<c>Your trade request to " + target.getUsername() + " <c>has expired.");
            if (target.isOnline()) target.sendMessage("<c>The trade request from " + sender.getUsername() + " <c>has expired.");
        }).delay(TaskSchedule.duration(TradeRules.REQUEST_EXPIRY)).schedule();
    }

    private static void open(SkyBlockPlayer first, SkyBlockPlayer second) {
        TradeSession session = new TradeSession(first, second);
        sessions.put(first.getUuid(), session);
        sessions.put(second.getUuid(), session);
        openView(session.getFirst(), session);
        openView(session.getSecond(), session);
    }

    public static void openView(TradeOffer offer, TradeSession session) {
        ViewSession<TradeView.State> view = ViewNavigator.get(offer.getPlayer()).replace(new TradeView(session), new TradeView.State());
        offer.setView(view);
    }

    public static void onViewClosed(TradeSession session, SkyBlockPlayer player, ViewSession<?> closedView) {
        TradeOffer offer = session.offerOf(player);
        if (offer.isAwaitingCoinInput() || offer.getView() != closedView) return;
        cancel(session, player);
    }

    public static void cancel(TradeSession session, SkyBlockPlayer canceller) {
        if (!session.finish()) return;
        session.giveBackAllItems();
        forget(session);
        SkyBlockPlayer other = session.otherOffer(canceller).getPlayer();
        canceller.sendMessage("<c>You cancelled the trade!");
        other.sendMessage("<c>" + canceller.getUsername() + " <c>cancelled the trade!");
        closeView(session.getFirst());
        closeView(session.getSecond());
    }

    public static void handleDisconnect(SkyBlockPlayer player) {
        requests.keySet().removeIf(key -> key.sender().equals(player.getUuid()) || key.target().equals(player.getUuid()));
        sessionOf(player).ifPresent(session -> cancel(session, player));
    }

    public static void dealClicked(TradeSession session, SkyBlockPlayer player) {
        TradeSession.DealResult result = session.clickDeal(player);
        if (result == TradeSession.DealResult.WARNED) {
            player.sendMessage("<c>You are about to give items or coins away for nothing! Click again to confirm.");
        }
        if (result != TradeSession.DealResult.BOTH_ACCEPTED) return;
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> complete(session));
    }

    private static void complete(TradeSession session) {
        TradeOffer first = session.getFirst();
        TradeOffer second = session.getSecond();
        List<ItemStack> firstGave;
        List<ItemStack> secondGave;
        synchronized (session) {
            if (session.isFinished()) return;
            Optional<String> refusal = session.bothAccepted()
                    ? TradeExchange.validate(session)
                    : Optional.of("<c>The trade changed, so it was reset!");
            if (refusal.isPresent()) {
                session.abortCompletion(refusal.get());
                return;
            }
            if (!session.finish()) return;
            forget(session);
            firstGave = first.stacks();
            secondGave = second.stacks();
            TradeExchange.swap(session);
        }
        closeView(first);
        closeView(second);
        summarise(first.getPlayer(), second.getPlayer(), secondGave, second.getCoins(), firstGave, first.getCoins());
        summarise(second.getPlayer(), first.getPlayer(), firstGave, first.getCoins(), secondGave, second.getCoins());
    }

    private static void summarise(SkyBlockPlayer player, SkyBlockPlayer other,
                                  List<ItemStack> received, long coinsReceived,
                                  List<ItemStack> given, long coinsGiven) {
        player.sendMessage("<6>Trade completed with " + other.getUsername() + "<6>!");
        player.getAchievementHandler().completeAchievement("skyblock.businessman");
        if (coinsReceived > 0) player.sendMessage("<a><l>+ <6>" + StringUtility.commaify(coinsReceived) + " coins");
        received.forEach(stack -> player.sendMessage(Text.of("<a><l>+ <f>{}x ", stack.amount())
                .append(ItemStacks.nameText(stack)).asComponent()));
        if (coinsGiven > 0) player.sendMessage("<c><l>- <6>" + StringUtility.commaify(coinsGiven) + " coins");
        given.forEach(stack -> player.sendMessage(Text.of("<c><l>- <f>{}x ", stack.amount())
                .append(ItemStacks.nameText(stack)).asComponent()));
        player.playSuccessSound();
    }

    private static void forget(TradeSession session) {
        sessions.remove(session.getFirst().getPlayer().getUuid(), session);
        sessions.remove(session.getSecond().getPlayer().getUuid(), session);
    }

    private static void closeView(TradeOffer offer) {
        ViewSession<?> view = offer.getView();
        if (view != null && !view.isClosed()) view.close(ViewSession.CloseReason.SERVER_EXITED);
    }

    private record RequestKey(UUID sender, UUID target) {}
}
