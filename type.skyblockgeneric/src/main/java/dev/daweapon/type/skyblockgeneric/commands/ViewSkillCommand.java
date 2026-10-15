package dev.daweapon.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.skills.GUISkillCategory;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens up a skills GUI",
        usage = "/viewskill <skill>",
        permission = Rank.DEFAULT,
    labels = "vs viewskill",
        allowsConsole = false)
public class ViewSkillCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<SkillCategories> skillArgument = ArgumentType.Enum("skill", SkillCategories.class);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            final SkillCategories skillCategory = context.get(skillArgument);
            ((SkyBlockPlayer) sender).openView(new GUISkillCategory(skillCategory, 0));
        }, skillArgument);
    }
}
