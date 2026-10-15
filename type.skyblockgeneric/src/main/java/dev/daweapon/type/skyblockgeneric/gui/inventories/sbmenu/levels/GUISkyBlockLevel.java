package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelUnlock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// GUI showing the unlocks granted at one SkyBlock level
public class GUISkyBlockLevel extends StatelessView {
    private static final Map<Integer, List<Integer>> SLOTS_MAP = new HashMap<>(
            Map.of(
                    1, List.of(13),
                    2, List.of(12, 14),
                    3, List.of(11, 13, 15),
                    4, List.of(10, 12, 14, 16),
                    5, List.of(11, 12, 13, 14, 15)
            )
    );

    private final SkyBlockLevelRequirement levelRequirement;

    public GUISkyBlockLevel(SkyBlockLevelRequirement levelRequirement) {
        this.levelRequirement = levelRequirement;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText(
            (state, ctx) -> Text.key("gui_sbmenu.levels.level.title", levelRequirement.asInt()),
                InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        List<SkyBlockLevelUnlock> unlocks = levelRequirement.getUnlocks();
        List<Integer> slots = SLOTS_MAP.get(unlocks.size());

        if (slots != null) {
            for (int i = 0; i < unlocks.size(); i++) {
                SkyBlockLevelUnlock unlock = unlocks.get(i);
                int slot = slots.get(i);

                layout.slot(slot, (s, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    return unlock.getItemDisplay(player, levelRequirement.asInt());
                });
            }
        }
    }
}
