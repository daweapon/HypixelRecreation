package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.function.Consumer;

public final class VargulService {
    private static final int LINE_DELAY_TICKS = 15;
    private static final String AWAKENED = "<f><l>Vargul the Unearthed has awakened!";

    private record Unearthing(String omen, String line, String reaction, String reply) {
    }

    private static final List<Unearthing> UNEARTHINGS = List.of(
            new Unearthing("The ground beneath you trembles, and an eerie wind howls through the air...",
                    "Who dares disturb my eternal slumber?", "The tombstone cracks and shifts!",
                    "You better have something to offer me."),
            new Unearthing("The earth shakes with knowing fury.", "You've become a regular visitor.",
                    "The tombstone practically opens itself.", "Ah, my most loyal servant returns."),
            new Unearthing("The earth shivers in anticipation...", "Once more you wake me.",
                    "The tombstone shifts, almost eager.", "I sense you have more offerings."),
            new Unearthing("A familiar rumble echoes.", "So, you seek my favor again?",
                    "The tombstone rises quicker this time.", "You're proving to be quite useful."),
            new Unearthing("The ground almost hums with energy...", "I grow accustomed to these interruptions.",
                    "The tombstone practically leaps from the ground.", "You've earned a place in my dark realm."),
            new Unearthing("The ground rumbles, almost lazily.", "Ah, you again...",
                    "The tombstone creaks open with little fanfare.",
                    "Return as often as you wish, but know that my generosity has its limits."),
            new Unearthing("The ground quakes once more...", "You dare return?!",
                    "The tombstone seems to mend itself.", "Ah, it's you again. Have you brought more Dark Candies?"));

    private VargulService() {
    }

    public static Unearthing unearthingFor(int visit) {
        return UNEARTHINGS.get(Math.min(Math.max(visit, 1), UNEARTHINGS.size()) - 1);
    }

    public static int unearthingCount() {
        return UNEARTHINGS.size();
    }

    public static List<String> script(int visit) {
        Unearthing unearthing = unearthingFor(visit);
        return List.of("<c><o>" + unearthing.omen(), speak(unearthing.line()),
                "<c><o>" + unearthing.reaction(), speak(unearthing.reply()), AWAKENED);
    }

    private static String speak(String text) {
        return "<f><l>[<c><l>Vargul<f><l>]<r><7>: <d>" + text;
    }

    public static void unearth(SkyBlockPlayer player, Consumer<SkyBlockPlayer> menu) {
        List<String> lines = script(VargulProgress.recordVisit(player));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (i == 0) player.sendMessage(line);
            else schedule(i * LINE_DELAY_TICKS, () -> player.sendMessage(line));
        }
        schedule(lines.size() * LINE_DELAY_TICKS, () -> menu.accept(player));
    }

    private static void schedule(int ticks, Runnable task) {
        MinecraftServer.getSchedulerManager().buildTask(task).delay(TaskSchedule.tick(ticks)).schedule();
    }
}
