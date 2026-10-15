package dev.daweapon.type.thegarden.tab;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GardenInfoModule extends TablistModule {
    private static final List<ItemType> COLLECTIONS = List.of(ItemType.CARROT, ItemType.POTATO, ItemType.WHEAT);

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        Locale l = player.getLocale();
        GardenProfile garden = Gardens.profile(player);
        int level = GardenProgress.level(garden);

        List<TablistEntry> entries = new ArrayList<>(List.of(
                centered("<b><l>Info", l, TablistSkinRegistry.CYAN)
        ));
        entries.add(new TablistEntry(Text.key("tablist.server_info.area.garden"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.server_info.server_label", HypixelConst.getServerName()), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.gems", StringUtility.commaify(player.getGems())), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.level",
                level > 0 ? StringUtility.getAsRomanNumeral(level) : "0"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.copper", StringUtility.commaify(Math.floor(garden.getCopper()))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.garden.sowdust", StringUtility.commaify(Math.floor(garden.getSowdust()))), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());
        entries.add(new TablistEntry(Text.of("<a><l>Collection:"), TablistSkinRegistry.GRAY));
        for (ItemType type : COLLECTIONS) {
            entries.add(new TablistEntry(Text.of(" <7>" + name(type) + " " + collectionLevel(player, type)), TablistSkinRegistry.GRAY));
        }
        entries.add(getGrayEntry());
        entries.addAll(GardenComposterModule.entries(player));
        fillRestWithGray(entries);
        return entries;
    }

    private static String name(ItemType type) {
        return StringUtility.toNormalCase(type.name());
    }

    private static String collectionLevel(SkyBlockPlayer player, ItemType type) {
        CollectionCategory category = CollectionCategories.getCategory(type);
        if (category == null) return "<7>-";
        CollectionCategory.ItemCollection collection = category.getCollection(type);
        int collected = player.getCollection().get(type);
        int level = (int) Arrays.stream(collection.rewards()).filter(reward -> collected >= reward.requirement()).count();
        if (level >= collection.rewards().length) return "<c><l>MAX";
        return "<a>" + (level > 0 ? StringUtility.getAsRomanNumeral(level) : "0");
    }
}
