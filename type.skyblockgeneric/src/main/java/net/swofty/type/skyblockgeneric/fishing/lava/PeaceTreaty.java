package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.regex.Pattern;

/**
 * Trophy Hunter armor's "Peace Treaty" (wiki: Bronze_Hunter_Armor): wearing 2+ pieces of any Hunter tier,
 * you can't catch Sea Creatures, they can't hurt you and you can't hurt them.
 */
public final class PeaceTreaty {
    public static final int PIECES = 2;
    private static final Pattern HUNTER_PIECE = Pattern.compile("^(BRONZE|SILVER|GOLD|DIAMOND)_HUNTER_(HELMET|CHESTPLATE|LEGGINGS|BOOTS)$");

    private PeaceTreaty() {
    }

    public static int hunterPieces(SkyBlockPlayer player) {
        int pieces = 0;
        for (SkyBlockItem piece : player.getArmor()) {
            ItemType type = piece.getAttributeHandler().getPotentialType();
            if (type != null && HUNTER_PIECE.matcher(type.name()).matches()) pieces++;
        }
        return pieces;
    }

    public static boolean applies(SkyBlockPlayer player) {
        return hunterPieces(player) >= PIECES;
    }
}
