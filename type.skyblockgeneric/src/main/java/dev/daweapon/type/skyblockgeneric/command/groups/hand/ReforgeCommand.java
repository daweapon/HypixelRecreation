package dev.daweapon.type.skyblockgeneric.command.groups.hand;

import dev.daweapon.commons.skyblock.item.reforge.Reforge;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeLoader;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.components.ReforgableComponent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@CommandParameters(labels = "reforge",
        description = "Reforges the item in the players hand",
        usage = "/hand reforge",
        permission = Rank.STAFF,
        allowsConsole = false)
public class ReforgeCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
                ReforgeType reforgeType = item.getComponent(ReforgableComponent.class).getReforgeType();

                // Get all valid reforges for this item type
                List<Reforge> validReforges = ReforgeLoader.getReforgesForType(reforgeType);

                if (validReforges.isEmpty()) {
                    sender.sendMessage("<c>No reforges available for this item type!");
                    return;
                }

                // Select a random reforge
                Reforge reforge = validReforges.get(MathUtility.random(0, validReforges.size() - 1));

                try {
                    itemAttributeHandler.setReforge(reforge);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Text.of("<c>{}", e.getMessage()));
                }
            });
        });
    }
}
