package dev.daweapon.type.skyblockgeneric.century;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;

public final class CakeSoul {
    private static final String SEPARATOR = "|";

    private CakeSoul() {
    }

    public static SkyBlockItem create(CakeSoulColour colour, @Nullable String owner) {
        SkyBlockItem soul = new SkyBlockItem(ItemType.CAKE_SOUL);
        soul.getAttributeHandler().setCakeSoul(encode(colour, owner));
        return soul;
    }

    public static String encode(CakeSoulColour colour, @Nullable String owner) {
        return colour.name() + SEPARATOR + (owner == null ? "" : owner);
    }

    public static @Nullable CakeSoulColour colour(SkyBlockItem item) {
        return colourOf(item.getAttributeHandler().getCakeSoul());
    }

    public static @Nullable String owner(SkyBlockItem item) {
        return ownerOf(item.getAttributeHandler().getCakeSoul());
    }

    static @Nullable CakeSoulColour colourOf(@Nullable String data) {
        if (data == null || data.isEmpty()) return null;
        int split = data.indexOf(SEPARATOR);
        return CakeSoulColour.parse(split < 0 ? data : data.substring(0, split));
    }

    static @Nullable String ownerOf(@Nullable String data) {
        if (data == null) return null;
        int split = data.indexOf(SEPARATOR);
        if (split < 0 || split == data.length() - 1) return null;
        return data.substring(split + 1);
    }
}
