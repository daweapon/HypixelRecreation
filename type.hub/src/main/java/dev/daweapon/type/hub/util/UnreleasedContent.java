package dev.daweapon.type.hub.util;

import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonsUnreleased;

import java.util.List;

public enum UnreleasedContent {
    DUNGEONS(DungeonsUnreleased.MESSAGE),
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
