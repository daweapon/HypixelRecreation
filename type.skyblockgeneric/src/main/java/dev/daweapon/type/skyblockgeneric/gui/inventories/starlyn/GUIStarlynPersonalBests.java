package dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynPersonalBests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIStarlynPersonalBests extends StatelessView {
    private static final int[] SLOTS = {21, 23};
    private final StarlynSister sister;

    public GUIStarlynPersonalBests(StarlynSister sister) {
        this.sister = sister;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Personal Bests", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 39, ctx);
        Components.close(layout, 40);
        List<StarlynConfig.PersonalBest> perks = StarlynConfig.get(sister).shop().stream()
                .filter(StarlynConfig.PersonalBest.class::isInstance).map(StarlynConfig.PersonalBest.class::cast).toList();
        int[] slots = perks.size() == 1 ? new int[]{22} : SLOTS;
        for (int index = 0; index < perks.size() && index < slots.length; index++) {
            StarlynConfig.PersonalBest perk = perks.get(index);
            layout.slot(slots[index], (_, c) -> item((SkyBlockPlayer) c.player(), perk));
        }
    }

    private ItemStack.Builder item(SkyBlockPlayer player, StarlynConfig.PersonalBest perk) {
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        if (data.purchased(perk.id()) <= 0) {
            return ItemStacks.item(Material.BARRIER, "<a>" + perk.name() + "\n\n<c><l>LOCKED\n<7>Buy it in " + sister.shopName() + "<7>.");
        }
        long best = data.personalBest(perk.tree());
        return ItemStacks.item(Material.GOLDEN_HELMET, "<a>" + perk.name() + "\n\n<7>Personal Best: <e>" + StringUtility.commaify(best)
                + " " + perk.tree().getDisplayName() + " Logs\n<7>Bonus: <2>+" + String.format("%.2f", StarlynPersonalBests.sweepPercent(best))
                + "% ∮ Sweep\n\n<8>+0.01% Sweep for every 100 logs,\n<8>up to " + StringUtility.commaify(StarlynPersonalBests.MAX_COLLECTION) + " logs.");
    }
}
