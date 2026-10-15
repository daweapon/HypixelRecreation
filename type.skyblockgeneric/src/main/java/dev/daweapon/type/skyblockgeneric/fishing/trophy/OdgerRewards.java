package dev.daweapon.type.skyblockgeneric.fishing.trophy;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Odger's rewards (wiki: Odger, Odger/UI, Odger/Filleting_Rewards): the items each Trophy Fisher rank grants, the
 * Lost Rewards Shop that sells them back, filleting trophy fish into Magmafish, and the free Starter Lava Rod.
 * Hypixel says the higher ranks "upgrade" the Hunter armor and tooth; here they hand over the new tier (estimate).
 */
public final class OdgerRewards {
    public static final Map<TrophyFisherRank, List<Reward>> RANK_REWARDS = Map.of(
            TrophyFisherRank.NOVICE, List.of(new Reward("BRONZE_HUNTER_HELMET", 1), new Reward("BRONZE_HUNTER_CHESTPLATE", 1),
                    new Reward("BRONZE_HUNTER_LEGGINGS", 1), new Reward("BRONZE_HUNTER_BOOTS", 1),
                    new Reward("ODGERS_BRONZE_TOOTH", 1), new Reward("BRONZE_TROPHY_FISHING_SACK", 1), new Reward("MAGMAFISH", 20)),
            TrophyFisherRank.ADEPT, List.of(new Reward("SILVER_TROPHY_FISHING_SACK", 1), new Reward("SILVER_HUNTER_HELMET", 1),
                    new Reward("SILVER_HUNTER_CHESTPLATE", 1), new Reward("SILVER_HUNTER_LEGGINGS", 1),
                    new Reward("SILVER_HUNTER_BOOTS", 1), new Reward("ODGERS_SILVER_TOOTH", 1), new Reward("SILVER_MAGMAFISH", 20)),
            TrophyFisherRank.EXPERT, List.of(new Reward("GOLD_HUNTER_HELMET", 1), new Reward("GOLD_HUNTER_CHESTPLATE", 1),
                    new Reward("GOLD_HUNTER_LEGGINGS", 1), new Reward("GOLD_HUNTER_BOOTS", 1),
                    new Reward("ODGERS_GOLD_TOOTH", 1), new Reward("GOLD_MAGMAFISH", 20)),
            TrophyFisherRank.MASTER, List.of(new Reward("DIAMOND_HUNTER_HELMET", 1), new Reward("DIAMOND_HUNTER_CHESTPLATE", 1),
                    new Reward("DIAMOND_HUNTER_LEGGINGS", 1), new Reward("DIAMOND_HUNTER_BOOTS", 1),
                    new Reward("ODGERS_DIAMOND_TOOTH", 1), new Reward("DIAMOND_THE_FISH", 1), new Reward("DIAMOND_MAGMAFISH", 20)));

    public static final List<ShopEntry> LOST_REWARDS_SHOP = List.of(
            new ShopEntry(10, "STARTER_LAVA_ROD", 10_000, null),
            new ShopEntry(12, "ODGERS_BRONZE_TOOTH", 100_000, TrophyFisherRank.NOVICE),
            new ShopEntry(13, "BRONZE_HUNTER_HELMET", 100_000, TrophyFisherRank.NOVICE),
            new ShopEntry(14, "BRONZE_HUNTER_CHESTPLATE", 100_000, TrophyFisherRank.NOVICE),
            new ShopEntry(15, "BRONZE_HUNTER_LEGGINGS", 100_000, TrophyFisherRank.NOVICE),
            new ShopEntry(16, "BRONZE_HUNTER_BOOTS", 100_000, TrophyFisherRank.NOVICE),
            new ShopEntry(19, "BRONZE_TROPHY_FISHING_SACK", 1_000_000, TrophyFisherRank.NOVICE),
            new ShopEntry(21, "ODGERS_SILVER_TOOTH", 500_000, TrophyFisherRank.ADEPT),
            new ShopEntry(22, "SILVER_HUNTER_HELMET", 500_000, TrophyFisherRank.ADEPT),
            new ShopEntry(23, "SILVER_HUNTER_CHESTPLATE", 500_000, TrophyFisherRank.ADEPT),
            new ShopEntry(24, "SILVER_HUNTER_LEGGINGS", 500_000, TrophyFisherRank.ADEPT),
            new ShopEntry(25, "SILVER_HUNTER_BOOTS", 500_000, TrophyFisherRank.ADEPT),
            new ShopEntry(28, "SILVER_TROPHY_FISHING_SACK", 5_000_000, TrophyFisherRank.ADEPT),
            new ShopEntry(30, "ODGERS_GOLD_TOOTH", 1_000_000, TrophyFisherRank.EXPERT),
            new ShopEntry(31, "GOLD_HUNTER_HELMET", 1_000_000, TrophyFisherRank.EXPERT),
            new ShopEntry(32, "GOLD_HUNTER_CHESTPLATE", 1_000_000, TrophyFisherRank.EXPERT),
            new ShopEntry(33, "GOLD_HUNTER_LEGGINGS", 1_000_000, TrophyFisherRank.EXPERT),
            new ShopEntry(34, "GOLD_HUNTER_BOOTS", 1_000_000, TrophyFisherRank.EXPERT),
            new ShopEntry(37, "DIAMOND_THE_FISH", 10_000_000, TrophyFisherRank.MASTER),
            new ShopEntry(39, "ODGERS_DIAMOND_TOOTH", 2_500_000, TrophyFisherRank.MASTER),
            new ShopEntry(40, "DIAMOND_HUNTER_HELMET", 2_500_000, TrophyFisherRank.MASTER),
            new ShopEntry(41, "DIAMOND_HUNTER_CHESTPLATE", 2_500_000, TrophyFisherRank.MASTER),
            new ShopEntry(42, "DIAMOND_HUNTER_LEGGINGS", 2_500_000, TrophyFisherRank.MASTER),
            new ShopEntry(43, "DIAMOND_HUNTER_BOOTS", 2_500_000, TrophyFisherRank.MASTER));

    private static final int INVENTORY_SLOTS = 36;

    private OdgerRewards() {
    }

    public static boolean claimed(DatapointTrophyFish.TrophyFishData data, TrophyFisherRank rank) {
        return data.getClaimedRanks().contains(rank.name());
    }

    public static ClaimResult claim(SkyBlockPlayer player, TrophyFisherRank rank) {
        DatapointTrophyFish.TrophyFishData data = player.getTrophyFishData();
        if (claimed(data, rank)) return ClaimResult.ALREADY_CLAIMED;
        if (!rank.achieved(data)) return ClaimResult.NOT_REACHED;
        for (Reward reward : RANK_REWARDS.get(rank)) {
            ItemType type = ItemType.get(reward.itemId());
            if (type != null) player.addAndUpdateItem(type, reward.amount());
        }
        data.getClaimedRanks().add(rank.name());
        TrophyFishAwards.save(player, data);
        return ClaimResult.CLAIMED;
    }

    public static ShopResult buy(SkyBlockPlayer player, ShopEntry entry) {
        ItemType type = ItemType.get(entry.itemId());
        if (type == null) return ShopResult.UNAVAILABLE;
        if (entry.requiredRank() != null && !entry.requiredRank().achieved(player.getTrophyFishData())) return ShopResult.LOCKED;
        if (player.getCoins() < entry.coins()) return ShopResult.TOO_POOR;
        player.removeCoins(entry.coins());
        player.addAndUpdateItem(type);
        return ShopResult.BOUGHT;
    }

    /** Fillets every Trophy Fish in the inventory; returns the fish count and the Magmafish earned. */
    public static int[] filletAll(SkyBlockPlayer player) {
        int fish = 0;
        int magmafish = 0;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
            Optional<Fillet> fillet = type == null ? Optional.empty() : filletOf(type.name());
            if (fillet.isEmpty()) continue;
            fish += stack.amount();
            magmafish += fillet.get().magmafish() * stack.amount();
            player.getInventory().setItemStack(slot, ItemStack.AIR);
        }
        if (magmafish > 0) player.addAndUpdateItem(ItemType.MAGMAFISH, magmafish);
        return new int[]{fish, magmafish};
    }

    public static Optional<Fillet> filletOf(String itemId) {
        Optional<TrophyTier> tier = TrophyTier.fromItemId(itemId);
        if (tier.isEmpty()) return Optional.empty();
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            if (fish.itemId(tier.get()).equals(itemId)) return Optional.of(new Fillet(fish, tier.get(), fish.filletAmount(tier.get())));
        }
        return Optional.empty();
    }

    /**
     * Gives the profile its one free Starter Lava Rod. Tracked apart from "met Odger" so a profile
     * that met him while the rod failed to build still gets it on its next visit.
     */
    public static boolean giveStarterRod(SkyBlockPlayer player) {
        DatapointTrophyFish.TrophyFishData data = player.getTrophyFishData();
        if (data.isStarterRodGiven()) return false;
        player.addAndUpdateItem(ItemType.STARTER_LAVA_ROD);
        data.setOdgerMet(true);
        data.setStarterRodGiven(true);
        TrophyFishAwards.save(player, data);
        return true;
    }

    public record Reward(String itemId, int amount) {
    }

    public record ShopEntry(int slot, String itemId, long coins, @Nullable TrophyFisherRank requiredRank) {
    }

    public record Fillet(TrophyFishDefinition fish, TrophyTier tier, int magmafish) {
    }

    public enum ClaimResult { CLAIMED, ALREADY_CLAIMED, NOT_REACHED }

    public enum ShopResult { BOUGHT, LOCKED, TOO_POOR, UNAVAILABLE }
}
