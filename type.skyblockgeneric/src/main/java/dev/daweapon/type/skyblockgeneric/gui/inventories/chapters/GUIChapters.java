package dev.daweapon.type.skyblockgeneric.gui.inventories.chapters;

import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.chapters.Chapter;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterRegistry;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

public class GUIChapters extends StatelessView {
    private static final int HINA_SLOT = 4;
    private static final int FIRST_CHAPTER_SLOT = 19;
    private static final int CLOSE_SLOT = 31;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Hina's Chapters", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(HINA_SLOT, ChapterItems.hina());
        for (Chapter chapter : ChapterRegistry.get().chapters()) {
            layout.slot(FIRST_CHAPTER_SLOT + chapter.number() - 1, (_, _) -> ChapterItems.chapter(player, chapter), (_, c) -> {
                if (ChapterService.isUnlocked(player, chapter)) {
                    c.push(new GUIChapter(chapter.number()));
                } else {
                    player.sendMessage("<c>You haven't unlocked this chapter yet!");
                }
            });
        }
        Components.close(layout, CLOSE_SLOT);
    }
}
