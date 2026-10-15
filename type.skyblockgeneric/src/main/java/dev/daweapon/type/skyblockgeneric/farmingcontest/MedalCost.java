package dev.daweapon.type.skyblockgeneric.farmingcontest;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A price at Anita: medals from the profile's medal inventory plus Jacob's Tickets. */
public record MedalCost(int bronze, int silver, int gold, int tickets) {

    public static MedalCost gold(int gold, int tickets) {
        return new MedalCost(0, 0, gold, tickets);
    }

    public static MedalCost silver(int silver, int tickets) {
        return new MedalCost(0, silver, 0, tickets);
    }

    public static MedalCost bronze(int bronze, int tickets) {
        return new MedalCost(bronze, 0, 0, tickets);
    }

    public boolean affordable(SkyBlockPlayer player, FarmingContestProfile profile) {
        return profile.getBronzeMedals() >= bronze && profile.getSilverMedals() >= silver
                && profile.getGoldMedals() >= gold && tickets(player) >= tickets;
    }

    public void pay(SkyBlockPlayer player, FarmingContestProfile profile) {
        profile.addMedals(ContestBracket.BRONZE, -bronze);
        profile.addMedals(ContestBracket.SILVER, -silver);
        profile.addMedals(ContestBracket.GOLD, -gold);
        if (tickets > 0) player.removeItemsFromPlayer(Map.of(ItemType.JACOBS_TICKET, tickets));
    }

    public List<String> lore() {
        List<String> lines = new ArrayList<>();
        lines.add("<7>Cost");
        if (gold > 0) lines.add("<6>Gold medal" + amount(gold));
        if (silver > 0) lines.add("<f>Silver medal" + amount(silver));
        if (bronze > 0) lines.add("<c>Bronze medal" + amount(bronze));
        if (tickets > 0) lines.add("<a>Jacob's Ticket" + amount(tickets));
        return lines;
    }

    private static String amount(int amount) {
        return amount > 1 ? " <8>x" + StringUtility.commaify(amount) : "";
    }

    public static int tickets(SkyBlockPlayer player) {
        return player.getAmountInInventory(ItemType.JACOBS_TICKET) + player.getSackItems().getAmount(ItemType.JACOBS_TICKET);
    }
}
