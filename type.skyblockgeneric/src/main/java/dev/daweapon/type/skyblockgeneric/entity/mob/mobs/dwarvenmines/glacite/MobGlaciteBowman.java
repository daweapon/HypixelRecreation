package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite;

import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.RangedAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class MobGlaciteBowman extends GlaciteShaftMob {
    private static final double RANGE = 16;
    private static final long COOLDOWN_MS = 2000;

    public MobGlaciteBowman() {
        super(EntityType.ZOMBIE);
    }

    @Override
    public void onInit() {
        setHelmet(head(texture()));
        setItemInMainHand(ItemStack.of(Material.BOW));
    }

    @Override
    public Integer getLevel() {
        return 174;
    }

    @Override
    public String getMobID() {
        return "GLACITE_BOWMAN";
    }

    @Override
    public String getDisplayName() {
        return "Glacite Bowman";
    }

    @Override
    protected double baseHealth() {
        return 750_000;
    }

    @Override
    protected double attackDamage() {
        return 2_550;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new RangedAttackGoal(this, RANGE, COOLDOWN_MS, (mob, player) -> ((MobGlaciteBowman) mob).fire(player, Particle.CRIT, 6)));
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(261, 58, 191);
    }
}
