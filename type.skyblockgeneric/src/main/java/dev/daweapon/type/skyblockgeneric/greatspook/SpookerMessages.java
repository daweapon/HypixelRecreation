package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Random;

public final class SpookerMessages {
    public record Line(int delaySeconds, String markup) {
    }

    public record Spook(String name, List<Line> lines) {
    }

    private static final List<String> ITEMS = List.of("Hyperion", "Aspect of the End", "Bonzo's Mask");
    private static final List<String> GUILD_REASONS = List.of("We need more space", "The Ban Hammer Has Spoken!",
            "bruh", "F", "Goodbye!", "Not funny enough");
    private static final List<String> PREFIXES = List.of("<7>Nope! <d>Just kidding! ", "<7>False! <d>Just kidding! ", "");

    private SpookerMessages() {
    }

    public static Spook random(Random random) {
        String item = ITEMS.get(random.nextInt(ITEMS.size()));
        List<Spook> spooks = List.of(
                new Spook("Profile Delete", List.of(
                        new Line(0, "<c><l>DO YOU REALLY WANT TO DELETE YOUR CURRENT PROFILE?"),
                        new Line(0, "<c>It will delete in 10 seconds..."),
                        new Line(5, "<c>5"), new Line(1, "<c>4"), new Line(1, "<c>3"), new Line(1, "<c>2"),
                        new Line(1, "<c>1"), new Line(1, "<c>..."),
                        new Line(1, "<a>Your profile is fine! This was just an evil troll :)"))),
                new Spook("OP", List.of(
                        new Line(0, "<e>You are now op!"),
                        new Line(3, "<a>Only kidding! We won't give you op ;)"))),
                new Spook("Blacklisted Item", List.of(
                        new Line(0, "<c>You had a blacklisted " + item + " in your inventory, we had to delete it! Sorry!"),
                        new Line(3, "<a>JK! Your " + item + " is fine. This was just a big mean troll :)"))),
                new Spook("Lost Coins", List.of(
                        new Line(0, "<c>You died and lost half of your purse coins!"),
                        new Line(3, "<a>Just kidding! All your coins are fine, this was just a big mean troll :)"))),
                new Spook("Guild Kick", List.of(
                        new Line(0, "<c>You were kicked from your guild with reason \""
                                + GUILD_REASONS.get(random.nextInt(GUILD_REASONS.size())) + "\""),
                        new Line(3, "<a>Just kidding! You are still in your guild, this was just a mean troll :)"))));
        return spooks.get(random.nextInt(spooks.size()));
    }

    public static String reveal(Random random, String spooker) {
        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        return prefix.isEmpty() ? "<7>You just got spooked! " + spooker + " is the culprit!"
                : prefix + spooker + " just spooked you!";
    }

    public static void play(SkyBlockPlayer target, Spook spook) {
        int elapsed = 0;
        for (Line line : spook.lines()) {
            elapsed += line.delaySeconds();
            String markup = line.markup();
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (!target.isRemoved()) target.sendMessage(markup);
            }).delay(TaskSchedule.seconds(elapsed)).schedule();
        }
    }
}
