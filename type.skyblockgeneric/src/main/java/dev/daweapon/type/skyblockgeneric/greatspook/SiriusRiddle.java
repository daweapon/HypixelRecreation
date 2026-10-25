package dev.daweapon.type.skyblockgeneric.greatspook;

import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class SiriusRiddle {
    private SiriusRiddle() {
    }

    public static boolean eligible(SkyBlockPlayer player) {
        return GreatSpookEvent.isActive() && SpookNpcIngredients.exactSiriusPurse(player.getCoins());
    }

    public static void offer(SkyBlockPlayer player, Runnable accepted) {
        NPCOption.sendOption(player, "sirius", true, List.of(
                new NPCOption.Option("yes", NamedTextColor.GREEN, false, "Yes of course", _ -> accepted.run())));
    }

    public static void give(SkyBlockPlayer player) {
        SkyBlockItem book = new SkyBlockItem(ItemType.SIRIUS_BOOK);
        player.addAndUpdateItem(book);
        player.sendMessage("<a>You claimed a <5>{}<a>!", book.getDisplayName());
    }
}
