package net.swofty.type.skyblockgeneric.crystalhollows;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prehistoric Egg (wiki: Armadillo Pet; NEU lore): walking with the egg in the inventory gives it a 50% chance at
 * each of 4,000, 10,000, 20,000 and 40,000 blocks to hatch a Common, Uncommon, Rare or Epic Armadillo, and a
 * guaranteed Legendary at 100,000. Blocks walked are counted on the egg stack, and big jumps (teleports) are ignored.
 */
public final class PrehistoricEggs {
    public static final String LORE_HANDLER = "PREHISTORIC_EGG";
    public static final double MAX_STEP_BLOCKS = 24;
    private static final List<Tier> TIERS = List.of(
            new Tier(4_000, 50, Rarity.COMMON),
            new Tier(10_000, 50, Rarity.UNCOMMON),
            new Tier(20_000, 50, Rarity.RARE),
            new Tier(40_000, 50, Rarity.EPIC),
            new Tier(100_000, 100, Rarity.LEGENDARY));
    private static final Map<UUID, Pos> LAST = new ConcurrentHashMap<>();

    public record Tier(int blocks, double chance, Rarity rarity) {
    }

    private PrehistoricEggs() {
    }

    public static void registerLore() {
        LoreRegistry.register(LORE_HANDLER, new LoreConfig(PrehistoricEggs::lore, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static List<Tier> tiers() {
        return TIERS;
    }

    public static double stepLength(Pos from, Pos to) {
        double distance = Math.hypot(to.x() - from.x(), to.z() - from.z());
        return distance > MAX_STEP_BLOCKS ? 0 : distance;
    }

    public static Tier crossed(int before, int after, java.util.function.DoublePredicate roll) {
        for (Tier tier : TIERS) {
            if (before < tier.blocks() && after >= tier.blocks() && roll.test(tier.chance())) return tier;
        }
        return null;
    }

    public static void tick(SkyBlockPlayer player) {
        Pos now = player.getPosition();
        Pos before = LAST.put(player.getUuid(), now);
        if (before == null) return;
        int steps = (int) stepLength(before, now);
        if (steps <= 0) return;
        boolean carrying = false;
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !isEgg(stack)) continue;
            carrying = true;
            advance(player, slot, steps);
        }
        if (!carrying) LAST.remove(player.getUuid());
    }

    public static void forget(SkyBlockPlayer player) {
        LAST.remove(player.getUuid());
    }

    private static boolean isEgg(ItemStack stack) {
        return new SkyBlockItem(stack).getAttributeHandler().getPotentialType() == ItemType.PREHISTORIC_EGG;
    }

    private static void advance(SkyBlockPlayer player, int slot, int steps) {
        int[] counts = new int[2];
        player.updateItemInSlot(slot, item -> {
            counts[0] = item.getAttributeHandler().getBlocksWalked();
            counts[1] = counts[0] + steps;
            item.getAttributeHandler().setBlocksWalked(counts[1]);
        });
        Tier hatched = crossed(counts[0], counts[1], chance -> LootRoll.roll(player.getUuid(), ItemType.ARMADILLO_PET, chance));
        if (hatched != null) hatch(player, slot, hatched);
    }

    private static void hatch(SkyBlockPlayer player, int slot, Tier tier) {
        ItemStack stack = player.getInventory().getItemStack(slot);
        if (stack.amount() > 1) {
            player.updateItemInSlot(slot, item -> item.getAttributeHandler().setBlocksWalked(0));
            player.getInventory().setItemStack(slot, player.getInventory().getItemStack(slot).withAmount(stack.amount() - 1));
        } else {
            player.getInventory().setItemStack(slot, ItemStack.AIR);
        }
        SkyBlockItem pet = new SkyBlockItem(ItemType.ARMADILLO_PET);
        pet.getAttributeHandler().setRarity(tier.rarity());
        player.addAndUpdateItem(pet);
        player.sendMessage(Text.of("<a>Your <f>Prehistoric Egg <a>hatched into an Armadillo Pet!"));
    }

    private static List<String> lore(SkyBlockItem item, SkyBlockPlayer player) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        return List.of("", "<7>Blocks Walked: <b>" + format.format(item.getAttributeHandler().getBlocksWalked()));
    }
}
