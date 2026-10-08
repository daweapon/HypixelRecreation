package net.swofty.type.skyblockgeneric.fishing.lava;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * One of the three Baby Magma Slugs a Magma Slug splits into. Only the last of the litter to die drops the slug's
 * loot. Baby health isn't on the wiki; a third of the parent's is an estimate.
 */
public class BabyMagmaSlugMob extends SkyBlockMob {
    static final double HEALTH = LavaSeaCreature.MAGMA_SLUG.getHealth() / 3;
    private static final ThreadLocal<Litter> CONSTRUCTING = new ThreadLocal<>();
    private final Litter litter;

    private BabyMagmaSlugMob(Litter litter) {
        super(begin(litter));
        this.litter = CONSTRUCTING.get();
        CONSTRUCTING.remove();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(1));
    }

    public BabyMagmaSlugMob() {
        this(new Litter(new ArrayList<>(), null));
    }

    static BabyMagmaSlugMob create(Litter litter) {
        return new BabyMagmaSlugMob(litter);
    }

    private static EntityType begin(Litter litter) {
        CONSTRUCTING.set(litter);
        return EntityType.MAGMA_CUBE;
    }

    private Litter litter() {
        return litter != null ? litter : CONSTRUCTING.get();
    }

    private boolean lastAlive() {
        return litter().babies().stream().allMatch(baby -> baby == this || baby.isDead() || baby.isRemoved());
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        List<SkyBlockLootTable.LootRecord> records = lastAlive() ? litter().loot() : List.of();
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return records;
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    @Override
    public String getDisplayName() {
        return "Baby Magma Slug";
    }

    @Override
    public Integer getLevel() {
        return LavaSeaCreature.MAGMA_SLUG.getLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return SeaCreatureBehaviour.aggressive(1.2, 20, 16).goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return SeaCreatureBehaviour.aggressive(1.2, 20, 16).targets(this);
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, HEALTH)
                .withBase(ItemStatistic.DAMAGE, LavaSeaCreature.MAGMA_SLUG.getDamage())
                .withBase(ItemStatistic.SPEED, 60D)
                .build();
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
        return LavaSeaCreature.MAGMA_SLUG.getMobTypes();
    }

    @Override
    public boolean isImmuneToArrows() {
        return true;
    }

    record Litter(List<SkyBlockLootTable.LootRecord> loot, @Nullable SkyBlockPlayer angler, List<BabyMagmaSlugMob> babies) {
        Litter(List<SkyBlockLootTable.LootRecord> loot, @Nullable SkyBlockPlayer angler) {
            this(loot, angler, new ArrayList<>());
        }
    }
}
