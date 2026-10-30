package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

@Getter
public enum CorpseType {
    LAPIS("Lapis", "<9>", ItemType.LAPIS_ARMOR_HELMET),
    UMBER("Umber", "<6>", ItemType.ARMOR_OF_YOG_HELMET),
    TUNGSTEN("Tungsten", "<7>", ItemType.MINERAL_HELMET),
    VANGUARD("Vanguard", "<f>", null);

    private final String displayName;
    private final String colour;
    private final @Nullable ItemType helmet;

    CorpseType(String displayName, String colour, @Nullable ItemType helmet) {
        this.displayName = displayName;
        this.colour = colour;
        this.helmet = helmet;
    }

    public String coloured() {
        return colour + displayName;
    }

    public ItemStack helmetStack() {
        if (helmet == null) return ItemStack.of(Material.IRON_HELMET);
        return new dev.daweapon.type.skyblockgeneric.item.SkyBlockItem(helmet).getItemStack();
    }
}
