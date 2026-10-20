package dev.daweapon.type.skyblockgeneric.gui.inventories.chapters;

import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.chapters.Chapter;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterRegistry;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTask;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

import java.util.List;

public class GUIChapter extends StatelessView {
    private static final int HINA_SLOT = 4;
    private static final int PER_ROW = 7;
    private static final int[] SINGLE_ROW_COLUMNS = {3, 5, 7};
    private final Chapter chapter;

    public GUIChapter(int number) {
        this.chapter = ChapterRegistry.get().chapter(number);
    }

    private int taskRows() {
        return (chapter.tasks().size() + PER_ROW - 1) / PER_ROW;
    }

    private int firstRow() {
        return taskRows() == 1 ? 3 : 2;
    }

    private int totalRows() {
        return firstRow() + taskRows() + 1;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        InventoryType type = totalRows() <= 5 ? InventoryType.CHEST_5_ROW : InventoryType.CHEST_6_ROW;
        return new ViewConfiguration<>("Hina - Chapter " + chapter.number(), type);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(HINA_SLOT, ChapterItems.hina());
        List<ChapterTask> tasks = chapter.tasks();
        for (int row = 0; row < taskRows(); row++) {
            int from = row * PER_ROW;
            int count = Math.min(PER_ROW, tasks.size() - from);
            for (int index = 0; index < count; index++) {
                ChapterTask task = tasks.get(from + index);
                layout.slot((firstRow() + row - 1) * 9 + column(count, index) - 1, (_, _) -> ChapterItems.task(player, task));
            }
        }
        int chestRow = firstRow() + taskRows();
        layout.slot((chestRow - 1) * 9 + 4, (_, _) -> ChapterItems.completion(player, chapter));
        int bottom = (totalRows() - 1) * 9;
        Components.close(layout, bottom + 4);
        Components.back(layout, bottom + 3, ctx);
    }

    private int column(int count, int index) {
        if (taskRows() == 1 && count == SINGLE_ROW_COLUMNS.length) return SINGLE_ROW_COLUMNS[index];
        return 5 - count / 2 + index;
    }
}
