package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.levels.LevelsGuide;

import java.util.ArrayList;
import java.util.List;

/**
 * The guide's tiers, from New Player through End Game.
 *
 * Only Starter was reachable before - the menu opened it directly, so the other six tiers
 * existed with no way in.
 */
public class GUIGuideTiers extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("SkyBlock Guide", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        LevelsGuide[] guides = LevelsGuide.values();
        for (int i = 0; i < SLOTS.length && i < guides.length; i++) {
            LevelsGuide guide = guides[i];
            boolean built = !guide.getTasksSets().isEmpty();

            layout.slot(SLOTS[i], (s, c) -> {
                List<Text> lore = new ArrayList<>();
                lore.add(guide.getTitle());
                lore.add(Text.empty());
                lore.add(guide.getDescription());
                lore.add(Text.empty());
                if (built) {
                    lore.add(Text.of("<7>Tasks: <a>{}", guide.getTasksSets().size()));
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Click to view!"));
                } else {
                    lore.add(Text.of("<c>Not in the game yet."));
                    lore.add(Text.of("<8>Listed for parity with Hypixel."));
                }

                return ItemStacks.item(guide.getGlassMaterial(), 1,
                        Text.of((built ? "<a>" : "<7>") + "{}", StringUtility.toNormalCase(guide.name())), lore);
            }, (click, c) -> {
                if (built) c.push(new GUILevelsGuide(guide));
            });
        }
    }
}
