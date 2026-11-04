package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.levels.causes.CenturyEmblemCause;
import org.jetbrains.annotations.Nullable;

public enum RaffleMilestone {
    I(1, "I", 10_000, null, null, "<6>10,000 Coins"),
    II(10, "II", 0, null, CenturyEmblemCause.CELEBRANT, "<a>Century Celebrant Emblem <7>⛂"),
    III(25, "III", 0, "PET_SKIN_TURTLE_ANCIENT", null, "<d>Ancient Turtle Skin"),
    IV(50, "IV", 0, "CENTURY_TALISMAN", null, "<f>Talisman of the Century"),
    V(100, "V", 0, null, CenturyEmblemCause.PARTYGOER, "<a>Century Partygoer Emblem <6>⛂"),
    VI(200, "VI", 0, "GIFTBAG_OF_THE_CENTURY", null, "<5>Gift Bag of the Century"),
    VII(400, "VII", 0, "CENTURY_THE_FISH", null, "<c>Century the Fish");

    private final int tickets;
    private final String numeral;
    private final int coins;
    private final @Nullable String itemId;
    private final @Nullable String emblemKey;
    private final String rewardName;

    RaffleMilestone(int tickets, String numeral, int coins, @Nullable String itemId, @Nullable String emblemKey, String rewardName) {
        this.tickets = tickets;
        this.numeral = numeral;
        this.coins = coins;
        this.itemId = itemId;
        this.emblemKey = emblemKey;
        this.rewardName = rewardName;
    }

    public int tickets() {
        return tickets;
    }

    public String numeral() {
        return numeral;
    }

    public int coins() {
        return coins;
    }

    public @Nullable ItemType item() {
        return itemId == null ? null : ItemType.get(itemId);
    }

    public @Nullable String emblemKey() {
        return emblemKey;
    }

    public String rewardName() {
        return rewardName;
    }

    public int slices() {
        return ordinal() + 1;
    }

    public String field() {
        return RaffleProfile.MILESTONE_PREFIX + slices();
    }
}
