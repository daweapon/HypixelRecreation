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
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynMilestones;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynProfile;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynShop;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUIStarlynMilestones extends StatelessView {
    private static final int FIRST_SLOT = 20;
    private final StarlynSister sister;

    public GUIStarlynMilestones(StarlynSister sister) {
        this.sister = sister;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(sister.milestoneName() + "s", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        StarlynConfig config = StarlynConfig.get(sister);
        for (StarlynConfig.Milestone milestone : config.milestones()) {
            layout.slot(FIRST_SLOT + milestone.number() - 1, (_, c) -> item((SkyBlockPlayer) c.player(), config, milestone));
        }
    }

    private ItemStack.Builder item(SkyBlockPlayer player, StarlynConfig config, StarlynConfig.Milestone milestone) {
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        int current = data.getMilestoneGranted();
        boolean unlocked = milestone.number() <= current;
        Material pane = unlocked ? Material.LIME_STAINED_GLASS_PANE
                : milestone.number() == current + 1 ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String colour = unlocked ? "<a>" : milestone.number() == current + 1 ? "<e>" : "<c>";
        String roman = StarlynShop.roman(milestone.number());
        StringBuilder text = new StringBuilder(colour + sister.milestoneName() + " " + roman + "\n\n");
        if (!unlocked) {
            long earned = data.getCouponsEarned();
            text.append("<7>Progress to Milestone ").append(roman).append(": <e>")
                    .append(Math.min(100, earned * 100 / Math.max(1, milestone.threshold()))).append("%\n")
                    .append(StarlynGui.bar(earned, milestone.threshold())).append(" <e>").append(earned).append("<6>/<e>")
                    .append(StarlynGui.compact(milestone.threshold())).append("\n\n");
        }
        text.append("<7>Rewards:\n<8>  +<a>New items in ").append(sister.shopName());
        if (milestone.number() > 1) {
            text.append("\n<8>   +<3>").append(StringUtility.commaify(milestone.foragingXp())).append(" Foraging Exp")
                    .append("\n<8>   +<f>").append(StringUtility.commaify(milestone.essence())).append("x <d>Forest Essence")
                    .append("\n<8>   +<a>").append(StarlynMilestones.FORAGING_FORTUNE_PER_MILESTONE).append(" <6>☘ Foraging Fortune");
            if (sister == StarlynSister.AGATHA && milestone.number() == StarlynMilestones.EMBLEM_MILESTONE) {
                text.append("\n<8>   +<a>Agatha's Jewel Emblem <7>ꖘ");
            }
            text.append("\n<8>   +<b>").append(StarlynMilestones.SKYBLOCK_XP_PER_MILESTONE).append(" SkyBlock XP\n\n<9>")
                    .append(sister.getCurrency().getDisplayName()).append(" from ").append(sister.contestName())
                    .append("s\n<9>are now buffed by ").append(milestone.whisperBuffPercent()).append("%!");
        }
        if (unlocked) text.append("\n\n<a><l>UNLOCKED");
        return ItemStacks.item(pane, text.toString());
    }
}
