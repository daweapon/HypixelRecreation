package net.swofty.type.skyblockgeneric.carnival;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;

@Getter
public enum CarnivalGame {
    FRUIT_DIGGING("Fruit Digging", "<6>", Material.SAND, ItemType.CARNIVAL_SHOVEL),
    CATCH_A_FISH("Catch a Fish", "<3>", Material.COD, ItemType.CARNIVAL_ROD),
    ZOMBIE_SHOOTOUT("Zombie Shootout", "<c>", Material.ARROW, ItemType.CARNIVAL_DART_TUBE);

    private final String displayName;
    private final String color;
    private final Material icon;
    private final ItemType tool;

    CarnivalGame(String displayName, String color, Material icon, ItemType tool) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.tool = tool;
    }

    public String colored() {
        return color + displayName;
    }
}
