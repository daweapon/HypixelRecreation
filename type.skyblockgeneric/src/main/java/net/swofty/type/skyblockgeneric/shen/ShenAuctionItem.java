package net.swofty.type.skyblockgeneric.shen;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Everything Shen auctions (wiki Shen's Auction). {@code winners} is how many top bidders win on a
 * Classic profile without Diaz; Ironman profiles bid in their own auction with the Ironman count.
 * Diaz's Volume Trading doubles the Shen's Auction and Shen's Special counts and adds two of the
 * Volume Trading items, whose counts already assume Diaz.
 */
@Getter
public enum ShenAuctionItem {
    ARTIFACT_OF_CONTROL(Section.AUCTION, "<6>Artifact of Control", 20, 10, Material.PLAYER_HEAD,
            "31f748a43f3b3ada04f44d5d290a8b9bf583d93e1c83ab93c60c4dec1fde1c5c", null),
    PANDORAS_BOX(Section.AUCTION, "<f>Pandora's Box", 40, 20, Material.PLAYER_HEAD,
            "fe246925cbd4c05279b9c8dcf7c2fdfc9baf9b1424aa35e501533ebb98e00522",
            "Pandora's Box isn't in this server yet."),
    SHENS_REGALIA(Section.AUCTION, "<6>Shen's Regalia", 80, 40, Material.PLAYER_HEAD,
            "9c7fb6f414ff0eb6bf5797acb3b3af354ca3af95ceb8f3418125f6b805419f39",
            "Shen's Regalia and the Regalia Room aren't in this server yet."),

    PRICELESS_THE_FISH(Section.SPECIAL, "<c>Priceless the Fish", 5, 2, Material.PLAYER_HEAD,
            "3abe8783494d113fd71988c20ec141c5ab8df7a368a4b74950df91f6ee24315e",
            "Priceless the Fish isn't in this server yet."),
    GIANT_FISHING_ROD(Section.SPECIAL, "<6>Giant Fishing Rod", 10, 5, Material.FISHING_ROD, null, null),
    CLOVER_HELMET(Section.SPECIAL, "<6>Clover Helmet", 10, 5, Material.PLAYER_HEAD,
            "598949f4d546bd504376f79f8425b57ecf77be672976ee82aef5c86ec0db5d49",
            "The Clover Helmet isn't in this server yet."),
    RACING_HELMET(Section.SPECIAL, "<6>Racing Helmet", 10, 5, Material.PLAYER_HEAD,
            "2ce0746b12ee4053958e140ba29593272bd84ca34baf50d0d80b5bc3c615e9c6",
            "The Racing Helmet isn't in this server yet."),
    DIRT_ROD(Section.SPECIAL, "<a>Dirt Rod", 15, 7, Material.FISHING_ROD, null, null),
    WIZARD_WAND(Section.SPECIAL, "<5>Wizard Wand", 20, 10, Material.BLAZE_ROD, null, null),

    HOE_OF_NO_TILLING(Section.VOLUME_TRADING, "<a>Hoe of No Tilling", 20, 10, Material.GOLDEN_HOE, null,
            "The Hoe of No Tilling isn't in this server yet."),
    KNUCKLE_SANDWICH(Section.VOLUME_TRADING, "<6>Knuckle Sandwich", 20, 10, Material.PAPER, null, null),
    PUZZLE_CUBE(Section.VOLUME_TRADING, "<6>Puzzle Cube", 20, 10, Material.PLAYER_HEAD,
            "243178db2a9f5af604fda72dc4f83e754fcc8fe9ca034e6227268a90eb1e70d7",
            "The Puzzle Cube isn't in this server yet."),
    SHIMMERSPARKLE_CHESTPLATE(Section.VOLUME_TRADING, "<6>Shimmersparkle Chestplate", 20, 10, Material.LEATHER_CHESTPLATE, null,
            "The Shimmersparkle Chestplate isn't in this server yet."),
    SPELLING_STICK(Section.VOLUME_TRADING, "<6>Spelling Stick", 20, 10, Material.STICK, null,
            "The Spelling Stick isn't in this server yet.");

    private final Section section;
    private final String displayName;
    private final int winners;
    private final int ironmanWinners;
    private final Material icon;
    private final @Nullable String headTexture;
    private final @Nullable String unavailableReason;

    ShenAuctionItem(Section section, String displayName, int winners, int ironmanWinners, Material icon,
                    @Nullable String headTexture, @Nullable String unavailableReason) {
        this.section = section;
        this.displayName = displayName;
        this.winners = winners;
        this.ironmanWinners = ironmanWinners;
        this.icon = icon;
        this.headTexture = headTexture;
        this.unavailableReason = unavailableReason;
    }

    public boolean isAvailable() {
        return unavailableReason == null && itemType() != null;
    }

    public @Nullable ItemType itemType() {
        try {
            return ItemType.valueOf(name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int winners(boolean ironman, boolean volumeTrading) {
        int base = ironman ? ironmanWinners : winners;
        return volumeTrading && section != Section.VOLUME_TRADING ? base * 2 : base;
    }

    public static List<ShenAuctionItem> of(Section section) {
        return Arrays.stream(values()).filter(item -> item.section == section).toList();
    }

    public enum Section {
        AUCTION,
        SPECIAL,
        VOLUME_TRADING
    }
}
