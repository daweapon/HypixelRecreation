package dev.daweapon.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGoal;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalProfile;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Chantelle's Carnival Goals, laid out from the wiki's Chantelle/UI subpage.
public class GUICarnivalGoals extends StatelessView {
    private static final int[] ROW_STARTS = {11, 20, 29};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Carnival Goals", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        CarnivalGoal[] goals = CarnivalGoal.values();
        for (int i = 0; i < goals.length; i++) {
            CarnivalGoal goal = goals[i];
            int slot = ROW_STARTS[goal.getGame().ordinal()] + i % 5;
            layout.slot(slot, (_, c) -> goalItem(Carnivals.profile((SkyBlockPlayer) c.player()), goal));
        }
        layout.slot(50, (_, _) -> ItemStacks.item(Material.BOOK, """
                <a>Carnival Stats
                <7>Track all your stats for all the
                <7>Carnival games!

                <e>Click to view!"""), (_, c) -> c.push(new GUICarnivalStats()));
    }

    private static ItemStack.Builder goalItem(CarnivalProfile profile, CarnivalGoal goal) {
        boolean complete = profile.getCompletedGoals().contains(goal);
        StringBuilder lore = new StringBuilder(goal.getGame().colored()).append("\n<7>")
                .append(goal.getDescription().replace("\n", "\n<7>"));
        if (goal.getKind() == CarnivalGoal.Kind.PLAY) {
            int played = Math.min(profile.stats(goal.getGame()).getYearAttempts(), goal.getTarget());
            lore.append("\n\n<7>Progress: <a>").append(played).append("<7>/<c>").append(goal.getTarget());
        }
        lore.append("\n\n<7>Rewards:\n<8>+<e>").append(goal.getTokens()).append(" Carnival Tokens")
                .append("\n<a>Carnival Ticket <8>x").append(CarnivalGoal.TICKETS_PER_GOAL)
                .append("\n\n").append(complete ? "<a><l>COMPLETE" : "<c><l>INCOMPLETE");
        return ItemStacks.item(goal.getGame().getIcon(), lore.toString());
    }
}
