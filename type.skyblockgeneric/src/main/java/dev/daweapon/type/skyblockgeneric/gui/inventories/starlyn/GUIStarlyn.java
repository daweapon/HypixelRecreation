package dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn;

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
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynRewards;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynShop;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUIStarlyn extends StatelessView {
    private final StarlynSister sister;

    public GUIStarlyn(StarlynSister sister) {
        this.sister = sister;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(sister.getDisplayName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.OAK_SIGN, """
                <a>What is this?
                <8>Instructions

                <7>Every SkyBlock Day, <b>%s <7>holds a
                <7>contest for everyone currently on
                <7>the %s<7>.

                <7>Doing various activities while on the
                %s <7>grants <9>points<7>.

                <7>Reaching <d>point brackets <7>will earn
                <7>you even greater rewards!

                <e>Click for details!""".formatted(sister.getDisplayName(), sister.getIsland(), sister.getIsland())),
                (_, c) -> c.push(new GUIStarlynRules(sister)));
        layout.slot(13, (_, c) -> milestonesItem((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIStarlynMilestones(sister)));
        layout.slot(15, (_, c) -> rewardsItem((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIStarlynRewards(sister)));
        layout.slot(30, (_, _) -> ItemStacks.item(Material.EMERALD, """
                <a>Shop
                <7>Spend your <a>%s's Coupons <7>in
                <7>her shop!

                <7>Some items are locked behind higher
                <7>tiers of <6>%s<7>!

                <e>Click to open!""".formatted(sister.getDisplayName(), sister.milestoneName() + "s")),
                (_, c) -> c.push(new GUIStarlynShop(sister)));
        layout.slot(32, (_, c) -> personalBestsItem((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIStarlynPersonalBests(sister)));
    }

    private ItemStack.Builder milestonesItem(SkyBlockPlayer player) {
        StarlynConfig config = StarlynConfig.get(sister);
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        int current = data.getMilestoneGranted();
        StringBuilder text = new StringBuilder("<6>" + sister.milestoneName() + "s\n");
        text.append("<7>Your Milestone: <a>").append(StarlynShop.roman(current)).append("\n\n");
        text.append("<7>Gain <a>").append(sister.couponName()).append("s <7>to progress\n<7>on this Milestone!\n\n");
        if (current >= config.maxMilestone()) {
            text.append("<a>Maxed out!");
        } else {
            int target = config.milestone(current + 1).threshold();
            long earned = data.getCouponsEarned();
            text.append("<7>Progress to Level ").append(StarlynShop.roman(current + 1)).append(": <e>")
                    .append(Math.min(100, earned * 100 / target)).append("%\n")
                    .append(StarlynGui.bar(earned, target)).append(" <e>").append(earned).append("<6>/<e>").append(StarlynGui.compact(target));
        }
        text.append("\n\n<e>Click to open!");
        return ItemStacks.item(Material.LADDER, text.toString());
    }

    private ItemStack.Builder rewardsItem(SkyBlockPlayer player) {
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        int claimable = data.claimable().size();
        StringBuilder text = new StringBuilder("<b>Claim your rewards!\n<8>Previous contests\n\n");
        if (claimable == 0) {
            text.append("<7>You don't have any rewards to claim!\n");
        } else {
            text.append("<7>You have <a>").append(claimable).append(" <7>contest").append(claimable > 1 ? "s" : "")
                    .append(" to claim!\n");
        }
        text.append("\n<7>You will earn rewards if you\n<7>accumulate at least <a>")
                .append(StarlynConfig.get(sister).brackets().getFirst().points()).append(" <7>points in a\n<7>contest.\n\n")
                .append("<7>The first <e>").append(StarlynRewards.DAILY_BONUS_CONTESTS).append(" <7>contests each day\n<7>reward <a>+")
                .append("1,200 HOTF EXP<7>.\n\n<7>Daily Bonus claimed: <c>")
                .append(StarlynRewards.DAILY_BONUS_CONTESTS - StarlynRewards.dailyBonusesLeft(player, sister))
                .append("<7>/<a>").append(StarlynRewards.DAILY_BONUS_CONTESTS);
        if (claimable > 0) text.append("\n\n<e>Click to view!");
        return ItemStacks.item(Material.CHEST, text.toString());
    }

    private ItemStack.Builder personalBestsItem(SkyBlockPlayer player) {
        StarlynProfile.SisterData data = StarlynContests.data(player, sister);
        StringBuilder text = new StringBuilder("<6>Personal Bests\n");
        text.append("<7>Once purchased in her shop, <b>").append(sister.getDisplayName()).append("\n")
                .append("<7>will keep track of your <6>Personal\n<6>Bests <7>during her contest!\n\n")
                .append("<7>The higher your <6>Personal Best<7>, the\n<7>more <2>∮ Sweep <7>you'll get!\n");
        for (StarlynConfig.ShopEntry entry : StarlynConfig.get(sister).shop()) {
            if (!(entry instanceof StarlynConfig.PersonalBest best)) continue;
            text.append("\n<a>").append(best.name()).append("<7>: ");
            if (data.purchased(best.id()) > 0) {
                text.append("<a>+").append(String.format("%.2f", StarlynPersonalBests.sweepPercent(data.personalBest(best.tree())))).append("% Sweep");
            } else {
                text.append("<c><l>LOCKED");
            }
        }
        text.append("\n\n<e>Click to view!");
        return ItemStacks.item(Material.GOLDEN_HELMET, text.toString());
    }
}
