package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.MiningValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.PlayerEnchantmentHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Efficiency enchantment: grants mining speed and reduces block-breaking time on pickaxes.
public class EnchantmentEfficiency extends SkyBlockValueEvent implements Ench, EnchFromTable {
    @Override
    public String getDescription(int level) {
        return "<7>Grants <6>+" + (30 + ((level - 1) * 20)) + " Mining Speed<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 9,
                2, 13,
                3, 18,
                4, 23,
                5, 27
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.EFFICIENCY_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.TOOLS, EnchantItemGroups.PICKAXE, EnchantItemGroups.DRILL, EnchantItemGroups.GAUNTLET, EnchantItemGroups.AXE, EnchantItemGroups.HOE, EnchantItemGroups.SHOVEL);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 15,
                3, 20,
                4, 25,
                5, 30
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.EFFICIENCY_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return MiningValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        PlayerEnchantmentHandler enchantmentHandler = event.getPlayer().getEnchantmentHandler();

        PlayerEnchantmentHandler.EnchantmentHandlerResponse response = enchantmentHandler.getItemWithHighestLevelOf(
                EnchantmentType.EFFICIENCY,
                PlayerEnchantmentHandler.EnchantedItemSource.HAND
        );

        if (response == null) return;

        int enchantLevel = response.level();
        double initialValue = (double) event.getValue();

        double newValue = initialValue - (initialValue * (0.3 + ((enchantLevel - 1) * 0.2)));
        event.setValue(newValue);
    }
}
