package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@CommandParameters(labels = "lagprofile",
        description = "Samples the server's busy threads and lists the code using the most time",
        usage = "/lagprofile [seconds]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class LagProfileCommand extends HypixelCommand {
    private static final int SAMPLE_INTERVAL_MS = 10;
    private static final int DEFAULT_SECONDS = 5;
    private static final int TOP = 8;

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Integer> seconds = ArgumentType.Integer("seconds").between(1, 30);
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            profile((SkyBlockPlayer) sender, DEFAULT_SECONDS);
        });
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            profile((SkyBlockPlayer) sender, context.get(seconds));
        }, seconds);
    }

    private static void profile(SkyBlockPlayer player, int seconds) {
        player.sendMessage("<e>Sampling the server for <a>" + seconds + "s<e>...");
        Thread.startVirtualThread(() -> {
            Map<String, Integer> hits = new HashMap<>();
            int samples = 0;
            long end = System.currentTimeMillis() + seconds * 1000L;
            while (System.currentTimeMillis() < end) {
                for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                    if (!isServerThread(entry.getKey()) || entry.getKey().getState() != Thread.State.RUNNABLE) continue;
                    String frame = firstOwnFrame(entry.getValue());
                    if (frame != null) hits.merge(frame, 1, Integer::sum);
                }
                samples++;
                try {
                    Thread.sleep(SAMPLE_INTERVAL_MS);
                } catch (InterruptedException e) {
                    return;
                }
            }
            int total = samples;
            player.sendMessage("<6><l>LAG PROFILE</l> <7>(" + total + " samples, % of samples a thread was busy there)");
            hits.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                    .limit(TOP)
                    .forEach(hit -> player.sendMessage("<e>" + Math.round(hit.getValue() * 100D / total) + "% <7>" + hit.getKey()));
            if (hits.isEmpty()) player.sendMessage("<a>Nothing in SkyBlock code was busy.");
        });
    }

    private static boolean isServerThread(Thread thread) {
        String name = thread.getName();
        return name.startsWith("Ms-") || name.startsWith("ForkJoinPool");
    }

    private static String firstOwnFrame(StackTraceElement[] stack) {
        for (StackTraceElement element : stack) {
            if (element.getClassName().startsWith("net.swofty")) {
                String className = element.getClassName().substring(element.getClassName().lastIndexOf('.') + 1);
                return className + "." + element.getMethodName() + ":" + element.getLineNumber();
            }
        }
        return null;
    }
}
