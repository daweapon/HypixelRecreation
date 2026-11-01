package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.emblem;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.generic.gui.v2.*;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockEmblems;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIEmblems extends StatelessView {
    private static final Map<SkyBlockEmblems, Integer> SLOTS = Map.of(
            SkyBlockEmblems.SKILLS, 11,
            SkyBlockEmblems.DUNGEONS, 12,
            SkyBlockEmblems.LEVELING, 13,
            SkyBlockEmblems.SLAYER, 14,
            SkyBlockEmblems.MISCELLANEOUS, 15,
            SkyBlockEmblems.SUBSCRIPTION, 21,
            SkyBlockEmblems.SPECIAL, 23
    );

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Emblems", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        for (SkyBlockEmblems emblem : SkyBlockEmblems.values()) {
            int slot = SLOTS.get(emblem);

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                String displayName = switch (emblem) {
                    case DUNGEONS -> "Catacombs";
                    case MISCELLANEOUS -> "Achievement";
                    default -> emblem.toString();
                };
                GUIMaterial guiMaterial = emblem.getGuiMaterial();

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<8>{} Unlocked", emblem.amountUnlocked(player)));
                lore.add(Text.literal(" "));
                lore.addAll(emblem.getDescription());
                lore.add(Text.literal(" "));
                lore.add(Text.of("<e>Click to view!"));

                return ItemStacks.of(guiMaterial, 1, Text.of("<a>{}", displayName), lore);
            }, (click, c) -> c.push(new GUIEmblem(emblem), GUIEmblem.createInitialState(emblem)));
        }
    }
}
