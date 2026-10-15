package dev.daweapon.type.crimsonisle.gui.odger;

import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.OdgerRewards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Odger's "Trophy Fishing" menu (wiki: Odger/UI): the 18 fish in rows 2-4, Fillet Trophy Fish, Trophy Fishing
 * Rewards and the Lost Rewards Shop. The discovered-fish lore isn't on the wiki; it lists each tier's count.
 */
public class GUITrophyFishing extends StatelessView {
    static final String MAGMAFISH_TEXTURE = "f56b5955b295522c9689481960c01a992ca1c7754cf4ee313c8dd0c356d335f";
    static final int[] FISH_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Trophy Fishing", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        List<TrophyFishDefinition> fish = FishingRegistry.getTrophyFish();
        for (int i = 0; i < fish.size() && i < FISH_SLOTS.length; i++) {
            TrophyFishDefinition definition = fish.get(i);
            layout.slot(FISH_SLOTS[i], (_, c) -> fishIcon((SkyBlockPlayer) c.player(), definition));
        }
        layout.slot(47, (_, c) -> ItemStacks.head(MAGMAFISH_TEXTURE, """
                <a>Fillet Trophy Fish
                <7>Fillets all the <6>Trophy Fish <7>in your
                <7>inventory, giving you <9>MagmaFish <7>as a
                <7>reward!

                <e>Click to Fillet"""), (_, c) -> fillet((SkyBlockPlayer) c.player()));
        layout.slot(51, (_, c) -> ItemStacks.item(Material.GOLD_BLOCK, """
                <a>Trophy Fishing Rewards
                <7>Catch all Trophy Fish at a tier
                <7>to gain unique rewards!

                <e>Click to view!"""), (_, c) -> c.push(new GUITrophyFishingRewards()));
        layout.slot(52, (_, c) -> ItemStacks.item(Material.CHEST, """
                <a>Lost Rewards Shop
                <7>Buy back any rewards you may
                <7>have lost!

                <e>Click to view!"""), (_, c) -> c.push(new GUILostRewardsShop()));
    }

    private static void fillet(SkyBlockPlayer player) {
        int[] result = OdgerRewards.filletAll(player);
        if (result[0] == 0) {
            player.sendMessage("<c>You don't have any Trophy Fish in your inventory to fillet!");
            return;
        }
        player.sendMessage("<a>You filleted <6>" + result[0] + " Trophy Fish <a>and received <9>" + result[1] + " Magmafish<a>!");
    }

    static ItemStack.Builder fishIcon(SkyBlockPlayer player, TrophyFishDefinition fish) {
        DatapointTrophyFish.FishProgress progress = player.getTrophyFishData().getFish().get(fish.id());
        if (progress == null || progress.getTotalCatches() == 0) {
            String hint = fish.hint() == null ? "???" : fish.hint();
            return ItemStacks.item(Material.GRAY_DYE, "<c><k>" + "x".repeat(fish.displayName().length()) + "\n"
                    + "<9>How to catch\n<7>  " + hint + "\n\n<c>Undiscovered");
        }
        StringBuilder lore = new StringBuilder(fish.coloredName()).append('\n');
        int[] counts = {progress.getBronze(), progress.getSilver(), progress.getGold(), progress.getDiamond()};
        for (TrophyTier tier : TrophyTier.values()) {
            int count = counts[tier.ordinal()];
            lore.append(tier.label()).append(" ").append(count > 0 ? "<a>✔ <7>" + count : "<c>✖").append('\n');
        }
        lore.append("\n<7>Total caught: <a>").append(progress.getTotalCatches());
        TrophyTier best = TrophyTier.orderedDescending().stream().filter(tier -> counts[tier.ordinal()] > 0).findFirst().orElse(TrophyTier.BRONZE);
        return ItemStacks.item(Material.PAPER, lore.toString())
                .set(DataComponents.ITEM_MODEL, "hypixel_skyblock:item/fishing/trophy/lava/" + fish.itemId(best).toLowerCase());
    }
}
