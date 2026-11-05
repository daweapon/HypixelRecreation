package dev.daweapon.type.skyblockgeneric.command.groups.force;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreCalculator;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsFloorIds;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUIDungeonRunChests;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.List;
import java.util.Locale;

@CommandParameters(description = "Grants the reward chests of a simulated finished Catacombs run at the given floor and score",
        labels = "dungeonchests",
        usage = "/force dungeonchests <floor> <score> [player]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ForceDungeonChestsCommand extends HypixelCommand {
    private static final List<String> FLOORS = List.of("F1", "F2", "F3", "F4", "F5", "F6", "F7",
            "M1", "M2", "M3", "M4", "M5", "M6", "M7");

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord floorArgument = ArgumentType.Word("floor");
        floorArgument.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toUpperCase(Locale.ROOT);
            FLOORS.stream().filter(floor -> floor.startsWith(typed)).forEach(floor -> suggestion.addEntry(new SuggestionEntry(floor)));
        });
        ArgumentInteger scoreArgument = ArgumentType.Integer("score");
        ArgumentEntity targetArgument = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            grant(player, player, context.get(floorArgument), context.get(scoreArgument));
        }, floorArgument, scoreArgument);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            Player target = context.get(targetArgument).findFirstPlayer(sender);
            if (target == null) {
                sender.sendMessage("<c>Could not find that player.");
                return;
            }
            grant((SkyBlockPlayer) sender, (SkyBlockPlayer) target, context.get(floorArgument), context.get(scoreArgument));
        }, floorArgument, scoreArgument, targetArgument);
    }

    private static void grant(SkyBlockPlayer staff, SkyBlockPlayer target, String floorInput, int score) {
        String key = floorInput.toUpperCase(Locale.ROOT);
        if (!FLOORS.contains(key)) {
            staff.sendMessage("<c>Unknown floor <e>" + floorInput + "<c>. Use F1-F7 or M1-M7 (the Entrance has no reward chests).");
            return;
        }
        CatacombsMode mode = key.startsWith("M") ? CatacombsMode.MASTER : CatacombsMode.NORMAL;
        CatacombsFloor floor = CatacombsFloorIds.parse("F" + key.substring(1)).orElseThrow();
        int clamped = Math.clamp(score, 0, DungeonScoreCalculator.MAXIMUM_SCORE);
        DungeonChestRun run = DungeonRewards.completeRun(target, floor, mode, clamped, List.of("<a>" + target.getUsername()));
        if (run == null) {
            staff.sendMessage("<c>That run has no reward chests.");
            return;
        }
        target.sendMessage("<a>Simulated a " + run.title() + " <a>win with <e>" + clamped + " <a>score (" + run.rank().displayName() + ").");
        target.sendMessage("<7>Your chests are stored at <e>Croesus <7>for <a>72 hours<7>.");
        target.openView(new GUIDungeonRunChests(run));
        if (target != staff) {
            staff.sendMessage("<a>Granted " + run.title() + " <a>reward chests to <e>" + target.getUsername() + "<a>.");
        }
    }
}
