package net.swofty.type.crimsonisle.quest.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestBoard;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** One quest's page, titled with its category and showing the quest in slot 22 (SkyHanni's captured layout). */
public class GUIFactionQuestDetail extends StatelessView {
    private static final int QUEST_SLOT = 22;
    private final int index;
    private final String title;

    private GUIFactionQuestDetail(int index, String title) {
        this.index = index;
        this.title = title;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(QUEST_SLOT, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            FactionQuestBoard board = FactionQuestService.board(player);
            return FactionQuestItems.quest(player, board.getSlots().get(index), false);
        }, (_, c) -> {
            if (claim((SkyBlockPlayer) c.player(), index)) c.backOrClose();
        });
    }

    static boolean claim(SkyBlockPlayer player, int index) {
        FactionQuestService.ClaimResult result = FactionQuestService.claim(player, index);
        switch (result) {
            case CLAIMED -> {
            }
            case MISSING_ITEMS -> player.sendMessage("<c>You don't have the items this request asks for!");
            case NOT_COMPLETE -> player.sendMessage("<c>You haven't completed this quest yet!");
            case ALREADY_CLAIMED -> player.sendMessage("<c>You already claimed this quest's rewards!");
            case NO_FACTION -> player.sendMessage("<c>You need to join a faction first!");
        }
        return result == FactionQuestService.ClaimResult.CLAIMED;
    }

    public static GUIFactionQuestDetail of(SkyBlockPlayer player, int index) {
        FactionQuestBoard board = FactionQuestService.board(player);
        String category = board == null || board.slot(index) == null ? "Faction Quest"
                : board.getSlots().get(index).quest().type().getCategoryName();
        return new GUIFactionQuestDetail(index, category);
    }
}
