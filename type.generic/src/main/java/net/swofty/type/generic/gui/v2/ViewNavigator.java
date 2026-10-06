package net.swofty.type.generic.gui.v2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.tinylog.Logger;

@ToString
@RequiredArgsConstructor
public final class ViewNavigator {

    private static final Map<HypixelPlayer, ViewNavigator> NAVIGATORS = new ConcurrentHashMap<>();

    private final HypixelPlayer player;
    private final Deque<NavigationEntry<?>> stack = new ArrayDeque<>();

    @Getter
    private ViewSession<?> currentSession;

    public static ViewNavigator get(HypixelPlayer player) {
        return NAVIGATORS.computeIfAbsent(player, ViewNavigator::new);
    }

    public static Optional<ViewNavigator> find(HypixelPlayer player) {
        return Optional.ofNullable(NAVIGATORS.get(player));
    }

    public static void remove(HypixelPlayer player) {
        ViewNavigator nav = NAVIGATORS.remove(player);
        if (nav != null) {
            nav.clear();
        }
    }

    public <S> ViewSession<S> push(View<S> view, S state) {
        if (currentSession != null && !currentSession.isClosed()) {
            pushCurrentToStack();
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        return adopt(() -> ViewSession.open(view, player, state), view);
    }

    public <S> ViewSession<S> push(View<S> view) {
        S state = resolveInitialState(view);
        return push(view, state);
    }

    public <S> ViewSession<S> pushShared(View<S> view, String contextId, S initialState) {
        if (currentSession != null && !currentSession.isClosed()) {
            pushCurrentToStack();
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        SharedContext<S> ctx = SharedContext.getOrCreate(contextId, initialState);
        return adopt(() -> ViewSession.openShared(view, player, ctx), view);
    }

    public <S> ViewSession<S> joinShared(View<S> view, String contextId) {
        return SharedContext.<S>get(contextId).map(ctx -> {
            if (currentSession != null && !currentSession.isClosed()) {
                pushCurrentToStack();
                currentSession.close(ViewSession.CloseReason.REPLACED);
            }
            return adopt(() -> ViewSession.openShared(view, player, ctx), view);
        }).orElseThrow(() -> new IllegalArgumentException("Shared context not found: " + contextId));
    }

    public <S> ViewSession<S> pushShared(View<S> view, SharedContext<S> sharedContext) {
        if (currentSession != null && !currentSession.isClosed()) {
            pushCurrentToStack();
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        return adopt(() -> ViewSession.openShared(view, player, sharedContext), view);
    }

    public <S> ViewSession<S> replace(View<S> view, S state) {
        if (currentSession != null && !currentSession.isClosed()) {
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        return adopt(() -> ViewSession.open(view, player, state), view);
    }

    public <S> ViewSession<S> replace(View<S> view) {
        S state = resolveInitialState(view);
        return replace(view, state);
    }

    public boolean pop() {
        if (stack.isEmpty()) {
            if (currentSession != null && !currentSession.isClosed()) {
                currentSession.close(ViewSession.CloseReason.PLAYER_EXITED);
            }
            currentSession = null;
            return false;
        }

        if (currentSession != null && !currentSession.isClosed()) {
            currentSession.close(ViewSession.CloseReason.REPLACED);
        }

        NavigationEntry<?> entry = stack.pop();
        return adopt(() -> openEntry(entry), entry.view()) != null;
    }

    public void popTo(int levels) {
        for (int i = 0; i < levels - 1 && !stack.isEmpty(); i++) {
            stack.pop();
        }
        pop();
    }

    public void popToRoot() {
        while (stack.size() > 1) {
            stack.pop();
        }
        pop();
    }

    public void clear() {
        stack.clear();
        if (currentSession != null && !currentSession.isClosed()) {
            currentSession.close(ViewSession.CloseReason.SERVER_EXITED);
        }
        currentSession = null;
    }

    public boolean hasStack() {
        return !stack.isEmpty();
    }

    public int depth() {
        return stack.size();
    }

    @SuppressWarnings("unchecked")
    public <S> Optional<NavigationEntry<S>> peekPrevious() {
        return Optional.ofNullable((NavigationEntry<S>) stack.peek());
    }

    public HypixelPlayer player() {
        return player;
    }


    /**
     * Building a view runs arbitrary rendering code, and a failure used to leave the player
     * looking at an inventory this navigator no longer drives - clicks in it went uncancelled,
     * so its items could be dragged into the player's inventory. Anything that throws now tears
     * the whole stack down and shuts the window.
     */
    private <S> ViewSession<S> adopt(Supplier<ViewSession<S>> opener, View<?> view) {
        ViewSession<S> session;
        try {
            session = opener.get();
        } catch (RuntimeException e) {
            Logger.error(e, "Failed to open view " + view.getClass().getName() + " for " + player.getUsername());
            stack.clear();
            currentSession = null;
            player.closeInventory();
            player.sendMessage("<c>That menu could not be opened. Please report this.");
            return null;
        }
        this.currentSession = session;
        registerCloseHandler(session);
        return session;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void pushCurrentToStack() {
        if (currentSession == null) return;
        ViewSession<?> session = currentSession;
        stack.push(new NavigationEntry(session.view(), session.state()));
    }

    private <S> ViewSession<S> openEntry(NavigationEntry<S> entry) {
        return ViewSession.open(entry.view(), player, entry.state());
    }

    private void registerCloseHandler(ViewSession<?> session) {
        session.onClose(reason -> {
            if (reason == ViewSession.CloseReason.PLAYER_EXITED) {
                clear();
            }
        });
    }

    private <S> S resolveInitialState(View<S> view) {
        if (view instanceof StatefulView<S> stateful) {
            return stateful.initialState();
        }
        return null;
    }

    public record NavigationEntry<S>(View<S> view, S state) {
    }
}
