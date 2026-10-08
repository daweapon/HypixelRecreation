package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraCollectionTier;

import java.util.ArrayList;
import java.util.List;

/** The Boss Collections page, which only has Kuudra so far. Shown as a catalogue until the fight exists. */
public class GUIKuudraCollection extends StatelessView {
    private static final String KUUDRA_HEAD = "5051c83d9ebf69013f1ec8c9efc979ec2d925a921cc877ff64abe09aadd2f6cc";
    private static final int FIRST_TIER_SLOT = 20;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Boss Collections", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (_, _) -> ItemStacks.head(KUUDRA_HEAD, Text.of("<c>Kuudra"), List.of(
                Text.of("<7>Defeat <c>Kuudra <7>to progress this"),
                Text.of("<7>collection. Higher tiers count for more."),
                Text.empty(),
                Text.of("<7>Progress: <e>0"),
                Text.empty(),
                Text.of(KuudraAvailability.UNAVAILABLE_LORE))));

        for (KuudraCollectionTier tier : KuudraCollectionTier.values()) {
            layout.slot(FIRST_TIER_SLOT + tier.ordinal(), (_, _) -> {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>Progress: <e>0<6>/<e>{}", StringUtility.commaify(tier.getRequiredProgress())));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Rewards:"));
                tier.getRewards().forEach(reward -> lore.add(Text.of("  " + reward)));
                lore.add(Text.of("  <8>+<b>{} SkyBlock XP", tier.getSkyBlockXP()));
                lore.add(Text.empty());
                lore.add(Text.of(KuudraAvailability.UNAVAILABLE_LORE));
                return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, Text.of("<c>Kuudra {}", tier.name()), lore);
            });
        }
    }
}
