package net.swofty.type.hub.gui.farmingcontest;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestCrop;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// The wiki documents the Personal Bests button but not the menu behind it; this layout is an estimate.
public class GUIAnitaPersonalBests extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Personal Bests", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        if (!Components.back(layout, 40, ctx)) Components.close(layout, 40);

        ContestCrop[] crops = ContestCrop.values();
        for (int i = 0; i < crops.length; i++) {
            ContestCrop crop = crops[i];
            layout.slot(SLOTS[i], (_, c) -> {
                FarmingContestProfile profile = FarmingContests.profile((SkyBlockPlayer) c.player());
                long best = profile.personalBest(crop);
                List<String> lines = new ArrayList<>();
                lines.add("<a>" + crop.getDisplayName());
                lines.add("<7>Personal Best: <e>" + StringUtility.commaify(best));
                lines.add("");
                if (crop.getFortuneStatistic() == null) {
                    lines.add("<8>" + crop.getUnavailableReason());
                } else {
                    lines.add("<7>Granting: <6>+" + String.format("%.2f", crop.personalBestFortune(best)) + "☘ "
                            + crop.getDisplayName() + " Fortune");
                }
                lines.add("<7>Every <e>" + StringUtility.commaify(crop.getCropsPerTenthFortune()) + " <7>crops grant <6>+0.1☘");
                lines.add("<7>Maximum: <6>+" + (int) ContestCrop.PERSONAL_BEST_FORTUNE_CAP + "☘ <7>at <e>"
                        + StringUtility.commaify(crop.getCropsPerTenthFortune() * 1_000L) + " <7>crops");
                return ItemStacks.item(crop.getIcon(), String.join("\n", lines));
            });
        }
    }
}
