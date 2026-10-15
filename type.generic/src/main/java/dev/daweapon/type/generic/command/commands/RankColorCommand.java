package dev.daweapon.type.generic.command.commands;

import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.gui.impl.GUIRankColor;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "rankcolour rankcolor", description = "Change your MVP+ rank color",
    usage = "/rankcolor", permission = Rank.DEFAULT, allowsConsole = false)
// Command that opens the GUI for changing an MVP+ player's rank color.
public class RankColorCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> ((HypixelPlayer) sender).openView(new GUIRankColor()));
    }
}
