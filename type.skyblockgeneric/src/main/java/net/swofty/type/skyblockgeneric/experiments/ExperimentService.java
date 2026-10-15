package net.swofty.type.skyblockgeneric.experiments;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointExperiments;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ExperimentService {
    private ExperimentService() {
    }

    public static @Nullable String blockedReason(SkyBlockPlayer player, SuperpairsStake stake) {
        if (ExperimentTable.enchantingLevel(player) < stake.requiredEnchanting()) {
            return "<c>You need Enchanting " + stake.requiredEnchanting() + " to play " + stake.displayName() + " Superpairs!";
        }
        if (!ExperimentTable.superpairsUnlocked(player)) {
            return "<c>Complete the add-on experiments first!";
        }
        if (ExperimentTable.chargesAvailable(ExperimentTable.data(player), System.currentTimeMillis()) <= 0) {
            return "<c>Your Experimentation Table has no charges left!";
        }
        if (player.getLevel() < stake.startLevels()) {
            return "<c>You need " + stake.startLevels() + " Experience Levels to start this experiment!";
        }
        return null;
    }

    public static @Nullable SuperpairsGame startSuperpairs(SkyBlockPlayer player, SuperpairsStake stake) {
        String blocked = blockedReason(player, stake);
        if (blocked != null) {
            player.sendMessage(blocked);
            return null;
        }
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        ExperimentTable.consumeCharge(data, System.currentTimeMillis());
        player.setLevel(player.getLevel() - stake.startLevels());
        int clicks = stake.baseClicks() + ExperimentTable.addOnClicks(data, stake);
        ExperimentTable.resetAddOns(data);
        ExperimentTable.save(player);
        var random = ThreadLocalRandom.current();
        List<PairPrize> pairs = SuperpairsRewards.board(stake, player.getUuid(), UltraRareBoost.multiplier(player),
                ExperimentRngMeter.guaranteedPrize(player), random);
        return new SuperpairsGame(stake, pairs, clicks, random);
    }

    public static SuperpairsResult finishSuperpairs(SkyBlockPlayer player, SuperpairsGame game) {
        game.end();
        SuperpairsStake stake = game.stake();
        List<PairPrize> found = game.foundPrizes();
        long pairXp = 0;
        List<PairPrize> items = new ArrayList<>();
        for (PairPrize prize : found) {
            if (prize.kind() == PairPrize.Kind.XP || prize.kind() == PairPrize.Kind.LAPIS_BLOCK) pairXp += prize.xp();
            else if (!prize.isPowerUp()) items.add(prize);
        }
        return new SuperpairsResult(stake.stakeXp(), pairXp, found.size(), stake.pairCount(), items, game.clicksUsed(), stake.displayName());
    }

    public static void claimSuperpairs(SkyBlockPlayer player, SuperpairsGame game, SuperpairsResult result) {
        for (PairPrize prize : result.prizes()) grant(player, prize);
        EnchantingXpService.award(player, result.totalXp());
        ExperimentRngMeter.addFromEnchantingXp(player, result.totalXp());
        ExperimentRngMeter.consumeIfWon(player, game.foundPrizes());
    }

    public static @Nullable SkyBlockItem itemFor(PairPrize prize) {
        switch (prize.kind()) {
            case BOOK -> {
                SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
                book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(prize.enchantment(), prize.level()));
                return book;
            }
            case PET -> {
                SkyBlockItem pet = new SkyBlockItem(prize.item(), 1);
                pet.getAttributeHandler().setRarity(prize.rarity());
                return pet;
            }
            case ITEM, SERUM -> {
                SkyBlockItem item = new SkyBlockItem(prize.item());
                item.setAmount(prize.amount());
                return item;
            }
            default -> {
                return null;
            }
        }
    }

    public static void grant(SkyBlockPlayer player, PairPrize prize) {
        SkyBlockItem item = itemFor(prize);
        if (item != null) player.addAndUpdateItem(item);
    }

    public static ChronomatronGame startChronomatron(SkyBlockPlayer player, ChronomatronStake stake) {
        if (!chargeAttempt(player, "C" + stake.name(), stake.requiredEnchanting(), stake.restartLevels())) return null;
        return new ChronomatronGame(stake, ThreadLocalRandom.current());
    }

    public static UltrasequencerGame startUltrasequencer(SkyBlockPlayer player, UltrasequencerStake stake) {
        if (!chargeAttempt(player, "U" + stake.name(), stake.requiredEnchanting(), stake.restartLevels())) return null;
        return new UltrasequencerGame(stake, ThreadLocalRandom.current());
    }

    private static boolean chargeAttempt(SkyBlockPlayer player, String key, int requiredEnchanting, int restartLevels) {
        if (ExperimentTable.enchantingLevel(player) < requiredEnchanting) {
            player.sendMessage("<c>You need Enchanting {} to play this experiment!", requiredEnchanting);
            return false;
        }
        int cost = ExperimentTable.attemptCost(player, key, restartLevels);
        if (!ExperimentTable.spendLevels(player, cost)) {
            player.sendMessage("<c>You need {} Experience Levels to try again today!", cost);
            return false;
        }
        ExperimentTable.recordAttempt(ExperimentTable.data(player), key);
        ExperimentTable.save(player);
        return true;
    }

    public static int claimChronomatron(SkyBlockPlayer player, ChronomatronGame game) {
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        int clicks = game.stake().clickBonus().clicksFor(game.longestChain(), ExperimentTable.serums(data));
        EnchantingXpService.award(player, game.stake().xpFor(game.longestChain()));
        ExperimentRngMeter.addFromEnchantingXp(player, game.stake().xpFor(game.longestChain()));
        ExperimentTable.completeChronomatron(data, clicks);
        ExperimentTable.save(player);
        return clicks;
    }

    public static int claimUltrasequencer(SkyBlockPlayer player, UltrasequencerGame game) {
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        int clicks = game.stake().clickBonus().clicksFor(game.longestSequence(), ExperimentTable.serums(data));
        EnchantingXpService.award(player, game.stake().xpFor(game.longestSequence()));
        ExperimentRngMeter.addFromEnchantingXp(player, game.stake().xpFor(game.longestSequence()));
        ExperimentTable.completeUltrasequencer(data, clicks);
        ExperimentTable.save(player);
        return clicks;
    }
}
