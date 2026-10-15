package dev.daweapon.type.skyblockgeneric.entity.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stands in for a Bestiary family whose mob isn't in this server yet, so the family is still
 * catalogued and can be maxed. It is never spawned. Its id is the family's own, so kills move
 * over unchanged once a real mob with that id is added.
 */
public final class CataloguedBestiaryMob extends BestiaryMob {

    public record Family(String mobId, String displayName, int bracket, int maxTier, GUIMaterial guiMaterial, MobType mobType) {
    }

    private static final ThreadLocal<Family> IN_CONSTRUCTION = new ThreadLocal<>();

    private final Family family;

    private CataloguedBestiaryMob(Family family) {
        super(EntityType.ARMOR_STAND);
        this.family = family;
    }

    public static CataloguedBestiaryMob of(Family family) {
        IN_CONSTRUCTION.set(family);
        try {
            return new CataloguedBestiaryMob(family);
        } finally {
            IN_CONSTRUCTION.remove();
        }
    }

    @Override public String getDisplayName() { return live().displayName(); }
    @Override public Integer getLevel() { return 1; }
    @Override public List<GoalSelector> getGoalSelectors() { return List.of(); }
    @Override public List<TargetSelector> getTargetSelectors() { return List.of(); }
    @Override public ItemStatistics getBaseStatistics() { return ItemStatistics.empty(); }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return null; }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }
    @Override public long damageCooldown() { return 0; }
    @Override public OtherLoot getOtherLoot() { return new OtherLoot(0, 0, 0); }
    @Override public List<MobType> getMobTypes() { return List.of(live().mobType()); }
    @Override public int getMaxBestiaryTier() { return live().maxTier(); }
    @Override public int getBestiaryBracket() { return live().bracket(); }
    @Override public String getMobID() { return live().mobId(); }
    @Override public GUIMaterial getGuiMaterial() { return live().guiMaterial(); }

    private Family live() {
        return family != null ? family : IN_CONSTRUCTION.get();
    }
}
