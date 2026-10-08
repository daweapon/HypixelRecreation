package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/** Crag's "Select Tier" menu (wiki Crag/UI): the five tiers at row 2, columns 3 to 7. */
public class GUICragSelectTier extends StatelessView {
    private static final int FIRST_TIER_SLOT = 11;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Select Tier", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        for (KuudraTier tier : KuudraTier.values()) {
            layout.slot(FIRST_TIER_SLOT + tier.ordinal(),
                    (_, c) -> tierItem((SkyBlockPlayer) c.player(), tier, true),
                    (_, c) -> c.push(new GUIKuudraTierQueue(tier)));
        }
    }

    static ItemStack.Builder tierItem(SkyBlockPlayer player, KuudraTier tier, boolean clickable) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        lore.add(Text.of("<7>Tier Requirements:"));
        for (KuudraProgress.Requirement requirement : KuudraProgress.requirements(player, tier)) {
            requirement.lore().forEach(line -> lore.add(Text.of(line)));
        }
        if (clickable) {
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to select!"));
        }
        return ItemStacks.head(tier.getKeyTexture(), Text.of("<b>{} Tier", tier.getDisplayName()), lore);
    }
}
