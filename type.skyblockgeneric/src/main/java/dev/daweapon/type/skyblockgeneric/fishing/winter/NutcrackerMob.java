package dev.daweapon.type.skyblockgeneric.fishing.winter;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Nutcracker charges quickly through water and crawls on land, and if it isn't killed quickly
 * it summons Lv25 Toy Soldiers to protect it (wiki: Nutcracker). The wiki gives no timer; here the
 * first soldier comes after 20 seconds and another every 20 seconds after, at most three at once.
 */
public class NutcrackerMob extends WinterSeaCreatureMob {
    static final int SOLDIER_INTERVAL_TICKS = 400;
    static final int MAX_SOLDIERS = 3;
    private static final double WATER_SPEED = 300;
    private static final double LAND_SPEED = 60;
    private static final double ANNOUNCE_RANGE = 32;

    private final List<ToySoldierMob> soldiers = new ArrayList<>();
    private boolean wasInWater;

    public NutcrackerMob(@Nullable SkyBlockPlayer angler) {
        super(WinterSeaCreature.NUTCRACKER, angler);
    }

    @Override
    protected void onTick(int ticks) {
        updateSpeed();
        if (ticks % SOLDIER_INTERVAL_TICKS == 0) summonSoldier();
    }

    private void updateSpeed() {
        Instance instance = getInstance();
        if (instance == null) return;
        boolean inWater = instance.getBlock(getPosition()).compare(Block.WATER);
        if (inWater == wasInWater) return;
        wasInWater = inWater;
        double speed = inWater ? WATER_SPEED : LAND_SPEED;
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue((float) (speed / 1000 * 2.5));
    }

    private void summonSoldier() {
        soldiers.removeIf(soldier -> soldier.isRemoved() || soldier.isDead());
        Instance instance = getInstance();
        if (instance == null || soldiers.size() >= MAX_SOLDIERS) return;
        ToySoldierMob soldier = new ToySoldierMob();
        soldier.setInstance(instance, getPosition());
        soldiers.add(soldier);
        Text message = Text.of("<6><l>CRACK! </l><r>A <c>Toy Soldier <f>has come to protect the <c>Nutcracker<f>!");
        MythologicalCombat.nearby(this, ANNOUNCE_RANGE).forEach(player -> player.sendMessage(message));
    }

    @Override
    public void kill() {
        dismissSoldiers();
        super.kill();
    }

    @Override
    public void remove() {
        dismissSoldiers();
        super.remove();
    }

    private void dismissSoldiers() {
        soldiers.forEach(soldier -> {
            if (!soldier.isRemoved()) soldier.remove();
        });
        soldiers.clear();
    }
}
