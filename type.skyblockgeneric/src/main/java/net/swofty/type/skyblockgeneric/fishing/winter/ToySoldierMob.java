package net.swofty.type.skyblockgeneric.fishing.winter;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.player.ResolvableProfile;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Lv25 Toy Soldier summoned to protect a Nutcracker that isn't killed quickly (wiki: Nutcracker).
 * The wiki gives only its level and 500,000 Health; its damage, skin and lack of drops are guesses.
 */
public class ToySoldierMob extends SkyBlockMob {
    private static final double HEALTH = 500_000;
    private static final double DAMAGE = 250;
    private static final SeaCreatureBehaviour BEHAVIOUR = SeaCreatureBehaviour.aggressive(1.4, 20, 24);

    public ToySoldierMob() {
        super(EntityType.MANNEQUIN);
        String skin = WinterSeaCreature.NUTCRACKER.getSkinTexture();
        if (skin != null) {
            editEntityMeta(MannequinMeta.class, meta -> {
                meta.setProfile(new ResolvableProfile(new PlayerSkin(WinterSeaCreatureMob.encodeSkin(skin), null)));
                meta.setDescription(null);
            });
        }
    }

    @Override
    public String getDisplayName() {
        return "Toy Soldier";
    }

    @Override
    public Integer getLevel() {
        return 25;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return BEHAVIOUR.goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return BEHAVIOUR.targets(this);
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, HEALTH)
                .withBase(ItemStatistic.DAMAGE, DAMAGE)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return null;
    }

    @Override
    public SkillCategories getSkillCategory() {
        return SkillCategories.FISHING;
    }

    @Override
    public long damageCooldown() {
        return 400;
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(0, 0, 0);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.CONSTRUCT, MobType.FROZEN);
    }
}
