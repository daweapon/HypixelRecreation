package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.farming.animal.FarmAnimal;
import dev.daweapon.type.skyblockgeneric.farming.jake.JakeDelivery;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class HeadOfBetrayal {
    private static final String COW_HAT_ID = "COW_HEAD";

    private HeadOfBetrayal() {
    }

    public static void award(SkyBlockPlayer talker, JakeDelivery.Delivery delivery, Pos jake) {
        if (!GreatSpookEvent.isActive() || delivery == null || delivery.animal() != FarmAnimal.COW) return;
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            boolean nearby = player == talker || (player.getInstance() == talker.getInstance()
                    && player.getPosition().distance(jake) <= SpookNpcIngredients.COW_HAT_RANGE);
            if (!nearby || !COW_HAT_ID.equals(WornArmor.id(player.getHelmet()))) continue;
            SkyBlockItem head = new SkyBlockItem(ItemType.BETRAYAL_HEAD);
            player.setHelmet(head.getItemStack());
            player.sendMessage("<a>Your Cow Hat turned into a <f>{}<a>!", head.getDisplayName());
        }
    }
}
