package dev.daweapon.type.crimsonisle.dojo;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** The menus Hypixel refuses during a Dojo challenge, keyed by the commands that open them here. */
public final class DojoBlockedCommands {
    private static final String TRAVEL = "<c>You can't do that during a challenge!";
    private static final Map<String, String> MESSAGES = new HashMap<>();

    static {
        block("<c>You cannot access the bazaar during a challenge.", "bazaar", "bazzar", "bz");
        block("<c>You cannot use the Auction House during a challenge!", "auctionhouse", "ah", "auctionview", "ahview");
        block("<c>You cannot access your pets here!", "pets", "petsmenu", "petmenu", "viewpetsmenu");
        block("<c>You cannot use the Recipe Book during a challenge!", "viewrecipe", "vr");
        block("<c>You cannot use the Collection Page during a challenge!", "viewcollection", "vc");
        block("<c>You cannot use the Your Skills Page during a challenge!", "viewskill", "vs");
        block("<c>You cannot use the Calendar and Events Page during a challenge!", "calendar");
        block("<c>You cannot open the wardrobe during a challenge!", "wardrobe", "wd");
        block(TRAVEL, "warp", "teleportregion", "hub", "h", "island", "is", "visit", "trade", "bank",
                "reforge", "reforgeitem", "etable", "et", "enchantingtable", "anvilgui", "chocolatefactory", "cf",
                "factory", "attributes", "am", "huntingbox", "hb", "slayer", "skyblockxp", "sbxp", "pickupstash");
    }

    private DojoBlockedCommands() {
    }

    private static void block(String message, String... labels) {
        for (String label : labels) MESSAGES.put(label, message);
    }

    public static @Nullable String messageFor(String commandLine) {
        String label = commandLine.strip().split(" ", 2)[0].toLowerCase();
        if (label.startsWith("/")) label = label.substring(1);
        return MESSAGES.get(label);
    }
}
