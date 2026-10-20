package dev.daweapon.type.skyblockgeneric.crimson.mob;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.drop.ItemDrops;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.ItemLore;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ArmorComponent;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassives;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.loottable.LootAffector;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Rolls a Crimson Isle drop table for one player through {@link LootRoll} and hands the items over the way
 * every SkyBlock mob does: sacks first, then the inventory, then the ground.
 */
public final class CrimsonMobLoot {
    private static final double RARE_DROP_CHANCE_PERCENT = 5;

    private CrimsonMobLoot() {
    }

    public record Won(MobDrop drop, SkyBlockItem item) {
        public boolean rare() {
            return drop.chancePercent() < RARE_DROP_CHANCE_PERCENT;
        }
    }

    public static List<Won> roll(SkyBlockPlayer player, SkyBlockMob mob, List<MobDrop> drops) {
        List<Won> won = rollItems(player, mob, drops);
        won.forEach(entry -> deliver(player, mob, entry.item(), entry.rare()));
        return won;
    }

    public static List<Won> rollItems(SkyBlockPlayer player, SkyBlockMob mob, List<MobDrop> drops) {
        List<Won> won = new ArrayList<>();
        for (MobDrop drop : drops) {
            ItemType type = rollOne(player, mob, drop);
            if (type != null) won.add(new Won(drop, create(player, mob, type, drop)));
        }
        return won;
    }

    private static ItemType rollOne(SkyBlockPlayer player, SkyBlockMob mob, MobDrop drop) {
        List<ItemType> items = drop.availableItems();
        if (items.isEmpty()) return null;
        double chance = drop.magicFind() ? boosted(player, mob, items.getFirst(), drop.chancePercent()) : drop.chancePercent();
        return decide(player.getUuid(), items, chance, ThreadLocalRandom.current());
    }

    public static @Nullable ItemType decide(UUID player, List<ItemType> items, double chance, RandomGenerator random) {
        if (items.isEmpty()) return null;
        if (items.size() == 1) {
            return LootRoll.roll(player, items.getFirst(), chance, random) ? items.getFirst() : null;
        }
        ItemType forced = LootRoll.claim(player, items);
        if (forced != null) return forced;
        if (!LootRoll.roll(player, null, chance, random)) return null;
        return LootRoll.pick(player, items, type -> type, type -> 1, random);
    }

    private static double boosted(SkyBlockPlayer player, SkyBlockMob mob, ItemType type, double chance) {
        double adjusted = LootAffector.MAGIC_FIND.getAffector().apply(player, chance, mob);
        LootAffector enchant = new SkyBlockItem(type).hasComponent(ArmorComponent.class)
                ? LootAffector.ENCHANTMENT_LUCK : LootAffector.ENCHANTMENT_LOOTING;
        double enchanted = enchant.getAffector().apply(player, adjusted, mob);
        return new SkyBlockItem(type).hasComponent(ArmorComponent.class) ? CombatPetHooks.armorDropChance(player, enchanted) : enchanted;
    }

    private static SkyBlockItem create(SkyBlockPlayer player, SkyBlockMob mob, ItemType type, MobDrop drop) {
        int amount = EquipmentPassives.magmaCream(player, mob, type, drop.rollAmount());
        SkyBlockItem item = new SkyBlockItem(type, CombatPetHooks.collectionDropAmount(player, type, amount));
        if (drop.rarity() != null) item.getAttributeHandler().setRarity(drop.rarity());
        EnchantmentType enchantment = drop.enchantmentType();
        if (enchantment != null) {
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(enchantment, drop.enchantmentLevel()));
        }
        return item;
    }

    public static void deliver(SkyBlockPlayer player, SkyBlockMob mob, SkyBlockItem item, boolean rare) {
        if (rare) announceRareDrop(player, mob, item);
        ItemType type = item.getAttributeHandler().getPotentialType();
        int amount = item.getAmount();
        if (type != null && player.canInsertItemIntoSacks(type, amount)) {
            player.getSackItems().increase(type, amount);
        } else if (player.getSkyBlockExperience().getLevel().asInt() >= 6 || mob.getInstance() == null) {
            player.addAndUpdateItem(item);
        } else {
            Pos at = mob.getPosition().add(0, 0.5, 0);
            ItemDrops.dropAt(new DroppedItemEntityImpl(item, player), mob.getInstance(), at);
        }
    }

    public static void announceRareDrop(SkyBlockPlayer player, SkyBlockMob mob, SkyBlockItem item) {
        Text name = item.getDisplayNameText();
        List<Text> enchantments = item.getAttributeHandler().getEnchantments().map(ItemLore::enchantmentTitle).toList();
        if (!enchantments.isEmpty()) {
            name = Text.of("{} <a>({}<a>)", name, Text.join(Text.of("<a>, "), enchantments));
        }
        double magicFind = player.getStatistics().allStatistics(player, mob).getOverall(ItemStatistic.MAGIC_FIND);
        player.sendMessage(magicFind > 0
                ? Text.of("<6><l>RARE DROP!</l> {} <b>(+{}% ✯ Magic Find)", name, Math.round(magicFind))
                : Text.of("<6><l>RARE DROP!</l> {}", name));
    }
}
