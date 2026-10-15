package dev.daweapon.type.skyblockgeneric.crimson.town;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

/** The crops Carrolyn exports from Scarleton (wiki Carrolyn/UI), in menu order. */
@Getter
@Accessors(fluent = true)
public enum CarrolynExport {
    MUSHROOM(Material.RED_MUSHROOM, "HALF_EATEN_MUSHROOM", "<a>Half-Eaten Mushroom", "Mushrooms", "MUSHROOMS",
            ItemStatistic.MUSHROOM_FORTUNE, "Thank you for the <c>mushrooms<f>.",
            "Although since they're all half-eaten, you technically brought me half the amount!"),
    WHEAT(Material.WHEAT, "FINE_FLOUR", "<a>Fine Flour", "Fine Flours", "FINE FLOURS", ItemStatistic.WHEAT_FORTUNE,
            "Thank you for the <6>flour<f>."),
    CACTUS(Material.CACTUS, "POTTED_CACTUS", "<a>Potted Cactus", "Cactus", "CACTUS", ItemStatistic.CACTUS_FORTUNE,
            "Thank you for the <6>cacti<f>.", "They're cute but prickly, like my grandmother."),
    WILD_ROSE(Material.ROSE_BUSH, "PRICKLY_KISS", "<a>Prickly Kiss", "Prickly Kisses", "PRICKLY KISSES",
            ItemStatistic.WILD_ROSE_FORTUNE, "Thank you for the <c>Prickly Kisses<f>.",
            "I hope you handle these with care. I got pricked by those once.", "It was NOT a pleasant experience."),
    CARROT(Material.CARROT, "EXPORTABLE_CARROTS", "<a>Exportable Carrots", "Carrots", "CARROTS", ItemStatistic.CARROT_FORTUNE,
            "Thank you for the <6>carrots<f>.", "I don't know where you got them from and at this point I'm too afraid to ask."),
    NETHER_WART(Material.NETHER_WART, "WARTY", "<5>Warty", "Wartys", "WARTY", ItemStatistic.NETHER_WART_FORTUNE,
            "Thank you for the <5>Warty<f>."),
    COCOA_BEANS(Material.COCOA_BEANS, "SUPREME_CHOCOLATE_BAR", "<a>Supreme Chocolate Bar", "Chocolate Bars", "CHOCOLATE BARS",
            ItemStatistic.COCOA_BEANS_FORTUNE, "Thank you for the <6>chocolate<f>.",
            "It is absolutely delicious. By far the best chocolate bars I have ever eaten in my life!"),
    PUMPKIN(Material.PUMPKIN, "EXPIRED_PUMPKIN", "<a>Expired Pumpkin", "Pumpkins", "PUMPKINS", ItemStatistic.PUMPKIN_FORTUNE,
            "Thank you for the <6>pumpkins<f>.", "They're squishy and disgusting. I don't want to know where you got these from."),
    ;

    private final Material icon;
    private final String itemId;
    private final String displayName;
    private final String progressWord;
    private final String completeTitle;
    private final ItemStatistic fortune;
    private final String[] thanks;

    CarrolynExport(Material icon, String itemId, String displayName, String progressWord, String completeTitle,
                   ItemStatistic fortune, String... thanks) {
        this.icon = icon;
        this.itemId = itemId;
        this.displayName = displayName;
        this.progressWord = progressWord;
        this.completeTitle = completeTitle;
        this.fortune = fortune;
        this.thanks = thanks;
    }

    /** The crop item, or null while it does not exist on this server yet. */
    public @Nullable ItemType itemType() {
        return ItemType.get(itemId);
    }
}
