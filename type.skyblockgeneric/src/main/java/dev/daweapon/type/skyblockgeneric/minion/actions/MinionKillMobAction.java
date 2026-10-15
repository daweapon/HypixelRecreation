package dev.daweapon.type.skyblockgeneric.minion.actions;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.drops.MinionDropTable;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Alternates like every slaying minion on Hypixel: one action spawns the mob, the next kills it and
 * rolls the minion's drop table.
 */
public class MinionKillMobAction extends MinionAction {
    private final Class<? extends SkyBlockMob> mobClass;
    private final Supplier<? extends SkyBlockMob> mobFactory;
    private final MinionDropTable drops;

    public <T extends SkyBlockMob> MinionKillMobAction(Class<T> mobClass, Supplier<T> mobFactory, MinionDropTable drops) {
        this.mobClass = mobClass;
        this.mobFactory = mobFactory;
        this.drops = drops;
    }

    public MinionDropTable getDrops() {
        return drops;
    }

    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        List<Pos> spawnArea = spawnArea(minion);

        for (Pos pos : spawnArea) {
            for (SkyBlockMob mob : SkyBlockMob.getMobFromFuzzyPosition(pos)) {
                if (mob.getClass() != mobClass || mob.isDead() || mob.getInstance() != island) continue;

                event.setToLook(mob.getPosition());
                event.setAction(() -> {
                    if (mob.isDead()) return;
                    minion.getMinionEntity().swingAnimation();

                    DamageIndicator indicator = new DamageIndicator();
                    indicator.pos(mob.getPosition());
                    indicator.damage(mob.getHealth());
                    indicator.critical(false);
                    indicator.display(island);

                    mob.damage(new Damage(DamageType.GENERIC_KILL, null, minion.getMinionEntity(), null, 0));
                    mob.kill();
                });
                return drops.rollItems();
            }
        }

        Collections.shuffle(spawnArea);
        Pos spawnAt = spawnArea.stream()
                .filter(pos -> island.getBlock(pos).air() && island.getBlock(pos.add(0, 1, 0)).air())
                .findFirst()
                .orElse(null);

        if (spawnAt == null) {
            event.setToLook(minion.getPosition());
            event.setAction(() -> {});
            return List.of();
        }

        event.setToLook(spawnAt);
        event.setAction(() -> mobFactory.get().setInstance(island, spawnAt.add(0.5, 0, 0.5)));
        return List.of();
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        return isStorageFull(minion);
    }

    private static List<Pos> spawnArea(IslandMinionData.IslandMinion minion) {
        List<Pos> area = new ArrayList<>();
        for (Pos column : MathUtility.getRangeExcludingSelf(minion.getPosition(), 2 + minion.getBonusRange())) {
            for (int dy = -3; dy <= 2; dy++) {
                area.add(column.add(0, dy, 0));
            }
        }
        return area;
    }
}
