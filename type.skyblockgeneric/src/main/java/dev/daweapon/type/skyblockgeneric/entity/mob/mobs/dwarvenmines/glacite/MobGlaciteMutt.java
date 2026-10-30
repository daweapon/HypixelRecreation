package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.glacite;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;

public class MobGlaciteMutt extends GlaciteShaftMob {
    private static final String TEXTURE = "69d1d3113ec43ac2961dd59f28175fb4718873c6c448dfca8722317d67";

    public MobGlaciteMutt() {
        super(EntityType.WOLF);
    }

    @Override
    protected String texture() {
        return TEXTURE;
    }

    @Override
    public Integer getLevel() {
        return 173;
    }

    @Override
    public String getMobID() {
        return "GLACITE_MUTT";
    }

    @Override
    public String getDisplayName() {
        return "Glacite Mutt";
    }

    @Override
    protected double baseHealth() {
        return 750_000;
    }

    @Override
    protected double attackDamage() {
        return 2_500;
    }

    @Override
    protected double baseSpeed() {
        return 160;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(new VanillaMeleeAttackGoal(this, 1.8, 20, TimeUnit.SERVER_TICK));
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.GLACIAL, MobType.ANIMAL);
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(259, 57, 190);
    }
}
