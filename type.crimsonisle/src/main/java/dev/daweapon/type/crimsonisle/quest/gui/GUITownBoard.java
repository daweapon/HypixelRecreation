package dev.daweapon.type.crimsonisle.quest.gui;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestBoard;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Town Board: today's five quests in the middle row, with the reputation header and daily checklists. */
public class GUITownBoard extends StatelessView {
    private static final int[] QUEST_SLOTS = {20, 21, 22, 23, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Town Board", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> FactionQuestItems.header((SkyBlockPlayer) c.player()));
        layout.slot(39, (_, c) -> FactionQuestItems.dailyMinibosses((SkyBlockPlayer) c.player()));
        layout.slot(41, (_, c) -> FactionQuestItems.dailyKuudra((SkyBlockPlayer) c.player()));

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        FactionQuestBoard board = FactionQuestService.board(player);
        if (board == null) return;
        for (int i = 0; i < board.getSlots().size() && i < QUEST_SLOTS.length; i++) {
            int index = i;
            layout.slot(QUEST_SLOTS[i],
                    (_, c) -> {
                        FactionQuestBoard current = FactionQuestService.board((SkyBlockPlayer) c.player());
                        return FactionQuestItems.quest((SkyBlockPlayer) c.player(), current.getSlots().get(index), true);
                    },
                    (_, c) -> open(c, index));
        }
    }

    private static void open(ViewContext ctx, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        FactionQuestBoard board = FactionQuestService.board(player);
        FactionQuestBoard.Slot slot = board == null ? null : board.slot(index);
        if (slot == null) return;
        if (slot.isClaimed()) {
            player.sendMessage("<c>You already claimed this quest's rewards!");
            return;
        }
        if (FactionQuestService.isClaimable(player, slot)) {
            if (GUIFactionQuestDetail.claim(player, index)) ctx.session(Object.class).refresh();
            return;
        }
        ctx.push(GUIFactionQuestDetail.of(player, index));
    }
}
