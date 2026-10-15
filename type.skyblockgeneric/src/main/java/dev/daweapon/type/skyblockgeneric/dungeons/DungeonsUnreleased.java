package dev.daweapon.type.skyblockgeneric.dungeons;

import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.ViewLayout;

import java.util.List;

/** The part of the Catacombs that still isn't in the server: classes, Master Mode, the profile and rules menus. */
public final class DungeonsUnreleased {
    public static final String MESSAGE = "<c>This part of The Catacombs is not available on this server yet!";

    private DungeonsUnreleased() {
    }

    public static Text message() {
        return Text.of(MESSAGE);
    }

    public static List<Text> loreLines() {
        return List.of(Text.empty(), Text.of(MESSAGE));
    }

    public static <S> void slot(ViewLayout<S> layout, int slot, Material material, String textBlock) {
        layout.slot(slot, ItemStacks.appendLore(ItemStacks.item(material, textBlock), loreLines()),
                (click, ctx) -> ctx.player().sendMessage(message()));
    }
}
