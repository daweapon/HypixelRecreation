package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.collection.CollectionNames;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCollection;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class CollectionsWidget implements TabWidget {
    private static final int FALLBACK_COUNT = 5;

    @Override
    public String id() {
        return "collections";
    }

    @Override
    public String displayName() {
        return "Collections";
    }

    @Override
    public Material icon() {
        return Material.PAINTING;
    }

    @Override
    public int defaultPriority() {
        return 80;
    }

    @Override
    public boolean enabledByDefault(TabLocation location) {
        return false;
    }

    @Override
    public List<TabWidgetToggle> toggles() {
        return allCollections().stream()
                .map(collection -> new TabWidgetToggle(collection.type().name(), CollectionNames.displayName(collection.type()), false))
                .toList();
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        DatapointCollection.PlayerCollection owned = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class).getValue();
        List<CollectionCategory.ItemCollection> chosen = allCollections().stream()
                .filter(collection -> context.shows(this, collection.type().name()))
                .toList();
        if (chosen.isEmpty()) {
            chosen = allCollections().stream()
                    .filter(collection -> owned.get(collection.type()) > 0)
                    .sorted(Comparator.comparingInt((CollectionCategory.ItemCollection collection) -> owned.get(collection.type())).reversed())
                    .limit(FALLBACK_COUNT)
                    .toList();
        }
        if (chosen.isEmpty()) return List.of();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<3><l>Collections:"));
        for (CollectionCategory.ItemCollection collection : chosen) {
            lines.add(line(collection, owned.get(collection.type())));
        }
        return lines;
    }

    private static Text line(CollectionCategory.ItemCollection collection, int collected) {
        String name = CollectionNames.displayName(collection.type());
        int tier = 0;
        for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
            if (collected >= reward.requirement()) tier++;
        }
        if (tier >= collection.rewards().length) return Text.of(" <f>{}: <a>MAX", name);
        double percent = collected * 100.0 / collection.rewards()[tier].requirement();
        return Text.of(" <f>{} {}: <e>{}%", name, tier + 1, String.format("%.1f", Math.min(100, percent)));
    }

    private static List<CollectionCategory.ItemCollection> allCollections() {
        return CollectionCategories.getCategories().stream()
                .flatMap(category -> Arrays.stream(category.getCollections()))
                .toList();
    }
}
