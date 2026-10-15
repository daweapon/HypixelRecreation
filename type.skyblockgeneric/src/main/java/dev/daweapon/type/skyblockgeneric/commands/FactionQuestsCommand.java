package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestBoard;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Lists, rerolls or completes your daily Crimson Isle Faction Quests",
        labels = "factionquests fquests",
        usage = "/factionquests <list|reroll|complete> [slot]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class FactionQuestsCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord action = ArgumentType.Word("action").from("list", "reroll");
        ArgumentWord complete = ArgumentType.Word("complete").from("complete");
        ArgumentNumber<Integer> slot = ArgumentType.Integer("slot").between(1, FactionQuestService.SLOTS);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            CrimsonFaction faction = CrimsonReputationService.faction(player);
            if (faction == null) {
                player.sendMessage("<c>Join a faction first by talking to Elle.");
                return;
            }
            if (context.get(action).equals("reroll")) {
                FactionQuestService.reroll(player, faction);
                player.sendMessage("<a>Rolled a new " + faction.coloured() + " <a>Town Board for today.");
            }
            list(player);
        }, action);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!FactionQuestService.forceComplete(player, context.get(slot) - 1)) {
                player.sendMessage("<c>That slot is empty or already complete.");
            }
        }, complete, slot);
    }

    private void list(SkyBlockPlayer player) {
        FactionQuestBoard board = FactionQuestService.board(player);
        if (board == null) return;
        player.sendMessage("<e>Faction Quests <7>(reset in " + FactionQuestService.describeReset() + ")");
        for (int i = 0; i < board.getSlots().size(); i++) {
            FactionQuestBoard.Slot slot = board.getSlots().get(i);
            player.sendMessage(" <7>" + (i + 1) + ". " + slot.statusSymbol() + " " + slot.quest().tier().coloured() + " "
                    + slot.quest().tier().getColour() + slot.quest().tabName() + " <8>(" + slot.quest().type().getCategoryName()
                    + (slot.isClaimed() ? ", claimed" : "") + ")");
        }
    }
}
