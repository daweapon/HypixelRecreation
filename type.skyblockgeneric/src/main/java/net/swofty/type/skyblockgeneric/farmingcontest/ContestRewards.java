package net.swofty.type.skyblockgeneric.farmingcontest;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.elections.FameRank;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Hands out a bracket's non-medal rewards. Each medal bracket also awards a Turbo-Crop book for
 * the contest crop on Hypixel; farming enchantments don't exist on this server yet, so that line
 * is shown greyed out instead.
 */
public final class ContestRewards {
    public static final String TURBO_CROP_UNAVAILABLE = "Turbo-Crop books aren't in this server yet.";

    private ContestRewards() {}

    public static void grant(SkyBlockPlayer player, ContestBracket bracket) {
        player.addAndUpdateItem(new SkyBlockItem(ItemType.JACOBS_TICKET, bracket.getJacobsTickets()));
        if (bracket.getCarnivalTickets() > 0) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.CARNIVAL_TICKET, bracket.getCarnivalTickets()));
        }
        int bits = bits(player, bracket);
        if (bits > 0) player.addBits(bits);
    }

    public static int bits(SkyBlockPlayer player, ContestBracket bracket) {
        return (int) Math.round(bracket.getBits() * FameRank.forFame(player.getFame()).getBitsMultiplier());
    }

    public static List<String> lore(ContestBracket bracket, ContestCrop crop) {
        List<String> lines = new ArrayList<>();
        if (bracket.getGoldMedals() > 0) lines.add("<8>+ <6>" + bracket.getGoldMedals() + "x <l>GOLD<r> <7>medal");
        if (bracket.getSilverMedals() > 0) lines.add("<8>+ <f>" + bracket.getSilverMedals() + "x <l>SILVER<r> <7>medal");
        if (bracket.getBronzeMedals() > 0) lines.add("<8>+ <c>" + bracket.getBronzeMedals() + "x <l>BRONZE<r> <7>medal");
        lines.add("<8>+ <a>" + bracket.getJacobsTickets() + "x Jacob's Ticket");
        if (bracket.getCarnivalTickets() > 0) lines.add("<8>+ <a>" + bracket.getCarnivalTickets() + "x Carnival Ticket");
        if (bracket.earnsMedal()) lines.add("<8>+ <8><m>Enchanted Book (Turbo-" + crop.getDisplayName() + " I)");
        if (bracket.getBits() > 0) lines.add("<8>+ <b>" + bracket.getBits() + " Bits <8>(before Fame multiplier)");
        return lines;
    }
}
