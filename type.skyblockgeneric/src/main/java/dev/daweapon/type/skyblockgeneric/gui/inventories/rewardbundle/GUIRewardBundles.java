package dev.daweapon.type.skyblockgeneric.gui.inventories.rewardbundle;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.rewardbundle.BundleReward;
import dev.daweapon.type.skyblockgeneric.rewardbundle.BundleSeason;
import dev.daweapon.type.skyblockgeneric.rewardbundle.RewardBundleService;
import dev.daweapon.type.skyblockgeneric.rewardbundle.RewardBundles;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GUIRewardBundles extends StatelessView {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    private static final int PREVIOUS_PAGE_SLOT = 45;
    private static final int CLAIM_ALL_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 50;
    private static final int CYCLE_SLOT = 53;

    private final BundleSeason season;
    private final int page;

    public GUIRewardBundles() {
        this(defaultSeason(), 0);
    }

    public GUIRewardBundles(BundleSeason season, int page) {
        this.season = season;
        this.page = page;
    }

    private static BundleSeason defaultSeason() {
        BundleSeason current = BundleSeason.current(LocalDate.now());
        return current == null ? BundleSeason.SUMMER : current;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Reward Bundles: " + season.label(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Components.close(layout, CLOSE_SLOT);
        List<BundleReward> rewards = RewardBundles.of(season);
        int pages = Math.max(1, (rewards.size() + SLOTS.length - 1) / SLOTS.length);
        int current = Math.min(page, pages - 1);
        for (int i = 0; i < SLOTS.length; i++) {
            int index = current * SLOTS.length + i;
            if (index >= rewards.size()) break;
            BundleReward reward = rewards.get(index);
            layout.slot(SLOTS[i], (s, c) -> render(player, reward), (click, c) -> {
                RewardBundleService.claim((SkyBlockPlayer) c.player(), reward);
                c.session(DefaultState.class).refresh();
            });
        }
        if (current > 0) {
            layout.slot(PREVIOUS_PAGE_SLOT, ItemStacks.item(Material.ARROW, 1, "<a>Previous Page\n<e>Page " + current),
                    (_, c) -> c.player().openView(new GUIRewardBundles(season, current - 1)));
        }
        if (current < pages - 1) {
            layout.slot(NEXT_PAGE_SLOT, ItemStacks.item(Material.ARROW, 1, "<a>Next Page\n<e>Page " + (current + 2)),
                    (_, c) -> c.player().openView(new GUIRewardBundles(season, current + 1)));
        }
        layout.slot(CLAIM_ALL_SLOT, ItemStacks.item(Material.CHEST, 1, "<a>Claim Everything\n<7>Claim every " + season.label() + " reward\n<7>your rank allows.\n\n<e>Click to claim!"),
                (_, c) -> {
                    RewardBundleService.claimAll((SkyBlockPlayer) c.player(), season);
                    c.session(DefaultState.class).refresh();
                });
        layout.slot(CYCLE_SLOT, (s, c) -> cycleButton(), (click, c) -> {
            BundleSeason target = click.click() instanceof Click.Right ? season.previous() : season.next();
            c.player().openView(new GUIRewardBundles(target, 0));
        });
    }

    private ItemStack.Builder cycleButton() {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        for (BundleSeason option : BundleSeason.values()) {
            lore.add(Text.of(option == season ? "<a>▶ {}" : "<7>  {}", option.label()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch season!"));
        return ItemStacks.item(season.icon(), 1, Text.of("<a>Season: {}", season.label()), lore);
    }

    private ItemStack.Builder render(SkyBlockPlayer player, BundleReward reward) {
        List<Text> lore = new ArrayList<>();
        ItemStack.Builder base;
        if (reward.type() == BundleReward.Type.SPRAY) {
            base = ItemStacks.item(Material.PAINTING, 1, Text.of("<a>{}", RewardBundleService.displayName(reward)), List.of());
            lore.add(Text.of("<8>Spray"));
        } else {
            base = new NonPlayerItemUpdater(new SkyBlockItem(ItemType.get(reward.id()), reward.amount())).getUpdatedItem();
            lore.addAll(ItemStacks.loreText(base));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rank: " + reward.rank().prefixMarkup()));
        lore.add(Text.of("<7>Available since: <f>{}", reward.year()));
        lore.add(Text.empty());
        if (RewardBundleService.isClaimed(player, reward)) lore.add(Text.of("<a>Already claimed!"));
        else if (!RewardBundleService.meetsRank(player, reward)) lore.add(Text.of("<c>Requires " + reward.rank().prefixMarkup() + "<c>!"));
        else lore.add(Text.of("<e>Click to claim!"));
        return ItemStacks.lore(base, lore);
    }
}
