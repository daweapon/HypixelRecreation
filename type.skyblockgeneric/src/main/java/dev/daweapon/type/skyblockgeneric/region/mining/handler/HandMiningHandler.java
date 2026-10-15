package dev.daweapon.type.skyblockgeneric.region.mining.handler;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.components.DrillComponent;
import dev.daweapon.type.skyblockgeneric.item.components.PickaxeComponent;

import java.util.Collections;
import java.util.List;

public class HandMiningHandler implements SkyBlockMiningHandler {
    private final int handBreakTimeTicks;
    private final int pickaxeBreakTimeTicks;

    public HandMiningHandler(int handBreakTimeTicks, int pickaxeBreakTimeTicks) {
        this.handBreakTimeTicks = handBreakTimeTicks;
        this.pickaxeBreakTimeTicks = pickaxeBreakTimeTicks;
    }

    @Override
    public List<Class<? extends SkyBlockItemComponent>> getValidToolComponents() {
        return Collections.emptyList();
    }

    @Override
    public double getStrength() {
        return handBreakTimeTicks;
    }

    @Override
    public int getMiningPowerRequirement() {
        return 0;
    }

    @Override
    public boolean usesVanillaBreakTime() {
        return true;
    }

    @Override
    public int getFixedBreakTime() {
        return handBreakTimeTicks;
    }

    @Override
    public int getBreakTimeForTool(SkyBlockItem tool) {
        if (tool != null && (tool.hasComponent(PickaxeComponent.class) || tool.hasComponent(DrillComponent.class))) {
            return pickaxeBreakTimeTicks;
        }
        return handBreakTimeTicks;
    }

    @Override
    public boolean canToolBreak(SkyBlockItem tool) {
        return true;
    }

    @Override
    public String getHandlerName() {
        return "Hand";
    }
}
