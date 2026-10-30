package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite;

import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event.RangedAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;

import java.util.List;

public class MobGlaciteMage extends GlaciteShaftMob {
    private static final double RANGE = 14;
    private static final long COOLDOWN_MS = 2500;

    public MobGlaciteMage() {
        super(EntityType.ZOMBIE);
    }

    @Override
    public void onInit() {
        setHelmet(head(texture()));
        setItemInMainHand(ItemStack.of(Material.BLAZE_ROD));
    }

    @Override
    public Integer getLevel() {
        return 177;
    }

    @Override
    public String getMobID() {
        return "GLACITE_MAGE";
    }

    @Override
    public String getDisplayName() {
        return "Glacite Mage";
    }

    @Override
    protected double baseHealth() {
        return 825_000;
    }

    @Override
    protected double attackDamage() {
        return 2_550;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new RangedAttackGoal(this, RANGE, COOLDOWN_MS, (mob, player) -> ((MobGlaciteMage) mob).fire(player, Particle.WITCH, 8)));
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(265, 59, 194);
    }
}
