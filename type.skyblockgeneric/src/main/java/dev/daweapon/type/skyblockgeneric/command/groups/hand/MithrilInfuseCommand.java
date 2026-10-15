package dev.daweapon.type.skyblockgeneric.command.groups.hand;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

@CommandParameters(labels = "mithrilinfuse",
        description = "Mithril Infuses the minion in the players hand",
        usage = "/hand mithrilinfuse",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MithrilInfuseCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = ((SkyBlockPlayer) sender);
            if (!(new SkyBlockItem(player.getItemInMainHand()).hasComponent(MinionComponent.class))) {
                player.sendMessage("<c>Mithril Infusions can only be applied to minions.");
                return;
            }
            player.updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
                try {
                    itemAttributeHandler.setMithrilInfused(true);
                    player.sendMessage("<a>Mithril Infusion applied to {}.", item.getDisplayName());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("<c>{}", e.getMessage());
                }
            });
        });
    }
}
