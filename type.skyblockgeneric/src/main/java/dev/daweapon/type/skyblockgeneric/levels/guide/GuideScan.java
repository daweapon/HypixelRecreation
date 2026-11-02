package dev.daweapon.type.skyblockgeneric.levels.guide;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.museum.MuseumCatalogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashSet;
import java.util.Set;

public final class GuideScan {
    private final SkyBlockPlayer player;
    private Set<String> museumEntries;
    private Set<ItemType> accessories;
    private Set<ItemType> pets;

    GuideScan(SkyBlockPlayer player) {
        this.player = player;
    }

    public SkyBlockPlayer player() {
        return player;
    }

    public Set<String> museumEntries() {
        if (museumEntries == null) museumEntries = player.getMuseumData().heldEntryIds(MuseumCatalogue.get());
        return museumEntries;
    }

    public Set<ItemType> accessories() {
        if (accessories == null) accessories = types(player.getAccessoryBag().getAllAccessories());
        return accessories;
    }

    public Set<ItemType> pets() {
        if (pets == null) pets = types(player.getPetData().getPetsMap().keySet());
        return pets;
    }

    private static Set<ItemType> types(Iterable<SkyBlockItem> items) {
        Set<ItemType> types = new HashSet<>();
        for (SkyBlockItem item : items) {
            ItemType type = item.getAttributeHandler().getPotentialType();
            if (type != null) types.add(type);
        }
        return types;
    }
}
