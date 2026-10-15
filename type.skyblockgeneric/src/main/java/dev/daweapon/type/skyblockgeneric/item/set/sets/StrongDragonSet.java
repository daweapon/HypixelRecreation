package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilities;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorWorn;
import dev.daweapon.type.skyblockgeneric.item.set.impl.PlayerArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class StrongDragonSet implements PlayerArmorSet {
    private static final String AOTE = "ASPECT_OF_THE_END";

    public static boolean isWearing(SkyBlockPlayer player) {
        return ArmorWorn.fullSet(player, "STRONG_DRAGON");
    }

    public static double aoteDamage(boolean holdingAote) {
        return holdingAote ? ArmorAbilities.STRONG_BLOOD_DAMAGE : 0;
    }

    @Override
    public String getName() {
        return "Strong Blood";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Improves <9>Aspect of the End",
                "<8>⋗ <c>+75 Damage",
                "",
                "<7>Instant Transmission:",
                "<8>⋗ <a>+2 <7>teleport range",
                "<8>⋗ <a>+3 <7>seconds",
                "<8>⋗ <c>+5 Strength <7>on cast"));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
        boolean holding = !held.isAir() && AOTE.equals(held.getAttributeHandler().getTypeAsString());
        double damage = aoteDamage(holding);
        return damage == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damage).build();
    }
}
