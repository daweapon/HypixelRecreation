package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraCollectionTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/** The Boss Collections page, which only has Kuudra so far: total kill count and the five collection tiers. */
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

        long points = KuudraProgress.collectionPoints((SkyBlockPlayer) ctx.player());
        KuudraCollectionTier highest = KuudraCollectionTier.highestReached(points);

        layout.slot(4, (_, _) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Defeat <c>Kuudra <7>to progress this"));
            lore.add(Text.of("<7>collection."));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Total Kills: <e>{}", StringUtility.commaify(points)));
            lore.add(Text.of("<7>Collection Tier: <e>{}", highest == null ? "0" : highest.name()));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Higher Tier kills reward more Kill Count:"));
            for (KuudraTier tier : KuudraTier.values()) {
                lore.add(Text.of("<7> - {} Tier: <a>+{} Kill Count", tier.getDisplayName(), KuudraCollectionTier.pointsFor(tier)));
            }
            KuudraAvailability.lore().forEach(line -> lore.add(line.isEmpty() ? Text.empty() : Text.of(line)));
            return ItemStacks.head(KUUDRA_HEAD, Text.of("<c>Kuudra"), lore);
        });

        for (KuudraCollectionTier tier : KuudraCollectionTier.values()) {
            boolean unlocked = points >= tier.getRequiredProgress();
            layout.slot(FIRST_TIER_SLOT + tier.ordinal(), (_, _) -> {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>Progress: <e>{}<6>/<e>{}", StringUtility.commaify(Math.min(points, tier.getRequiredProgress())),
                        StringUtility.commaify(tier.getRequiredProgress())));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Rewards:"));
                tier.getRewards().forEach(reward -> lore.add(Text.of("  " + reward)));
                lore.add(Text.of("  <8>+<b>{} SkyBlock XP", tier.getSkyBlockXP()));
                lore.add(Text.empty());
                lore.add(Text.of(unlocked ? "<a>UNLOCKED" : "<c>LOCKED"));
                return ItemStacks.item(unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        Text.of((unlocked ? "<a>" : "<c>") + "Kuudra {}", tier.name()), lore);
            });
        }
    }
}
