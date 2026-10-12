package net.swofty.type.skyblockgeneric.fossil;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.RareDropAward;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FossilService {
    private static final double DYE_FOSSIL_CHANCE = 1.0 / 500_000 * 100;

    public static final String ESSENCE_TEXTURE = "93a1b830399ab432a5178fdaf3939b24bf25c724a66be947296c503352bc380d";
    public static final int DONATION_MINING_XP = 25_000;
    public static final int DONATION_POWDER = 20_000;
    public static final int DONATION_ESSENCE = 100;
    public static final int DONATION_HOTM_XP = 5_000;
    public static final int DONATION_SKYBLOCK_XP = 10;
    public static final int COMPLETION_POWDER = 50_000;
    public static final int COMPLETION_HOTM_XP = 15_000;
    public static final int SCRAP_ESSENCE_COST = 100;
    public static final int MUNCHER_POWDER = 25_000;
    public static final int MUNCHER_ESSENCE = 100;
    public static final long MUNCHER_COOLDOWN_MILLIS = 24L * 60 * 60 * 1000;

    public enum Donation {DONATED, ALREADY_DONATED, NO_FOSSIL}

    private FossilService() {}

    public static Set<String> donated(SkyBlockPlayer player) {
        return HotmService.data(player).getDonatedFossils();
    }

    public static boolean hasDonated(SkyBlockPlayer player, FossilType type) {
        return donated(player).contains(type.name());
    }

    public static boolean allDonated(SkyBlockPlayer player) {
        return donated(player).size() >= FossilType.values().length;
    }

    public static Donation donate(SkyBlockPlayer player, FossilType type) {
        if (hasDonated(player, type)) return Donation.ALREADY_DONATED;
        if (!player.removeItemsFromPlayer(Map.of(type.getItem(), 1))) return Donation.NO_FOSSIL;
        markDonated(player, type);
        player.getSkills().increase(player, SkillCategories.MINING, (double) DONATION_MINING_XP);
        grantPowder(player, DONATION_POWDER);
        Essences.add(player, EssenceType.FOSSIL, DONATION_ESSENCE);
        HotmService.addExperience(player, DONATION_HOTM_XP);
        if (allDonated(player)) {
            grantPowder(player, COMPLETION_POWDER);
            HotmService.addExperience(player, COMPLETION_HOTM_XP);
        }
        return Donation.DONATED;
    }

    public static void markDonated(SkyBlockPlayer player, FossilType type) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.getDonatedFossils().add(type.name());
        HotmService.save(player, data);
        syncSkyBlockXp(player);
    }

    public static void donateAll(SkyBlockPlayer player) {
        for (FossilType type : FossilType.values()) markDonated(player, type);
    }

    public static void syncSkyBlockXp(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.FOSSIL_RESEARCH,
                (double) DONATION_SKYBLOCK_XP * donated(player).size());
    }

    private static void grantPowder(SkyBlockPlayer player, long amount) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        data.addGlacitePowder(amount);
        HotmService.save(player, data);
    }

    public static String recipeLock(SkyBlockPlayer player, String recipeId) {
        FossilType type = FossilType.byPetRecipe(recipeId);
        if (type == null || !type.unlocksPetRecipe() || hasDonated(player, type)) return null;
        return "<c>Donate a " + type.getDisplayName() + " to Dr. Stone to unlock this recipe.";
    }

    public static List<ItemStack> chiselsIn(SkyBlockPlayer player) {
        List<ItemStack> chisels = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
            if (type != null && ChiselStats.isChisel(type)) chisels.add(stack);
        }
        return chisels;
    }

    public static ChiselStats statsOf(ItemStack chisel) {
        SkyBlockItem item = new SkyBlockItem(chisel);
        List<ItemType> gems = new ArrayList<>();
        if (item.hasComponent(net.swofty.type.skyblockgeneric.item.components.GemstoneComponent.class)) {
            item.getAttributeHandler().getGemData().slots.forEach(slot -> gems.add(slot.filledWith));
        }
        return ChiselStats.of(item.getAttributeHandler().getPotentialType(), gems);
    }

    public static boolean canUse(SkyBlockPlayer player, ChiselStats stats) {
        return HotmService.data(player).getTier() >= stats.hotmTier();
    }

    public static void sendSummary(SkyBlockPlayer player, ExcavatorGame game, List<ExcavatorLoot.Drop> drops) {
        player.sendMessage("<2><m>" + "-".repeat(40));
        player.sendMessage(game.isCompleted() ? "<6><l>EXCAVATION COMPLETE" : "<6><l>EXCAVATION OVER");
        player.sendMessage("");
        player.sendMessage("<6><l>REWARDS");
        if (game.isCompleted() && game.fossil() != null) {
            player.sendMessage("  <6>" + game.fossil().getDisplayName());
        }
        for (ExcavatorLoot.Drop drop : drops) {
            player.sendMessage("  " + describe(drop));
        }
        player.sendMessage("<2><m>" + "-".repeat(40));
    }

    private static String describe(ExcavatorLoot.Drop drop) {
        String amount = " <8>x" + StringUtility.commaify(drop.amount());
        return switch (drop.kind()) {
            case ESSENCE -> "<d>Fossil Essence" + amount;
            case POWDER -> HotmPowder.GLACITE.coloured() + amount;
            case BOOK -> "<9>Enchanted Book <7>(Paleontologist I)";
            case ITEM -> new SkyBlockItem(drop.item()).getDisplayName() + (drop.amount() > 1 ? amount : "");
        };
    }

    public static void award(SkyBlockPlayer player, ExcavatorGame game) {
        List<ExcavatorLoot.Drop> drops = game.uncoveredRewards();
        for (ExcavatorLoot.Drop drop : drops) {
            switch (drop.kind()) {
                case ESSENCE -> Essences.add(player, EssenceType.FOSSIL, drop.amount());
                case POWDER -> HotmService.gainPowder(player, HotmPowder.GLACITE, drop.amount());
                case BOOK -> player.addAndUpdateItem(paleontologistBook());
                case ITEM -> player.addAndUpdateItem(new SkyBlockItem(drop.item(), drop.amount()));
            }
        }
        if (game.isCompleted() && game.fossil() != null) player.addAndUpdateItem(new SkyBlockItem(game.fossil().getItem()));
        sendSummary(player, game, drops);
        RareDropAward.tryAward(player, ItemType.DYE_FOSSIL, DYE_FOSSIL_CHANCE);
    }

    public static SkyBlockItem paleontologistBook() {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.PALEONTOLOGIST, 1));
        return book;
    }
}
