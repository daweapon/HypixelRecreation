package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointWardrobe;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookFear;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

public final class FearnesyHandler implements PetAbilityHandler {
    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double fear = wardrobeBonus(player);
        if (fear <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.FEAR, fear).build();
    }

    public static double wardrobeBonus(SkyBlockPlayer player) {
        DatapointWardrobe.WardrobeData wardrobe = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class).getValue();
        double best = 0;
        DatapointWardrobe.ArmorSet[] sets = wardrobe.getSets();
        for (int slot = 0; slot < sets.length; slot++) {
            if (slot == wardrobe.getEquippedSlot()) continue;
            double total = 0;
            for (SkyBlockItem piece : sets[slot].getPieces()) total += fearOf(piece);
            best = Math.max(best, total);
        }
        double worn = 0;
        for (ItemStack stack : new ItemStack[]{player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots()}) {
            if (stack == null || stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            worn += fearOf(new SkyBlockItem(stack));
        }
        return Math.max(0, best - worn);
    }

    private static double fearOf(SkyBlockItem piece) {
        if (piece == null || piece.isNA() || piece.isAir()) return 0;
        String id = piece.getAttributeHandler().getTypeAsString();
        if (id == null || !GreatSpookFear.ARMOR.contains(id)) return 0;
        return piece.getAttributeHandler().getStatistics().getOverall(ItemStatistic.FEAR);
    }
}
