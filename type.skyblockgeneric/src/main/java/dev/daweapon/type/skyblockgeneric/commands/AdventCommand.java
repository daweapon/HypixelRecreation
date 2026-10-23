package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.advent.AdventCalendar;
import dev.daweapon.type.skyblockgeneric.advent.AdventService;
import dev.daweapon.type.skyblockgeneric.gui.inventories.advent.GUIAdventCalendar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

@CommandParameters(description = "Opens the Advent Calendar", usage = "/sbadvent; /sbadvent preview <day|0>", permission = Rank.DEFAULT,
        labels = "sbadvent adventcalendar", allowsConsole = false)
public class AdventCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            ((SkyBlockPlayer) sender).openView(new GUIAdventCalendar());
        });

        ArgumentInteger dayArg = ArgumentType.Integer("day");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!player.getRank().isStaff()) {
                player.sendMessage("<c>Only staff can preview the calendar!");
                return;
            }
            int day = context.get(dayArg);
            AdventService.preview(player, day <= 0 ? null : Math.min(day, AdventCalendar.DAYS));
            player.sendMessage("<a>Advent Calendar preview {}.", day <= 0 ? "off" : "set to day " + Math.min(day, AdventCalendar.DAYS));
        }, ArgumentType.Literal("preview"), dayArg);
    }
}
