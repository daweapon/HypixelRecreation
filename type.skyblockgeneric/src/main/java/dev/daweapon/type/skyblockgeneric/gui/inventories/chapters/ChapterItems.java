package dev.daweapon.type.skyblockgeneric.gui.inventories.chapters;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.chapters.Chapter;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterRegistry;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTask;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterText;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

final class ChapterItems {
    static final String HINA_TEXTURE = "513be4d37a35840024b26f8d623ff14119e761784592f1a2cc4b01fca8fb7b2";

    private ChapterItems() {
    }

    static ItemStack.Builder hina() {
        return ItemStacks.head(HINA_TEXTURE, """
                <b>Hina
                <7>Hina knows every secret of the
                <2>Moonglade Marsh<7>, the first island of
                <2>Galatea<7>.

                <7>Complete her challenges to earn
                <7>rewards for each task, and even
                <7>greater ones for finishing a whole
                <7>chapter.""");
    }

    static ItemStack.Builder chapter(SkyBlockPlayer player, Chapter chapter) {
        boolean complete = ChapterService.isComplete(player, chapter);
        boolean unlocked = ChapterService.isUnlocked(player, chapter);
        Material pane = complete ? Material.LIME_STAINED_GLASS_PANE : unlocked ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String colour = complete ? "<a>" : unlocked ? "<e>" : "<c>";
        long done = ChapterService.tasksDone(player, chapter);
        List<String> lines = new ArrayList<>();
        lines.add("<2>" + ChapterRegistry.get().island() + " <7>- " + colour + "Chapter " + chapter.number());
        lines.addAll(ChapterText.wrap(chapter.description()));
        lines.add("");
        lines.add("<7>Tasks completed: " + colour + done + "<7>/<a>" + chapter.availableTasks().size());
        lines.add("");
        lines.add(complete ? "<a>Chapter completed!" : unlocked ? "<e>Click to view tasks!" : "<c>You haven't unlocked this chapter yet!");
        return ItemStacks.item(pane, chapter.number(), String.join("\n", lines));
    }

    static ItemStack.Builder task(SkyBlockPlayer player, ChapterTask task) {
        boolean done = ChapterService.isDone(player, task);
        String base = done ? "<a>" : task.available() ? "<c>" : "<8>";
        List<String> lines = new ArrayList<>();
        lines.add(base + task.coloredTitle(base));
        lines.addAll(ChapterText.wrap(task.description()));
        lines.add("");
        lines.add("<7>Reward:");
        task.reward().lines().forEach(line -> lines.add("  " + line));
        lines.add("");
        if (task.available()) {
            long progress = ChapterService.progress(player, task);
            String colour = done ? "<a>" : "<c>";
            lines.add("<7>Progress: " + colour + format(progress) + "<7>/<a>" + format(task.amount()));
        } else {
            lines.add("<8>Not available yet.");
        }
        return ItemStacks.item(done ? Material.LIME_DYE : Material.GRAY_DYE, String.join("\n", lines));
    }

    static ItemStack.Builder completion(SkyBlockPlayer player, Chapter chapter) {
        boolean complete = ChapterService.isComplete(player, chapter);
        long done = ChapterService.tasksDone(player, chapter);
        int total = chapter.availableTasks().size();
        String colour = complete ? "<a>" : "<c>";
        List<String> lines = new ArrayList<>();
        lines.add(colour + "Chapter " + chapter.number());
        lines.add("<7>Tasks completed: " + colour + done + "<7>/<a>" + total);
        lines.add("");
        lines.add("<7>Complete every task to obtain these rewards:");
        chapter.completion().lines().forEach(line -> lines.add("  " + line));
        if (chapter.number() < ChapterRegistry.get().chapters().size()) {
            lines.add("  <a>Unlocks Chapter " + (chapter.number() + 1) + " Tasks!");
        }
        lines.add("");
        lines.add("<7>Progress: " + colour + done + "<7>/<a>" + total);
        return ItemStacks.item(complete ? Material.ENDER_CHEST : Material.CHEST, String.join("\n", lines));
    }

    private static String format(long value) {
        return String.format("%,d", value);
    }
}
