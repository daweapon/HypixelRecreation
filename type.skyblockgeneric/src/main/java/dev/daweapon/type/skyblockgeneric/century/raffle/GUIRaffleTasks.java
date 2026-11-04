package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIRaffleTasks implements View<RaffleSnapshot> {
    private static final int[] ROW_STARTS = {10, 19, 28};
    private static final int INFO_SLOT = 4;
    private static final int BACK_SLOT = 48;
    private static final int CLOSE_SLOT = 49;

    @Override
    public ViewConfiguration<RaffleSnapshot> configuration() {
        return new ViewConfiguration<>("Raffle Tasks", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<RaffleSnapshot> layout, RaffleSnapshot state, ViewContext ctx) {
        Components.fill(layout);
        long now = System.currentTimeMillis();
        if (!state.active()) {
            layout.slot(INFO_SLOT, ItemStacks.item(Material.BARRIER, """
                    <c>No Raffle Tasks
                    <7>The Raffle of the Century is over."""));
        } else {
            int period = RaffleClock.taskPeriod(state.window(), now);
            long refresh = Math.min(state.window().start() + (period + 1L) * RaffleClock.TASK_PERIOD_MILLIS, state.window().end());
            List<RaffleTask> tasks = RaffleTasks.forPeriod(state.window().id(), period);
            long done = tasks.stream().filter(task -> state.profile().taskDone(period, task)).count();
            layout.slot(INFO_SLOT, ItemStacks.item(Material.CLOCK, """
                    <a>Raffle Tasks
                    <7>Completed: <a>%d<7>/<a>%d
                    <7>New tasks in: <e>%s

                    <7>Each task gives <b>+1 Raffle Ticket
                    <7>and a random <d>slice of cake<7>.""".formatted(done, tasks.size(), RaffleMenus.countdown(refresh - now))));
            for (int i = 0; i < tasks.size(); i++) {
                RaffleTask task = tasks.get(i);
                int slot = ROW_STARTS[i / RaffleTasks.PER_DIFFICULTY] + i % RaffleTasks.PER_DIFFICULTY;
                boolean complete = state.profile().taskDone(period, task);
                layout.slot(slot, ItemStacks.item(complete ? Material.LIME_DYE : Material.GRAY_DYE, """
                        %s
                        <7>%s

                        <7>Difficulty: %s
                        %s""".formatted(complete ? "<a>Task Complete" : "<e>Raffle Task", task.description(),
                        task.difficulty().display(), complete ? "<a>✔ Completed!" : "<c>✖ Not completed yet!")));
            }
        }
        layout.slot(BACK_SLOT, Components.BACK_BUTTON, (c, _) -> RaffleMenus.openBox((SkyBlockPlayer) c.player()));
        Components.close(layout, CLOSE_SLOT);
    }
}
