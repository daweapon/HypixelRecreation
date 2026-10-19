package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MiningEventRewards {
    private MiningEventRewards() {
    }

    public static void grant(SkyBlockPlayer player, MiningEventConfig.RewardTier tier, long count) {
        if (tier.miningXp() > 0) player.getSkills().increase(player, SkillCategories.MINING, (double) tier.miningXp());
        if (tier.hotmXp() > 0) HotmService.addExperience(player, tier.hotmXp());
        long powderBase = tier.mithrilPowder() + tier.perKillPowder() * count;
        powderBase += Math.round(powderBase * ReforgeLoadout.eventPowderPercent(player) / 100);
        long powder = powderBase > 0 ? HotmService.gainPowder(player, HotmPowder.MITHRIL, powderBase) : 0;
        boolean bits = tier.bits() > 0 && player.isBoosterCookieActive();
        if (bits) player.addBits(tier.bits());
        for (int index = 0; index < tier.items(); index++) giveMiningItem(player);

        player.sendMessage("<7>Rewards:");
        if (tier.miningXp() > 0) player.sendMessage("<8> +<3>{} Mining XP", StringUtility.commaify((int) tier.miningXp()));
        if (tier.hotmXp() > 0) player.sendMessage("<8> +<5>{} HotM EXP", StringUtility.commaify((int) tier.hotmXp()));
        if (powder > 0) player.sendMessage("<8> +<2>{} Mithril Powder", StringUtility.commaify((int) powder));
        if (bits) player.sendMessage("<8> +<b>{} Bits", tier.bits());
        if (tier.miningXp() + powder + tier.items() == 0) player.sendMessage("<8> Nothing this time.");
    }

    public static void giveMiningItem(SkyBlockPlayer player) {
        List<String> pool = MiningEventConfig.get().strings("mining_items");
        String id = LootRoll.pick(player.getUuid(), pool, MiningEventRewards::forcedType, entry -> 1D, ThreadLocalRandom.current());
        SkyBlockItem item = build(id);
        player.addAndUpdateItem(item);
        player.sendMessage("<8> +<f>{}", item.getDisplayName());
    }

    static SkyBlockItem build(String id) {
        if (id.equals("FLOWSTATE_BOOK")) {
            SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
            book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.FLOWSTATE, 1));
            return book;
        }
        return new SkyBlockItem(ItemType.valueOf(id));
    }

    private static ItemType forcedType(String id) {
        return id.equals("FLOWSTATE_BOOK") ? ItemType.ENCHANTED_BOOK : ItemType.valueOf(id);
    }
}
