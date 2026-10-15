package dev.daweapon.type.skyblockgeneric.command.groups.max;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.mythological.gear.CrownOfAvarice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "coa",
        description = "Maxes out a specific item",
        usage = "/max coa",
        permission = Rank.STAFF,
        allowsConsole = false)
public class MaxCoaCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!permissionCheck(sender)) return;
            maxCrownOfAvarice((SkyBlockPlayer) sender);
        });
    }

    private static void maxCrownOfAvarice(SkyBlockPlayer player) {
        PlayerItemOrigin origin = isCrown(player.getItemInMainHand()) ? PlayerItemOrigin.MAIN_HAND
                : isCrown(player.getHelmet()) ? PlayerItemOrigin.HELMET : null;
        if (origin == null) {
            player.sendMessage("<c>Hold or wear a Crown of Avarice to max it!");
            return;
        }
        player.updateItem(origin, item -> item.getAttributeHandler().setCoinsConsumed(CrownOfAvarice.CAPACITY));
        player.sendMessage("<a>Your <6>Crown of Avarice <a>has now consumed <6>1,000,000,000 Coins<a>.");
    }

    private static boolean isCrown(ItemStack stack) {
        return !stack.isAir() && new SkyBlockItem(stack).getAttributeHandler().getPotentialType() == ItemType.CROWN_OF_AVARICE;
    }
}
