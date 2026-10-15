package dev.daweapon.type.skyblockgeneric.command.groups.hand;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "enchant",
        description = "Enchants the contents o the players hand",
        usage = "/hand enchant <enchantment_type> <level>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class EnchantCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<EnchantmentType> typeArgument =
                ArgumentType.Enum("enchantment_type", EnchantmentType.class);
        ArgumentInteger level = ArgumentType.Integer("level");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
                itemAttributeHandler.addEnchantment(
                        SkyBlockEnchantment.builder()
                                .level(context.get(level))
                                .type(context.get(typeArgument))
                                .build()
                );
                sender.sendMessage("<a>Your item has been enchanted");
            });
        }, typeArgument, level);
    }
}
