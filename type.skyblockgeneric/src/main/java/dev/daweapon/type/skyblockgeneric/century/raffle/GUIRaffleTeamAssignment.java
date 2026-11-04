package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.function.BiConsumer;

public class GUIRaffleTeamAssignment implements View<GUIRaffleTeamAssignment.State> {
    private static final int ASSIGN_SLOT = 13;

    private final BiConsumer<SkyBlockPlayer, CenturyTeam> onJoined;
    private final Runnable onAbandoned;

    public GUIRaffleTeamAssignment(BiConsumer<SkyBlockPlayer, CenturyTeam> onJoined, Runnable onAbandoned) {
        this.onJoined = onJoined;
        this.onAbandoned = onAbandoned;
    }

    public record State(boolean joining) {
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Raffle Team Assignment", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(ASSIGN_SLOT, (_, _) -> ItemStacks.item(Material.JUKEBOX, """
                        <a>Raffle Team Assignment
                        <7>There are 5 teams for the raffle:
                        <8>• <c><l>RED
                        <8>• <9><l>BLUE
                        <8>• <a><l>GREEN
                        <8>• <e><l>YELLOW
                        <8>• <d><l>PINK

                        <7>The team with the most tickets
                        <7>for the big raffle gets
                        <7>eternally memorialized.

                        <e>Click to get a random team!"""),
                (c, view) -> {
                    if (c.state().joining()) return;
                    view.session(State.class).setState(new State(true));
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    player.closeInventory();
                    CenturyRaffle.joinTeam(player).thenAccept(team -> CenturyRaffle.sync(() -> {
                        if (team != null && player.isOnline()) onJoined.accept(player, team);
                    }));
                });
    }

    @Override
    public void onClose(State state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (!state.joining()) onAbandoned.run();
    }
}
