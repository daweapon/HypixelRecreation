package net.swofty.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGame;
import net.swofty.type.skyblockgeneric.carnival.CarnivalProfile;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Chantelle's Carnival Stats, from the wiki's Chantelle/UI subpage.
public class GUICarnivalStats extends StatelessView {
    private static final int[] SLOTS = {20, 22, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Carnival Stats", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        for (CarnivalGame game : CarnivalGame.values()) {
            layout.slot(SLOTS[game.ordinal()], (_, c) -> {
                CarnivalProfile.GameStats stats = Carnivals.profile((SkyBlockPlayer) c.player()).stats(game);
                return ItemStacks.item(game.getIcon(), game.colored() + "\n"
                        + "\n<e>Lifetime"
                        + "\n<7>Attempts: <a>" + StringUtility.commaify(stats.getLifetimeAttempts())
                        + "\n<7>High Score: <a>" + StringUtility.commaify(stats.getLifetimeHighScore())
                        + "\n"
                        + "\n<e>This Year"
                        + "\n<7>Attempts: <a>" + StringUtility.commaify(stats.getYearAttempts())
                        + "\n<7>High Score: <a>" + StringUtility.commaify(stats.getYearHighScore()));
            });
        }
    }
}
