package dev.daweapon.type.thefarmingislands.lab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

final class LabScene {
    static final int LINE_TICKS = 50;
    private static final Set<UUID> PLAYING = ConcurrentHashMap.newKeySet();

    record Line(String speaker, String text, Consumer<SkyBlockPlayer> after) {
        static Line say(String speaker, String text) {
            return new Line(speaker, text, null);
        }

        static Line say(String speaker, String text, Consumer<SkyBlockPlayer> after) {
            return new Line(speaker, text, after);
        }

        static Line narrate(String text) {
            return new Line(null, "<8><o>" + text, null);
        }
    }

    private LabScene() {
    }

    static boolean isPlaying(SkyBlockPlayer player) {
        return PLAYING.contains(player.getUuid());
    }

    static boolean play(SkyBlockPlayer player, List<Line> lines, Runnable whenDone) {
        if (!PLAYING.add(player.getUuid())) return false;
        next(player, lines, 0, whenDone);
        return true;
    }

    private static void next(SkyBlockPlayer player, List<Line> lines, int index, Runnable whenDone) {
        if (!player.isOnline()) {
            PLAYING.remove(player.getUuid());
            return;
        }
        if (index >= lines.size()) {
            PLAYING.remove(player.getUuid());
            whenDone.run();
            return;
        }
        Line line = lines.get(index);
        String markup = line.speaker() == null ? line.text() : "<e>[NPC] " + line.speaker() + "<f>: " + line.text();
        player.sendMessage(Text.read(markup.replace("{player}", player.getUsername())));
        if (line.after() != null) line.after().accept(player);
        ScheduleUtility.delay(() -> next(player, lines, index + 1, whenDone), LINE_TICKS);
    }
}
