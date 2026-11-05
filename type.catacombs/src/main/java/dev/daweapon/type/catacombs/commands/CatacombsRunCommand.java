package dev.daweapon.type.catacombs.commands;

import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import dev.daweapon.type.catacombs.engine.CatacombsMob;
import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.catacombs.engine.KeyType;
import dev.daweapon.type.catacombs.run.CatacombsLifecycle;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

import java.util.Arrays;
import java.util.function.BiConsumer;

@CommandParameters(labels = "catacombsrun",
        description = "Controls the Catacombs run you are in",
        usage = "/catacombsrun <ready|start|win|lose|boss|clear|clearall|secret|crypt|failpuzzle|die|revive|cooldowns|seed|layout|score|phase|key|door|spawn|blessing> [value]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class CatacombsRunCommand extends HypixelCommand {
    private static final int STAFF_BLESSING_LEVEL = 5;
    private static final String USAGE = "<c>Usage: /catacombsrun <ready|start|win|lose|boss|clear|clearall|secret|crypt|failpuzzle|die|revive|cooldowns|seed|layout|score|phase|key|door|spawn|blessing> [value]";

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord action = new ArgumentWord("action").from("ready", "start", "win", "lose", "boss", "clear", "clearall",
                "secret", "crypt", "failpuzzle", "die", "revive", "cooldowns", "seed", "layout", "score");
        ArgumentWord phaseLiteral = new ArgumentWord("phaseLiteral").from("phase");
        ArgumentWord phase = new ArgumentWord("phase").from(Arrays.stream(DungeonRunPhase.values())
                .filter(each -> each.ordinal() >= DungeonRunPhase.CLEARING.ordinal() && each.ordinal() <= DungeonRunPhase.BOSS.ordinal())
                .map(each -> each.name().toLowerCase()).toArray(String[]::new));
        ArgumentWord keyLiteral = new ArgumentWord("keyLiteral").from("key");
        ArgumentWord key = new ArgumentWord("key").from("wither", "blood");
        ArgumentWord doorLiteral = new ArgumentWord("doorLiteral").from("door");
        ArgumentWord door = new ArgumentWord("door").from("next");
        ArgumentWord spawnLiteral = new ArgumentWord("spawnLiteral").from("spawn");
        ArgumentWord blessingLiteral = new ArgumentWord("blessingLiteral").from("blessing");
        ArgumentWord blessing = new ArgumentWord("blessing").from(Arrays.stream(BlessingType.values())
                .map(each -> each.name().toLowerCase()).toArray(String[]::new));
        ArgumentWord mob = ArgumentType.Word("mob").from(DungeonMobRegistry.defaults().mobs().keySet().toArray(String[]::new));

        command.addSyntax((sender, context) -> with(sender, (player, run) -> perform(player, run, context.get(action))), action);

        command.addSyntax((sender, context) -> with(sender, (player, run) -> {
            DungeonRunPhase target = DungeonRunPhase.valueOf(context.get(phase).toUpperCase());
            player.sendMessage(run.advanceTo(target)
                    ? "<a>Moved the run to <e>" + target + "<a>."
                    : "<c>The run isn't active or is already at or past " + target + ".");
        }), phaseLiteral, phase);

        command.addSyntax((sender, context) -> with(sender, (player, run) -> withEngine(player, run, engine -> {
            KeyType type = KeyType.valueOf(context.get(key).toUpperCase());
            engine.giveKey(type);
        })), keyLiteral, key);

        command.addSyntax((sender, context) -> with(sender, (player, run) -> withEngine(player, run, engine ->
                player.sendMessage(engine.openNextDoor(player) ? "<a>Opened the next key door." : "<c>No closed key door is left on the critical path."))),
                doorLiteral, door);

        command.addSyntax((sender, context) -> with(sender, (player, run) -> withEngine(player, run, engine -> {
            String id = context.get(mob).toUpperCase();
            CatacombsMob spawned = engine.spawnMob(id, player.getPosition());
            player.sendMessage(spawned != null ? "<a>Spawned a <e>" + id + "<a>." : "<c>No dungeon mob called " + id + ".");
        })), spawnLiteral, mob);

        command.addSyntax((sender, context) -> with(sender, (player, run) -> withEngine(player, run, engine ->
                engine.grantBlessing(BlessingType.valueOf(context.get(blessing).toUpperCase()), STAFF_BLESSING_LEVEL, player))),
                blessingLiteral, blessing);
    }

    private static void with(Object sender, BiConsumer<SkyBlockPlayer, CatacombsRun> action) {
        if (!(sender instanceof SkyBlockPlayer player)) return;
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run == null) {
            player.sendMessage("<c>You aren't in a Catacombs run.");
            return;
        }
        action.accept(player, run);
    }

    private static void withEngine(SkyBlockPlayer player, CatacombsRun run, java.util.function.Consumer<DungeonEngine> action) {
        DungeonEngine engine = run.engine();
        if (engine == null) {
            player.sendMessage("<c>The run hasn't started. Use /catacombsrun start first.");
            return;
        }
        action.accept(engine);
    }

    private void perform(SkyBlockPlayer player, CatacombsRun run, String action) {
        switch (action) {
            case "ready" -> run.toggleReady(player);
            case "start" -> player.sendMessage(run.begin() ? "<a>Started the run." : "<c>This run has already started.");
            case "win" -> player.sendMessage(run.finish(CatacombsLifecycle.Outcome.WIN)
                    ? "<a>Won the run." : "<c>The run must be active to win it. Use /catacombsrun start first.");
            case "lose" -> player.sendMessage(run.finish(CatacombsLifecycle.Outcome.LOSE)
                    ? "<a>Failed the run." : "<c>This run is already over.");
            case "boss" -> withEngine(player, run, engine -> {
                engine.enterBoss();
                player.sendMessage("<a>Sent the party to the boss arena.");
            });
            case "clear" -> withEngine(player, run, engine -> {
                PlacedRoom room = engine.roomAt(player.getPosition()).orElse(null);
                if (room == null) {
                    player.sendMessage("<c>You aren't standing in a room.");
                    return;
                }
                engine.clearRoom(room, player);
                player.sendMessage("<a>Cleared room <e>" + room.index() + " <7>(" + room.template().name() + ")<a>.");
            });
            case "clearall" -> withEngine(player, run, engine -> {
                engine.clearAll(player);
                player.sendMessage("<a>Cleared every room except the Blood Room.");
            });
            case "secret" -> withEngine(player, run, engine -> player.sendMessage(engine.collectNearestSecret(player)
                    .map(spot -> "<a>Found the nearest " + spot.type().name().toLowerCase().replace('_', ' ') + " secret in room <e>"
                            + spot.room() + "<a>: <e>" + run.state().secretsFound() + "<7>/<e>" + run.state().config().totalSecrets())
                    .orElse("<c>Every secret in this run has been found.")));
            case "crypt" -> withEngine(player, run, engine -> player.sendMessage(engine.blowNearestCrypt(player.getPosition())
                    ? "<a>Blew open the nearest crypt. Kill its Crypt Undead to count it."
                    : "<c>Every crypt in this run is already open."));
            case "failpuzzle" -> withEngine(player, run, engine -> {
                PlacedRoom room = engine.roomAt(player.getPosition()).orElse(null);
                if (room == null) {
                    player.sendMessage("<c>You aren't standing in a room.");
                    return;
                }
                run.failPuzzle(room.index());
                player.sendMessage("<a>Failed the puzzle in room <e>" + room.index() + "<a>.");
            });
            case "die" -> player.sendMessage(run.death(player) ? "<a>You died." : "<c>You can't die right now.");
            case "revive" -> player.sendMessage("<a>Revived <e>" + run.reviveAll() + "<a> ghost(s).");
            case "cooldowns" -> {
                run.classes().cooldowns().reset(player.getUuid());
                player.sendMessage("<a>Reset your class ability cooldowns.");
            }
            case "seed" -> player.sendMessage("<a>Layout seed: <e>" + run.seed());
            case "layout" -> run.arena().layout().render().lines().forEach(line -> player.sendMessage("<7>" + line));
            case "score" -> withEngine(player, run, _ -> score(player, run));
            default -> player.sendMessage(USAGE);
        }
    }

    private static void score(SkyBlockPlayer player, CatacombsRun run) {
        CatacombsRunState state = run.state();
        DungeonScoreBreakdown score = state.score();
        player.sendMessage("<a>Skill <e>" + score.skill() + "<a>, Exploration <e>" + score.exploration() + "<a>, Speed <e>"
                + score.speed() + "<a>, Bonus <e>" + score.bonus() + "<a> = <e>" + score.total() + " <7>(" + score.rank().displayName() + ")");
        player.sendMessage("<7>Rooms " + state.completedRooms() + "/" + state.config().totalRooms() + ", secrets "
                + state.secretsFound() + "/" + state.config().totalSecrets() + ", deaths " + state.deaths()
                + ", " + state.elapsed().toSeconds() + "s");
    }
}
