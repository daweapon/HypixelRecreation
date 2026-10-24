package dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneItemSettings;
import dev.daweapon.type.skyblockgeneric.abiphone.Ringtone;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public final class RingtoneView implements View<RingtoneView.State> {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16};

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.literal("Ringtones"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        Ringtone[] tones = Ringtone.values();
        for (int i = 0; i < tones.length; i++) {
            Ringtone tone = tones[i];
            boolean selected = tone == AbiphoneItemSettings.ringtone(state.abiphone());
            layout.slot(SLOTS[i], (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(selected ? Material.LIME_DYE : Material.GRAY_DYE), "<a>{}", tone.label), List.of(
                    Text.of(selected ? "<a>Selected!" : "<e>Click to select!"),
                    Text.of("<b>Right-click to preview!"))),
                    (click, viewCtx) -> {
                        SkyBlockPlayer player = (SkyBlockPlayer) viewCtx.player();
                        if (click.click() instanceof Click.Right) {
                            tone.play(player, 0);
                            return;
                        }
                        AbiphoneItemSettings.setRingtone(player, state.abiphone(), tone);
                        tone.play(player, 0);
                        viewCtx.session(Object.class).updateUnchecked(o -> o);
                    });
        }
        if (!Components.back(layout, 30, ctx)) Components.close(layout, 30);
        Components.close(layout, 31);
    }

    public record State(SkyBlockItem abiphone) {
    }
}
