package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Map;

public class GUIRaffleMilestones implements View<RaffleSnapshot> {
    private static final int FIRST_MILESTONE_SLOT = 10;
    private static final int MEMENTO_SLOT = 31;
    private static final int BACK_SLOT = 39;
    private static final int CLOSE_SLOT = 40;

    @Override
    public ViewConfiguration<RaffleSnapshot> configuration() {
        return new ViewConfiguration<>("Raffle Extras", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<RaffleSnapshot> layout, RaffleSnapshot state, ViewContext ctx) {
        Components.fill(layout);
        for (RaffleMilestone milestone : RaffleMilestone.values()) {
            layout.slot(FIRST_MILESTONE_SLOT + milestone.ordinal(), (_, _) -> milestone(state, milestone), (c, _) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                RaffleClaims.claimMilestone(player, state, milestone, () -> RaffleMenus.open(player, GUIRaffleMilestones::new));
            });
        }
        layout.slot(MEMENTO_SLOT, (_, _) -> memento(state), (c, _) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            RaffleClaims.claimMemento(player, state, () -> RaffleMenus.open(player, GUIRaffleMilestones::new));
        });
        layout.slot(BACK_SLOT, Components.BACK_BUTTON, (c, _) -> RaffleMenus.openBox((SkyBlockPlayer) c.player()));
        Components.close(layout, CLOSE_SLOT);
    }

    private static ItemStack.Builder milestone(RaffleSnapshot state, RaffleMilestone milestone) {
        long tickets = state.profile().tickets();
        boolean claimed = state.profile().claimedMilestone(milestone.slices());
        boolean reached = tickets >= milestone.tickets();
        int percent = (int) Math.min(100, tickets * 100 / milestone.tickets());
        Material material = claimed ? Material.LIME_STAINED_GLASS_PANE
                : reached ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String status = claimed ? "<a>Already claimed!" : reached ? "<e>Click to claim!" : "<c>Milestone not reached yet!";
        String reward = milestone.coins() > 0
                ? "<8>+<6>" + StringUtility.commaify(milestone.coins()) + " <7>Coins"
                : "<8>+" + milestone.rewardName();
        return ItemStacks.item(material, """
                <6>Milestone %s Reward

                <7>Progress: <a>%d%%
                <b>%s <7>/ <b>%s Tickets

                <6>Reward:
                %s
                <8>+<d>%d Random Slice%s of Century Cake

                <8>Claimable once per Minecraft
                <8>account.

                %s""".formatted(milestone.numeral(), percent, StringUtility.commaify(Math.min(tickets, milestone.tickets())),
                StringUtility.commaify(milestone.tickets()), reward, milestone.slices(), milestone.slices() == 1 ? "" : "s", status));
    }

    private static ItemStack.Builder memento(RaffleSnapshot state) {
        StringBuilder lore = new StringBuilder("""
                <6>Cake Slice of the Century
                <7>The team with the most tickets
                <7>at the end of the event will be
                <7>forever memorialized on a
                <7>memento item.

                <7>Team tickets:
                """);
        long total = state.bigOneTeams().values().stream().mapToLong(Long::longValue).sum();
        for (Map.Entry<CenturyTeam, Long> entry : state.bigOneTeams().entrySet()) {
            long percent = total == 0 ? 0 : entry.getValue() * 100 / total;
            lore.append(" <b>").append(StringUtility.commaify(entry.getValue())).append(" tickets ")
                    .append(entry.getKey().colour()).append('(').append(percent).append("%) <7>- ")
                    .append(entry.getKey().display()).append('\n');
        }
        CenturyTeam team = state.profile().team();
        lore.append("\n<7>Your team: ").append(team == null ? "<7>None" : team.display()).append("\n\n");
        lore.append("<8>Claimable once per Minecraft\n<8>account.\n\n");
        if (state.mementoClaimed()) lore.append("<a>Already claimed!");
        else if (state.active()) lore.append("<5>Unlocked after end of event!");
        else lore.append("<e>Click to claim!");
        return ItemStacks.item(Material.CAKE, lore.toString());
    }
}
