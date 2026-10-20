package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.CapTier;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Fortune;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Give;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Item;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.LevelCap;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.PersonalBest;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.Power;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.PowerTier;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynConfig.ShopEntry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class StarlynShop {
    public static final String LEVEL_CAP_ID = "level_cap";

    private StarlynShop() {
    }

    public static int owned(SkyBlockPlayer player, ItemType type) {
        return player.getAmountInInventory(type) + player.getSackItems().getAmount(type);
    }

    public static boolean affordable(SkyBlockPlayer player, StarlynSister sister, StarlynCost cost) {
        for (Map.Entry<ItemType, Integer> required : cost.required(sister).entrySet()) {
            if (owned(player, required.getKey()) < required.getValue()) return false;
        }
        return true;
    }

    public static boolean ownsAward(SkyBlockPlayer player, CustomCollectionAward award) {
        if (player.getSkyblockDataHandler() == null) return false;
        for (StarlynSister sister : StarlynSister.values()) {
            int index = sister.getLevelCapAwards().indexOf(award);
            if (index >= 0) return StarlynContests.data(player, sister).purchased(LEVEL_CAP_ID) > index;
        }
        return false;
    }

    public static int levelCapsOwned(SkyBlockPlayer player, StarlynSister sister) {
        return StarlynContests.data(player, sister).purchased(LEVEL_CAP_ID);
    }

    public static @Nullable CapTier nextCap(SkyBlockPlayer player, StarlynSister sister, LevelCap entry) {
        int owned = levelCapsOwned(player, sister);
        return owned >= entry.tiers().size() ? null : entry.tiers().get(owned);
    }

    public static @Nullable PowerTier nextPower(SkyBlockPlayer player, StarlynSister sister, Power entry) {
        int owned = StarlynContests.data(player, sister).purchased(entry.id());
        return owned >= entry.tiers().size() ? null : entry.tiers().get(owned);
    }

    public static double sweepFromPower(SkyBlockPlayer player, StarlynSister sister, Power entry) {
        int owned = StarlynContests.data(player, sister).purchased(entry.id());
        return owned <= 0 ? 0 : entry.tiers().get(Math.min(owned, entry.tiers().size()) - 1).sweep();
    }

    public static SkyBlockItem create(Give give, int amount) {
        return switch (give.type()) {
            case "ITEM" -> new SkyBlockItem(give.item(), amount);
            case "PET" -> {
                SkyBlockItem pet = new SkyBlockItem(give.item(), amount);
                pet.getAttributeHandler().setRarity(give.rarity() == null ? Rarity.COMMON : give.rarity());
                yield pet;
            }
            case "BOOK" -> {
                SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK, amount);
                book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.valueOf(give.enchant()), give.level()));
                yield book;
            }
            case "SHARD" -> {
                AttributeDefinition definition = AttributeRegistry.get(give.shard());
                if (definition == null) throw new IllegalStateException("Unknown shard " + give.shard());
                yield AttributeShardComponent.create(definition, amount);
            }
            default -> throw new IllegalArgumentException("Unknown give type " + give.type());
        };
    }

    public static @Nullable String purchase(SkyBlockPlayer player, StarlynSister sister, String entryId) {
        StarlynConfig config = StarlynConfig.get(sister);
        ShopEntry entry = config.entry(entryId);
        if (entry == null) return "<c>That item does not exist.";
        StarlynProfile profile = StarlynContests.profile(player);
        StarlynProfile.SisterData data = profile.sister(sister);
        int milestone = data.getMilestoneGranted();
        String error = switch (entry) {
            case LevelCap cap -> buyCap(player, sister, data, milestone, cap);
            case Power power -> buyPower(player, sister, data, milestone, power);
            case Fortune fortune -> buyFortune(player, sister, data, milestone, fortune);
            case PersonalBest best -> buyPersonalBest(player, sister, data, milestone, best);
            case Item item -> buyItem(player, sister, milestone, item);
        };
        if (error == null) StarlynContests.save(player, profile);
        return error;
    }

    private static @Nullable String buyCap(SkyBlockPlayer player, StarlynSister sister, StarlynProfile.SisterData data,
                                           int milestone, LevelCap cap) {
        CapTier tier = nextCap(player, sister, cap);
        if (tier == null) return "<c>You already unlocked every Foraging level cap here.";
        String error = check(player, sister, milestone, tier.milestone(), tier.cost());
        if (error != null) return error;
        pay(player, sister, tier.cost());
        data.setPurchased(LEVEL_CAP_ID, data.purchased(LEVEL_CAP_ID) + 1);
        player.sendMessage("<a>You unlocked <e>+1 Foraging Level Cap<a>!");
        return null;
    }

    private static @Nullable String buyPower(SkyBlockPlayer player, StarlynSister sister, StarlynProfile.SisterData data,
                                             int milestone, Power power) {
        PowerTier tier = nextPower(player, sister, power);
        if (tier == null) return "<c>This is already at its maximum level.";
        StarlynCost cost = StarlynCost.coupons(tier.coupons());
        String error = check(player, sister, milestone, tier.milestone(), cost);
        if (error != null) return error;
        pay(player, sister, cost);
        data.setPurchased(power.id(), data.purchased(power.id()) + 1);
        player.sendMessage("<a>You unlocked <e>" + power.name() + " " + data.purchased(power.id()) + "<a>! <7>(<2>+" + (int) tier.sweep() + "∮ Sweep<7>)");
        return null;
    }

    private static @Nullable String buyFortune(SkyBlockPlayer player, StarlynSister sister, StarlynProfile.SisterData data,
                                               int milestone, Fortune fortune) {
        int level = data.purchased(fortune.id()) + 1;
        if (level > fortune.maxLevel()) return "<c>This is already at its maximum level.";
        StarlynCost cost = fortune.cost(level);
        String error = check(player, sister, milestone, fortune.milestone(), cost);
        if (error != null) return error;
        pay(player, sister, cost);
        data.setPurchased(fortune.id(), level);
        player.sendMessage("<a>You unlocked <e>" + fortune.name() + " " + level + "<a>! <7>(<6>+" + fortune.fortune(level) + " " + fortune.name() + "<7>)");
        return null;
    }

    private static @Nullable String buyPersonalBest(SkyBlockPlayer player, StarlynSister sister, StarlynProfile.SisterData data,
                                                    int milestone, PersonalBest best) {
        if (data.purchased(best.id()) > 0) return "<c>You already own this perk.";
        StarlynCost cost = StarlynCost.coupons(best.coupons());
        String error = check(player, sister, milestone, best.milestone(), cost);
        if (error != null) return error;
        pay(player, sister, cost);
        data.setPurchased(best.id(), 1);
        player.sendMessage("<a>You unlocked <e>" + best.name() + "<a>!");
        return null;
    }

    private static @Nullable String buyItem(SkyBlockPlayer player, StarlynSister sister, int milestone, Item item) {
        String error = check(player, sister, milestone, item.milestone(), item.cost());
        if (error != null) return error;
        pay(player, sister, item.cost());
        SkyBlockItem created = create(item.give(), 1);
        player.addAndUpdateItem(created);
        player.sendMessage("<a>You bought " + created.getDisplayName() + "<a>!");
        return null;
    }

    private static @Nullable String check(SkyBlockPlayer player, StarlynSister sister, int milestone, int required, StarlynCost cost) {
        if (milestone < required) return "<c>You need " + sister.milestoneName() + " " + roman(required) + " for this!";
        if (!affordable(player, sister, cost)) return "<c>You can't afford this!";
        return null;
    }

    private static void pay(SkyBlockPlayer player, StarlynSister sister, StarlynCost cost) {
        player.removeItemsFromPlayer(cost.required(sister));
    }

    public static String roman(int number) {
        return dev.daweapon.commons.StringUtility.getAsRomanNumeral(number);
    }
}
