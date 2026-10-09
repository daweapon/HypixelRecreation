package net.swofty.type.kuudra.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.kuudra.run.KuudraRunState;
import net.swofty.type.kuudra.run.KuudraRuns;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.function.Consumer;

@CommandParameters(labels = "kuudrarun",
        description = "Controls the Kuudra run you are in",
        usage = "/kuudrarun <win|lose|start|phase|supplies|build|fuel|stun|lair|eat|spit|damage|tokens|wave|spawn> [value]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class KuudraRunCommand extends HypixelCommand {
    private static final String USAGE = "<c>Usage: /kuudrarun <win|lose|start|phase|supplies|build|fuel|stun|lair|eat|spit|damage|tokens|wave|spawn> [value]";

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord action = new ArgumentWord("action").from("win", "lose", "start", "phase", "supplies", "build", "fuel",
                "stun", "lair", "eat", "spit");
        ArgumentWord stomach = new ArgumentWord("stomach").from("eat", "spit");
        ArgumentWord who = ArgumentType.Word("player");
        ArgumentWord phase = new ArgumentWord("phase").from(Arrays.stream(KuudraPhase.values())
                .map(each -> each.name().toLowerCase()).toArray(String[]::new));
        ArgumentWord numeric = new ArgumentWord("numeric").from("damage", "tokens", "wave");
        ArgumentDouble amount = ArgumentType.Double("amount");
        ArgumentWord spawn = new ArgumentWord("spawn").from("spawn");
        ArgumentWord mob = ArgumentType.Word("mob");

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
                case "supplies" -> withFight(player, run, fight -> {
                    fight.completeSupplies();
                    player.sendMessage("<a>Placed every supply; the run is now in <e>" + run.state().phase() + "<a>.");
                });
                case "build" -> withFight(player, run, fight -> {
                    fight.completeBuild();
                    player.sendMessage("<a>Finished the Ballista build.");
                });
                case "fuel" -> withFight(player, run, fight -> {
                    fight.completeFuel();
                    player.sendMessage("<a>Loaded every Fuel Cell; the run is now in <e>" + run.state().phase() + "<a>.");
                });
                case "stun" -> withFight(player, run, fight -> report(player, fight.stun(player),
                        "<a>Broke a pod: Kuudra is stunned."));
                case "lair" -> withFight(player, run, fight -> report(player, fight.lair(),
                        "<a>Kuudra is at 25%; the lair sequence is running (use it again to skip to the lair)."));
                case "eat", "spit" -> stomach(player, run, context.get(action), player);
                default -> player.sendMessage(USAGE);
            }
        }, action);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            KuudraRun run = runOf(player);
            if (run == null) return;
            String name = context.get(who);
            SkyBlockPlayer member = run.onlineMembers().stream()
                    .filter(each -> each.getUsername().equalsIgnoreCase(name)).findFirst().orElse(null);
            if (member == null) {
                player.sendMessage("<c>Nobody called " + name + " is in this run.");
                return;
            }
            stomach(player, run, context.get(stomach), member);
        }, stomach, who);

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

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            KuudraRun run = runOf(player);
            if (run == null) return;
            double value = context.get(amount);
            withFight(player, run, fight -> {
                switch (context.get(numeric)) {
                    case "damage" -> {
                        fight.damageKuudra(value);
                        player.sendMessage("<a>Kuudra took <c>" + value + "% <a>and is at <c>" + fight.bossState().percent() + "%<a>.");
                    }
                    case "tokens" -> {
                        fight.giveTokens((int) value);
                        player.sendMessage("<a>Gave the party <5>" + (int) value + " Tokens<a>.");
                    }
                    case "wave" -> {
                        fight.spawnWave((int) value);
                        player.sendMessage("<a>Spawned wave <c>" + (int) value + "<a>'s mobs.");
                    }
                    default -> player.sendMessage(USAGE);
                }
            });
        }, numeric, amount);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            KuudraRun run = runOf(player);
            if (run == null) return;
            String id = context.get(mob).toUpperCase();
            withFight(player, run, fight -> player.sendMessage(fight.spawnMob(id, player.getPosition()) != null
                    ? "<a>Spawned a <e>" + id + "<a>."
                    : "<c>No Kuudra mob called " + id + "."));
        }, spawn, mob);
    }

    private static KuudraRun runOf(SkyBlockPlayer player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        if (run == null) player.sendMessage("<c>You aren't in a Kuudra run.");
        return run;
    }

    private static void withFight(SkyBlockPlayer player, KuudraRun run, Consumer<KuudraFight> action) {
        KuudraFight fight = run.fight();
        if (fight == null) {
            player.sendMessage("<c>The fight hasn't started. Talk to Elle or use /kuudrarun start first.");
            return;
        }
        action.accept(fight);
    }

    private static void stomach(SkyBlockPlayer player, KuudraRun run, String action, SkyBlockPlayer target) {
        withFight(player, run, fight -> {
            if (action.equals("eat")) {
                report(player, fight.eat(target), "<a>" + target.getUsername() + " was swallowed into Kuudra's stomach.");
            } else {
                report(player, fight.spit(target), "<a>Kuudra spat " + target.getUsername() + " out.");
            }
        });
    }

    private static void report(SkyBlockPlayer player, KuudraFight.StaffResult result, String done) {
        player.sendMessage(switch (result) {
            case DONE -> done;
            case NOT_A_STUN_TIER -> "<c>Only Burning, Fiery and Infernal runs have Kuudra's stomach.";
            case NOT_INFERNAL -> "<c>Only Infernal runs have the lair.";
            case NOT_READY -> "<c>Not right now: Kuudra must be up (he is rising if he wasn't), or the player isn't inside.";
        });
    }

    private static void finish(SkyBlockPlayer player, KuudraRun run, KuudraRunState.Outcome outcome) {
        if (!run.finish(outcome)) {
            player.sendMessage(outcome == KuudraRunState.Outcome.WIN
                    ? "<c>The run must be in progress to win it. Talk to Elle or use /kuudrarun start first."
                    : "<c>This run is already over.");
        }
    }
}
