package dev.daweapon.type.skyblockgeneric.minion.minions.combat;

import net.minestom.server.color.Color;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.entity.MinionEntityImpl;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.MinionRegistry;
import dev.daweapon.type.skyblockgeneric.minion.SkyBlockMinion;
import dev.daweapon.type.skyblockgeneric.minion.actions.MinionInfernoAction;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoMinionFuelComponent;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoMinionProduction;

import java.util.ArrayList;
import java.util.List;

public class MinionInferno extends SkyBlockMinion {
    public static final int RISING_CELSIUS_PER_MINION = 18;
    public static final int RISING_CELSIUS_CAP = 180;
    private static final int APEX_MINION_TIER = 10;
    private static final Color ARMOR = new Color(178, 34, 34);

    @Override
    public List<MinionTier> getTiers() {
        return List.of(
                tier(1, 1013, 64, "665c54366f88fb3280b1c3fc500ce2b799c8dd327ab6d41c9bc959488f5cfd92"),
                tier(2, 982, 192, "22104034909623d6094a5060e05af6da5cbf752a18580911368fa36d69492af3"),
                tier(3, 950, 192, "2c01ba652a23b19dbb1d59ebab0b308eb72861d5bcd7d42630316c3e6542e01a"),
                tier(4, 919, 384, "83abec14acbc4938952fb5986cb14583484ccb7de62d1b9c2b41196a9876c3ce"),
                tier(5, 886, 384, "2a6e8184c3299b293af75774918c48722afaf3309da5290e0f258048f2aadf66"),
                tier(6, 855, 576, "1b6665f5be583328d1021f0ae6f973c7d9e728b1ac465c40c87a779af06587bf"),
                tier(7, 823, 576, "6bda967f41e0ea42289abfb661c7c71b28045d488a0f10e13d78b653c25f8f0d"),
                tier(8, 792, 768, "513f8ed44fb2f82ade92bb56a606be3b595cd716b1bbf0950f0866cc6393d25b"),
                tier(9, 760, 768, "86d467092efb156707c254f168ff03b0b763a74fe9f8ac15616ea4c97adfaf69"),
                tier(10, 728, 960, "65c70f90a11c2d47feed7d1317b1c1b7fe93706a192a983af9a81a5170dbe71b"),
                tier(11, 697, 960, "ec7cc5d8dea1eac6aa307b9ad32b79da0af7d16bad0fd8e34cc5735739db9753"));
    }

    private static MinionTier tier(int tier, float seconds, int storage, String texture) {
        return new MinionTier(tier, seconds, storage, texture, Material.BLAZE_ROD, true);
    }

    @Override
    public Color getBootColour() {
        return ARMOR;
    }

    @Override
    public Color getLeggingsColour() {
        return ARMOR;
    }

    @Override
    public Color getChestplateColour() {
        return ARMOR;
    }

    @Override
    public List<MinionExpectation> getExpectations() {
        return List.of(new MobGapExpectation(2));
    }

    @Override
    public MinionAction getAction() {
        return new MinionInfernoAction();
    }

    @Override
    public List<String> getDescription(String name) {
        return List.of(
                "<7>Place this minion and it will start",
                "<7>generating and slaying Infernals!",
                "<7>Minions also work when you are",
                "<7>offline!");
    }

    @Override
    public List<String> getAbilityLore(int tier) {
        List<String> lore = new ArrayList<>(List.of(
                "<6>Ability: Rising Celsius",
                "<7>Each Inferno minion increases the",
                "<7>speed of ALL Inferno minions by <a>" + RISING_CELSIUS_PER_MINION + "%<7>,",
                "<7>up to <a>" + RISING_CELSIUS_CAP + "%<7>."));
        if (tier >= APEX_MINION_TIER) {
            lore.addAll(List.of(
                    "",
                    "<6>Ability: Apex Minion",
                    "<7>When creating an Inferno Apex,",
                    "<7>create 2."));
        }
        return lore;
    }

    @Override
    public int getBonusSpeedPercentage(IslandMinionData.IslandMinion minion) {
        return risingCelsius(infernoMinionsOnIsland(minion));
    }

    @Override
    public double getSpeedMultiplier(IslandMinionData.IslandMinion minion) {
        InfernoMinionFuelComponent fuel = InfernoMinionProduction.activeFuel(minion);
        return fuel == null ? 1 : fuel.getGrade().actionTimeDivisor();
    }

    public static int risingCelsius(int infernoMinions) {
        return Math.min(RISING_CELSIUS_CAP, infernoMinions * RISING_CELSIUS_PER_MINION);
    }

    private static int infernoMinionsOnIsland(IslandMinionData.IslandMinion minion) {
        MinionEntityImpl self = minion.getMinionEntity();
        if (self == null || self.getInstance() == null) return 1;
        synchronized (MinionEntityImpl.getActiveMinions()) {
            return (int) MinionEntityImpl.getActiveMinions().stream()
                    .filter(entity -> entity.getInstance() == self.getInstance())
                    .filter(entity -> entity.getIslandMinion().getMinion() == MinionRegistry.INFERNO)
                    .count();
        }
    }
}
