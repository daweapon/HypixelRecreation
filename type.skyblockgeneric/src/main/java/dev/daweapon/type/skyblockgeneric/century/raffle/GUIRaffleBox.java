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

public class GUIRaffleBox implements View<RaffleSnapshot> {
    private static final int SPEED_SLOT = 11;
    private static final int DAILY_SLOT = 13;
    private static final int BIG_ONE_SLOT = 15;
    private static final int TASKS_SLOT = 29;
    private static final int HISTORY_SLOT = 31;
    private static final int TEAM_SLOT = 33;
    private static final int CLOSE_SLOT = 49;
    private static final int MILESTONES_SLOT = 50;

    @Override
    public ViewConfiguration<RaffleSnapshot> configuration() {
        return new ViewConfiguration<>("Incredible Raffle Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<RaffleSnapshot> layout, RaffleSnapshot state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(SPEED_SLOT, (s, _) -> raffle(s, RaffleType.SPEED, Material.GOLD_BLOCK,
                "All raffle tickets collected\n<7>during the two hours before the\n<7>draw are automatically entered!"),
                (c, _) -> rewards(c.player(), c.state(), RaffleType.SPEED));
        layout.slot(DAILY_SLOT, (s, _) -> raffle(s, RaffleType.DAILY, Material.DIAMOND_BLOCK,
                "All raffle tickets collected in\n<7>the last day before the draw are\n<7>automatically entered!"),
                (c, _) -> rewards(c.player(), c.state(), RaffleType.DAILY));
        layout.slot(BIG_ONE_SLOT, (s, _) -> raffle(s, RaffleType.BIG_ONE, Material.FIREWORK_ROCKET,
                "All raffle tickets ever earned\n<7>are entered! <o>Crazy rewards!"),
                (c, _) -> rewards(c.player(), c.state(), RaffleType.BIG_ONE));
        layout.slot(TASKS_SLOT, (s, _) -> ItemStacks.item(Material.WRITABLE_BOOK, """
                        <a>Raffle Tasks
                        <7>Every <a>2 hours <7>you get <a>21 <7>new tasks.
                        <7>Each one gives <b>+1 Raffle Ticket
                        <7>and a <d>slice of cake<7>!

                        <e>Click to view!"""),
                (c, _) -> open(c.player(), GUIRaffleTasks::new));
        layout.slot(HISTORY_SLOT, (s, _) -> ItemStacks.item(Material.CHEST, """
                        <6>All Raffles
                        <7>View all previous raffles in
                        <7>which you participated.

                        <7>Unclaimed: <a>%d

                        <e>Click to browse!""".formatted(s.history().stream().filter(RaffleSnapshot.Row::claimable).count())),
                (c, _) -> open(c.player(), GUIRaffleHistory::new));
        layout.slot(TEAM_SLOT, (s, _) -> team(s));
        layout.slot(MILESTONES_SLOT, (s, _) -> ItemStacks.item(Material.NAME_TAG, """
                        <6>Raffle Milestones & Bonus Rewards
                        <7>Claim limited-time rewards upon
                        <7>collecting a certain amount of
                        <7>raffle tickets.

                        <e>Click to open!"""),
                (c, _) -> open(c.player(), GUIRaffleMilestones::new));
        Components.close(layout, CLOSE_SLOT);
    }

    private static void open(net.minestom.server.entity.Player player, java.util.function.Supplier<View<RaffleSnapshot>> view) {
        RaffleMenus.open((SkyBlockPlayer) player, view);
    }

    private static void rewards(net.minestom.server.entity.Player player, RaffleSnapshot state, RaffleType type) {
        RaffleSnapshot.Current current = state.current().get(type);
        GUIRaffleRewards.open((SkyBlockPlayer) player, type, current == null ? type.displayName() : current.label());
    }

    private static ItemStack.Builder raffle(RaffleSnapshot state, RaffleType type, Material material, String description) {
        CenturyRaffleConfig config = CenturyRaffleConfig.get();
        RaffleSnapshot.Current current = state.current().get(type);
        StringBuilder lore = new StringBuilder();
        lore.append(type.colour()).append(current == null ? type.displayName() : current.label()).append('\n');
        lore.append("<8>").append(switch (type) {
            case SPEED -> "Drawn every 2 hours";
            case DAILY -> "Drawn once a day";
            case BIG_ONE -> "Drawn once, at the end";
        }).append("\n\n<7>").append(description).append("\n\n");
        lore.append(type.colour()).append("Prizes:\n");
        for (PrizeTier tier : PrizeTier.values()) {
            lore.append("<7> ").append(StringUtility.commaify(config.prizeCount(type, tier))).append("x ").append(tier.display()).append(" <r><7>rewards\n");
        }
        lore.append('\n');
        if (current == null) {
            lore.append("<c>This raffle is over!\n");
        } else {
            lore.append(type.colour()).append("You entered: <b>").append(StringUtility.commaify(current.entered())).append(" tickets!\n");
            lore.append(type.colour()).append("Total tickets (All Players): <b>").append(StringUtility.commaify(current.total())).append('\n');
            lore.append('\n').append("<7>Until draw: ").append(type.colour())
                    .append(RaffleMenus.countdown(current.drawAt() - System.currentTimeMillis())).append('\n');
        }
        lore.append("\n<e>Click to view rewards!");
        return ItemStacks.item(material, lore.toString());
    }

    private static ItemStack.Builder team(RaffleSnapshot state) {
        CenturyTeam team = state.profile().team();
        String name = team == null ? "<7>None" : team.display();
        return ItemStacks.item(Material.CAKE, """
                <d>Your Raffle Progress
                <7>Team: %s
                <7>Tickets earned: <b>%s

                <7>Feed a <d>slice of cake <7>to a player
                <7>on its matching team and you both
                <7>get <b>+1 Raffle Ticket <7>and a <6>reward<7>!""".formatted(name, StringUtility.commaify(state.profile().tickets())));
    }
}
