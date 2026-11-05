package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.type.skyblockgeneric.bank.CoinPayment;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

/**
 * Buys a run's chest, at the end of the run or later from Croesus: coins come from the purse, then the bank.
 * The first chest needs only its price; a second needs a Dungeon Chest Key, taken only when it opens. A Kismet
 * Feather rerolls one unopened chest once per run and is used up whether or not the chest is then bought.
 */
public final class DungeonChestService {

    public enum OpenResult { OPENED, ALREADY_OPENED, NO_MORE_CHESTS, NEEDS_KEY, CANNOT_AFFORD, EXPIRED }

    public enum RerollResult { REROLLED, ALREADY_REROLLED, OPENED, NO_FEATHER, EXPIRED }

    private DungeonChestService() {
    }

    public static OpenResult open(SkyBlockPlayer player, String runId, DungeonChestTier tier) {
        synchronized (player) {
            DungeonChestRun run = DungeonChestStorage.find(player, runId);
            DungeonRewardChest chest = run == null ? null : run.chest(tier);
            if (run == null || chest == null) {
                player.sendMessage("<c>These chests have expired!");
                return OpenResult.EXPIRED;
            }
            if (chest.opened()) {
                player.sendMessage("<c>This chest has already been opened!");
                return OpenResult.ALREADY_OPENED;
            }
            if (!run.canOpenMore()) {
                player.sendMessage("<c>You have already opened a chest for this run!");
                return OpenResult.NO_MORE_CHESTS;
            }
            boolean needsKey = run.nextNeedsKey();
            if (needsKey && !hasKey(player)) {
                player.sendMessage("<c>You need a <9>Dungeon Chest Key <c>to open another chest!");
                return OpenResult.NEEDS_KEY;
            }
            if (!CoinPayment.canAfford(player, chest.cost())) {
                player.sendMessage("<c>You cannot afford this!");
                return OpenResult.CANNOT_AFFORD;
            }
            if (needsKey && !player.removeItemFromPlayer(ItemType.DUNGEON_CHEST_KEY, 1)) {
                player.sendMessage("<c>You need a <9>Dungeon Chest Key <c>to open another chest!");
                return OpenResult.NEEDS_KEY;
            }
            if (!CoinPayment.pay(player, chest.cost(), tier.chestName())) {
                if (needsKey) player.addAndUpdateItem(ItemType.DUNGEON_CHEST_KEY);
                player.sendMessage("<c>You cannot afford this!");
                return OpenResult.CANNOT_AFFORD;
            }
            DungeonChestStorage.replace(player, run.open(tier, needsKey));
            announce(player, chest);
            boolean master = run.mode() == CatacombsMode.MASTER;
            chest.rewards().forEach(reward -> reward.deliver(player, master));
            return OpenResult.OPENED;
        }
    }

    public static RerollResult reroll(SkyBlockPlayer player, String runId, DungeonChestTier tier) {
        synchronized (player) {
            DungeonChestRun run = DungeonChestStorage.find(player, runId);
            DungeonRewardChest chest = run == null ? null : run.chest(tier);
            if (run == null || chest == null) {
                player.sendMessage("<c>These chests have expired!");
                return RerollResult.EXPIRED;
            }
            if (run.rerolled()) {
                player.sendMessage("<c>You already rerolled a chest for this run!");
                return RerollResult.ALREADY_REROLLED;
            }
            if (!run.canReroll(tier)) {
                player.sendMessage("<c>You can't reroll a chest you already opened!");
                return RerollResult.OPENED;
            }
            if (!player.removeItemFromPlayer(ItemType.KISMET_FEATHER, 1)) {
                player.sendMessage("<c>You need a <9>Kismet Feather <c>to reroll this chest!");
                return RerollResult.NO_FEATHER;
            }
            DungeonChestStorage.replace(player, run.reroll(tier, DungeonRewards.reroll(player, run, tier)));
            player.sendMessage("<a>You rerolled the " + tier.colour() + tier.chestName() + "<a>!");
            return RerollResult.REROLLED;
        }
    }

    public static boolean hasKey(SkyBlockPlayer player) {
        return count(player, ItemType.DUNGEON_CHEST_KEY) > 0;
    }

    public static boolean hasFeather(SkyBlockPlayer player) {
        return count(player, ItemType.KISMET_FEATHER) > 0;
    }

    private static int count(SkyBlockPlayer player, ItemType type) {
        return player.getAmountInInventory(type);
    }

    public static String costLine(long cost) {
        return cost <= 0 ? "<a>FREE" : "<6>" + StringUtility.commaify(cost) + " Coins";
    }

    private static void announce(SkyBlockPlayer player, DungeonRewardChest chest) {
        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(chest.tier().colour() + "<l>" + chest.tier().chestName().toUpperCase(Locale.ROOT) + " REWARDS");
        chest.rewards().forEach(reward -> player.sendMessage(Text.of("  {}", reward.name())));
        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
