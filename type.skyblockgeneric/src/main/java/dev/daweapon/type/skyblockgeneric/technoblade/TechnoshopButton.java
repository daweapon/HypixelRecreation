package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class TechnoshopButton {
    public static final int SLOT = 13;
    private static final String TEXTURE = "253f3a1ceae0f9a98320fa21bf2162c5ce692933b6205c47286d78daa2e20732";

    private TechnoshopButton() {
    }

    public static void layout(ViewLayout<DefaultState> layout) {
        layout.slot(SLOT, (state, ctx) -> ItemStacks.head(TEXTURE, Text.of("<d>Technoshop"), List.of(
                Text.of("<7>Pigs love shiny things, and so"),
                Text.of("<7>does the Blood God."),
                Text.empty(),
                TechnobladeShrine.technoshopOpen() ? Text.of("<e>Click to browse!") : Text.of("<c>The Technoshop is closed!"))),
                (click, ctx) -> TechnoshopView.open((SkyBlockPlayer) ctx.player()));
    }
}
