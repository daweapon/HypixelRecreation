package dev.daweapon.type.crimsonisle.town;

import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionHouse;
import dev.daweapon.type.skyblockgeneric.gui.inventories.banker.GUIBanker;
import dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar.GUIBazaar;
import dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar.GUISpecialBazaar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Bank, Auction House and Bazaar menus behind the Scarleton and Dragontail agents, gated as the Hub's are. */
public final class TownServices {
    public static final int BAZAAR_LEVEL = 7;

    private TownServices() {
    }

    public static void openBank(SkyBlockPlayer player) {
        new GUIBanker().open(player);
    }

    public static void openAuctionHouse(SkyBlockPlayer player) {
        new GUIAuctionHouse().open(player);
    }

    public static void openBazaar(SkyBlockPlayer player, String speaker) {
        if (player.getSkyBlockExperience().getLevel().asInt() < BAZAAR_LEVEL && !player.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            CrimsonScene.create().say(speaker, "<c>You need SkyBlock Level " + BAZAAR_LEVEL + " to access this feature!").play(player);
            return;
        }
        if (player.isIronman()) {
            player.openView(new GUISpecialBazaar());
            return;
        }
        new GUIBazaar(BazaarCategories.FARMING).open(player);
    }
}
