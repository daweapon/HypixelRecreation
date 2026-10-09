package net.swofty.type.kuudra.commands;

import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.kuudra.run.KuudraRunState;
import net.swofty.type.kuudra.run.KuudraRuns;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

@CommandParameters(labels = "kuudrarun",
        description = "Controls the Kuudra run you are in",
        usage = "/kuudrarun <win|lose|start|phase> [phase]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class KuudraRunCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord action = new ArgumentWord("action").from("win", "lose", "start", "phase");
        ArgumentWord phase = new ArgumentWord("phase").from(Arrays.stream(KuudraPhase.values())
                .map(each -> each.name().toLowerCase()).toArray(String[]::new));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            KuudraRun run = runOf(player);
            if (run == null) return;
            switch (context.get(action)) {
                case "win" -> finish(player, run, KuudraRunState.Outcome.WIN);
                case "lose" -> finish(player, run, KuudraRunState.Outcome.LOSE);
                case "start" -> player.sendMessage(run.begin()
                        ? "<a>Started the run." : "<c>This run has already started.");
                case "phase" -> player.sendMessage(run.advancePhase()
                        ? "<a>Moved the run to <e>" + run.state().phase() + "<a>."
                        : "<c>The run isn't in progress or is already in its last phase.");
                default -> player.sendMessage("<c>Usage: /kuudrarun <win|lose|start|phase> [phase]");
            }
        }, action);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            KuudraRun run = runOf(player);
            if (run == null) return;
            if (!context.get(action).equals("phase")) {
                player.sendMessage("<c>Only <e>/kuudrarun phase <c>takes a phase.");
                return;
            }
            KuudraPhase target = KuudraPhase.valueOf(context.get(phase).toUpperCase());
            player.sendMessage(run.enterPhase(target)
                    ? "<a>Moved the run to <e>" + target + "<a>."
                    : "<c>The run isn't in progress, is already in that phase, or this tier has no " + target + " phase.");
        }, action, phase);
    }

    private static KuudraRun runOf(SkyBlockPlayer player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        if (run == null) player.sendMessage("<c>You aren't in a Kuudra run.");
        return run;
    }

    private static void finish(SkyBlockPlayer player, KuudraRun run, KuudraRunState.Outcome outcome) {
        if (!run.finish(outcome)) {
            player.sendMessage(outcome == KuudraRunState.Outcome.WIN
                    ? "<c>The run must be in progress to win it. Talk to Elle or use /kuudrarun start first."
                    : "<c>This run is already over.");
        }
    }
}
