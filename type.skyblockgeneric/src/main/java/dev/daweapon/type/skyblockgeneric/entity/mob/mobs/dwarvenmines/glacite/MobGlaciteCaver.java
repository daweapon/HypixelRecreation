package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite;

import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;

public class MobGlaciteCaver extends GlaciteShaftMob {
    public MobGlaciteCaver() {
        super(EntityType.ZOMBIE);
    }

    @Override
    public void onInit() {
        setHelmet(head(texture()));
        setItemInMainHand(ItemStack.of(Material.IRON_PICKAXE));
    }

    @Override
    public Integer getLevel() {
        return 175;
    }

    @Override
    public String getMobID() {
        return "GLACITE_CAVER";
    }

    @Override
    public String getDisplayName() {
        return "Glacite Caver";
    }

    @Override
    protected double baseHealth() {
        return 800_000;
    }

    @Override
    protected double attackDamage() {
        return 2_500;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new VanillaMeleeAttackGoal(this, 1.8, 20, TimeUnit.SERVER_TICK));
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(262, 58, 192);
    }
}
