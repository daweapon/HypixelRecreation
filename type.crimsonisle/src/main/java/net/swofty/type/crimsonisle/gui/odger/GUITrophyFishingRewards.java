package net.swofty.type.crimsonisle.gui.odger;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import net.swofty.type.skyblockgeneric.fishing.trophy.OdgerRewards;
import net.swofty.type.skyblockgeneric.fishing.trophy.TrophyFisherRank;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Odger's "Trophy Fishing Rewards" menu (wiki: Odger/UI): one button per Trophy Fisher rank listing its rewards and
 * progress; clicking a reached rank claims its rewards once.
 */
public class GUITrophyFishingRewards extends StatelessView {
    private static final int[] SLOTS = {19, 21, 23, 25};
    private static final Material[] ICONS = {Material.OAK_WOOD, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Trophy Fishing Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx, ItemStacks.item(Material.ARROW, "<a>Go Back\n<7>To Trophy Fishing"));
        Components.close(layout, 49);
        for (TrophyFisherRank rank : TrophyFisherRank.values()) {
            layout.slot(SLOTS[rank.ordinal()], (_, c) -> icon((SkyBlockPlayer) c.player(), rank), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                switch (OdgerRewards.claim(player, rank)) {
                    case CLAIMED -> player.sendMessage("<a>You claimed the " + rank.coloredName() + " <a>rewards!");
                    case ALREADY_CLAIMED -> player.sendMessage("<c>You have already claimed these rewards!");
                    case NOT_REACHED -> player.sendMessage("<c>You do not meet the requirements!");
                }
                c.session().refresh();
            });
        }
    }

    private static ItemStack.Builder icon(SkyBlockPlayer player, TrophyFisherRank rank) {
        DatapointTrophyFish.TrophyFishData data = player.getTrophyFishData();
        StringBuilder text = new StringBuilder(rank.coloredName()).append('\n');
        text.append(rank == TrophyFisherRank.NOVICE
                ? "<7>Catch <a>15 <7>Trophy Fish of <8>Bronze\n<7>tier or better.\n"
                : "<7>Catch every Trophy Fish at " + rank.tier().label() + "\n<7>tier" + (rank == TrophyFisherRank.MASTER ? "s.\n" : " or better.\n"));
        text.append("\n<e>Rewards:\n");
        for (OdgerRewards.Reward reward : OdgerRewards.RANK_REWARDS.get(rank)) {
            ItemType type = ItemType.get(reward.itemId());
            String name = type == null ? StringUtility.toNormalCase(reward.itemId()) : new SkyBlockItem(type).getDisplayName();
            text.append("<7>- <f>").append(name).append(reward.amount() > 1 ? " <8>x" + reward.amount() : "").append('\n');
        }
        if (rank == TrophyFisherRank.NOVICE) text.append("<7>- <a>Unlocks <c>Thunder\n");
        if (rank == TrophyFisherRank.ADEPT) text.append("<7>- <a>Unlocks <c>Lord Jawbus\n");
        text.append('\n');
        if (OdgerRewards.claimed(data, rank)) {
            text.append("<a>Rewards claimed!");
        } else if (rank.achieved(data)) {
            text.append("<e>Click to claim!");
        } else {
            text.append("<c>You do not meet the requirements\n<c>Caught ")
                    .append(Math.min(rank.progress(data), rank.goal())).append(" / ").append(rank.goal());
        }
        return ItemStacks.item(ICONS[rank.ordinal()], text.toString());
    }
}
