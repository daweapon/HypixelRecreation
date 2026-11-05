package dev.daweapon.type.catacombs.secret;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Hypixel's lines for secrets, as client mods match them. */
public final class SecretMessages {
    public static final String CHEST_SEARCHED = "<c>This chest has already been searched!";
    public static final String REDSTONE_KEY = "<e>You found a Secret Redstone Key!";
    private static final String ESSENCE = "found a <d>Wither Essence<f>! Everyone gains an extra essence!";

    private SecretMessages() {
    }

    public static Text obtained(SkyBlockPlayer player, ItemType item) {
        return Text.of("{} <e>has obtained {}<e>!", player.getColouredName(), new SkyBlockItem(item).getDisplayNameText());
    }

    public static String essenceFoundByYou() {
        return "<f>You " + ESSENCE;
    }

    public static Text essenceFoundBy(SkyBlockPlayer finder) {
        return Text.of("{} <f>" + ESSENCE, finder.getColouredName());
    }

    public static String roomSecrets(int found, int total) {
        return "<7>" + found + "/" + total + " Secrets";
    }
}
