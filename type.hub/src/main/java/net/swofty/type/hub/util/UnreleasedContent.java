package net.swofty.type.hub.util;

import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.ViewLayout;

import java.util.List;

public enum UnreleasedContent {
    DUNGEONS("<c>The Catacombs are not available on this server yet!"),
    RIFT("<c>The Rift is not available on this server yet!"),
    DYES("<c>Dyes are not obtainable on this server yet!"),
    COSMETICS("<c>Taylor's cosmetics are not available on this server yet!");

    private final String message;

    UnreleasedContent(String message) {
        this.message = message;
    }

    public Text message() {
        return Text.of(message);
    }

    public List<Text> loreLines() {
        return List.of(Text.empty(), Text.of(message));
    }

    public <S> void slot(ViewLayout<S> layout, int slot, Material material, String textBlock) {
        layout.slot(slot, ItemStacks.appendLore(ItemStacks.item(material, textBlock), loreLines()),
                (click, ctx) -> ctx.player().sendMessage(message()));
    }
}
