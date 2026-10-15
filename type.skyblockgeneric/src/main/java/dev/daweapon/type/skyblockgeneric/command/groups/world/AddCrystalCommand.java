package dev.daweapon.type.skyblockgeneric.command.groups.world;

import dev.daweapon.type.skyblockgeneric.commands.ArgumentItemType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.data.crystals.CrystalCatalog;
import dev.daweapon.type.skyblockgeneric.entity.ServerCrystalImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ServerOrbComponent;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "crystal",
        description = "Spawns in a crystal at the player's location.",
        usage = "/world crystal",
        permission = Rank.STAFF,
        allowsConsole = false)
public class AddCrystalCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentItemType itemType = new ArgumentItemType("itemType");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ItemType type = context.get(itemType);
            SkyBlockItem item = new SkyBlockItem(type);

            // Spawn the orb
            ServerOrbComponent asOrb = item.getComponent(ServerOrbComponent.class);
            ServerCrystalImpl crystal = new ServerCrystalImpl(
                    asOrb.getSpawnMaterialFunction(),
                    item.getComponent(SkullHeadComponent.class).getSkullTexture(item),
                    asOrb.getValidBlocks()
            );
            crystal.setInstance(((SkyBlockPlayer) sender).getInstance(), ((SkyBlockPlayer) sender).getPosition());

            CrystalCatalog.addCrystal(item.getComponent(SkullHeadComponent.class).getSkullTexture(item),
                    ((SkyBlockPlayer) sender).getPosition(),
                    type);
        }, itemType);
    }
}
