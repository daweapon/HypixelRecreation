package dev.daweapon.type.skyblockgeneric.shop.catalogue;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/** A gate on a catalogue entry, written in shop YAML under {@code requires:}; a locked entry shows its lock line. */
public interface ShopRequirement {
    boolean isMet(SkyBlockPlayer player);

    String lockLine();

    static @Nullable ShopRequirement parse(@Nullable Object raw) {
        ShopRequirement floor = CatacombsFloorRequirement.parse(raw);
        return floor != null ? floor : ReputationRequirement.parse(raw);
    }
}
