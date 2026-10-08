package net.swofty.type.skyblockgeneric.crimson.quest;

import lombok.Getter;
import net.minestom.server.item.Material;

/**
 * The six Faction Quest categories. The category name titles the quest's Town Board page, and the chat word
 * fills "You completed your &lt;word&gt; quest!" (SkyHanni's captured lines use "Dojo" and "rescue").
 */
@Getter
public enum FactionQuestType {
    FISHING("Fishing", "fishing", Material.FISHING_ROD),
    FETCH("Fetch", "fetch", Material.CHEST),
    MINIBOSS("Miniboss", "miniboss", Material.IRON_SWORD),
    RESCUE("Rescue", "rescue", Material.PAINTING),
    DOJO("Dojo", "Dojo", Material.STICK),
    KUUDRA("Kuudra", "Kuudra", Material.MAGMA_CREAM),
    ;

    private final String categoryName;
    private final String chatWord;
    private final Material icon;

    FactionQuestType(String categoryName, String chatWord, Material icon) {
        this.categoryName = categoryName;
        this.chatWord = chatWord;
        this.icon = icon;
    }
}
