package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Hypixel splits Skill Related Tasks into one screen per Skill rather than listing them flat.
public class GUIXPSubAreas implements StatefulView<GUIXPSubAreas.State> {

    public record State() {
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Skill Related Tasks", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(SkyBlockXPSource.Group.SKILL_RELATED.getFiller()));
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockXPSource.SubArea[] subAreas = SkyBlockXPSource.SubArea.values();
        int[] slots = XPTaskLayout.slots(subAreas.length, 1, 4);
        for (int i = 0; i < slots.length; i++) {
            SkyBlockXPSource.SubArea subArea = subAreas[i];
            layout.slot(slots[i],
                    (s, c) -> subAreaItem(subArea, (SkyBlockPlayer) c.player()),
                    (click, c) -> GUIXPSourceGroup.open(c, subArea));
        }
    }

    private ItemStack.Builder subAreaItem(SkyBlockXPSource.SubArea subArea, SkyBlockPlayer player) {
        List<SkyBlockXPSource> tasks = SkyBlockXPSource.getBySubArea(subArea);
        double earned = tasks.stream().mapToDouble(player.getSkyBlockExperience()::getSourceXP).sum();
        int total = SkyBlockXPSource.getTotalXPFor(subArea);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{} Tasks", tasks.size()));
        lore.add(Text.empty());
        subArea.getDescription().forEach(line -> lore.add(Text.of("<7>{}", line)));
        lore.add(Text.empty());

        for (SkyBlockXPSource task : tasks) {
            lore.add(Text.of("<e>▶ <e>{}", task.getDisplayName()));
            lore.add(GUIXPSources.taskBar(player.getSkyBlockExperience().getSourceXP(task), task.getMaxXP()));
        }

        lore.add(Text.empty());
        lore.addAll(GUIXPSources.progressBar(earned, total));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view tasks!"));

        return ItemStacks.item(subArea.getIcon(), 1,
                Text.of("<b>{} Tasks", subArea.getDisplayName()), lore);
    }
}
