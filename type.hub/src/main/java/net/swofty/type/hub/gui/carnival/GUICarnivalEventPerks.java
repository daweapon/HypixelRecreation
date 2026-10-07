package net.swofty.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerkEvent;
import net.swofty.type.skyblockgeneric.carnival.CarnivalProfile;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// One event's four perks in Doug's shop, from the wiki's Doug/UI subpage.
public class GUICarnivalEventPerks extends StatelessView {
    private static final int[] SLOTS = {11, 12, 14, 15};
    private final CarnivalPerkEvent event;

    public GUICarnivalEventPerks(CarnivalPerkEvent event) {
        this.event = event;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(event.getDisplayName(), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);
        List<CarnivalPerk> perks = CarnivalPerk.of(event);
        for (int i = 0; i < perks.size(); i++) {
            CarnivalPerk perk = perks.get(i);
            layout.slot(SLOTS[i], (_, c) -> perkItem(Carnivals.profile((SkyBlockPlayer) c.player()), perk), (_, c) -> {
                buy((SkyBlockPlayer) c.player(), perk);
                c.session(DefaultState.class).refresh();
            });
        }
        layout.slot(32, (_, c) -> CarnivalTokensDisplay.of((SkyBlockPlayer) c.player()));
    }

    private static ItemStack.Builder perkItem(CarnivalProfile profile, CarnivalPerk perk) {
        int owned = profile.tier(perk);
        if (owned >= perk.maxTier()) {
            return ItemStacks.item(perk.getIcon(), "<a>" + perk.tierName(owned) + "\n"
                    + perk.description(owned) + "\n\n<a>Maxed out!");
        }
        int next = owned + 1;
        StringBuilder lore = new StringBuilder(perk.isAvailable() ? "<a>" : "<8>").append(perk.tierName(next)).append("\n")
                .append(perk.description(next)).append("\n\n");
        if (owned > 0) lore.append("<7>Current Tier: <a>").append(StringUtility.getAsRomanNumeral(owned)).append("\n\n");
        lore.append("<7>Cost:\n<e>").append(StringUtility.commaify(perk.costOf(next))).append(" Carnival Tokens\n\n");
        if (!perk.isAvailable()) {
            lore.append("<8>").append(perk.getUnavailableReason()).append("\n<c>Unavailable");
        } else if (profile.getTokens() < perk.costOf(next)) {
            lore.append("<c>You don't have enough Carnival Tokens!");
        } else {
            lore.append(owned == 0 ? "<e>Click to unlock!" : "<e>Click to upgrade!");
        }
        return ItemStacks.item(perk.getIcon(), lore.toString());
    }

    private static void buy(SkyBlockPlayer player, CarnivalPerk perk) {
        switch (Carnivals.buyPerk(player, perk)) {
            case BOUGHT -> player.sendMessage("<a>You unlocked <e>"
                    + perk.tierName(Carnivals.profile(player).tier(perk)) + "<a>!");
            case UNAVAILABLE -> player.sendMessage("<c>" + perk.getUnavailableReason());
            case TOO_POOR -> player.sendMessage("<c>You don't have enough Carnival Tokens!");
            case MAXED -> {}
        }
    }
}
