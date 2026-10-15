package dev.daweapon.type.skyblockgeneric.minion.actions;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import dev.daweapon.type.skyblockgeneric.minion.MinionHandler;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoMinionProduction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Alternates like every slaying minion: one action summons an Infernal, the next slays it and
 * harvests. The Infernal is a display entity; nothing about it can be fought or looted.
 */
public class MinionInfernoAction extends MinionAction {
    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        MinionHandler.InternalMinionTags tags = minion.getInternalMinionTags();
        Entity infernal = tags.getSpawnedMob();

        if (infernal == null || infernal.isRemoved()) {
            Pos spot = spawnSpot(minion, island);
            event.setToLook(spot);
            event.setAction(() -> tags.setSpawnedMob(summon(island, spot)));
            return List.of();
        }

        event.setToLook(infernal.getPosition());
        event.setAction(() -> {
            minion.getMinionEntity().swingAnimation();
            Pos at = infernal.getPosition();
            ParticlePacket flames = new ParticlePacket(Particle.FLAME, true, true, at.x(), at.y() + 1, at.z(),
                    0.3f, 0.5f, 0.3f, 0.02f, 15);
            island.getPlayers().forEach(viewer -> viewer.sendPacket(flames));
            infernal.remove();
            tags.setSpawnedMob(null);
        });
        return InfernoMinionProduction.harvest(minion, island);
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        int capacity = minion.getMinion().asSkyBlockMinion().getTiers().get(minion.getTier() - 1).storage();
        return minion.getItemsInMinion().stream().mapToInt(ItemQuantifiable::getAmount).sum() >= capacity;
    }

    private static Entity summon(Instance island, Pos spot) {
        LivingEntity infernal = new LivingEntity(EntityType.BLAZE);
        infernal.setNoGravity(true);
        infernal.setCustomName(Component.text("§cInfernal"));
        infernal.setCustomNameVisible(true);
        infernal.setInstance(island, spot.add(0.5, 0, 0.5));
        return infernal;
    }

    private static Pos spawnSpot(IslandMinionData.IslandMinion minion, Instance island) {
        List<Pos> spots = new ArrayList<>();
        for (Pos column : MathUtility.getRangeExcludingSelf(minion.getPosition(), 2 + minion.getBonusRange())) {
            for (int dy = -3; dy <= 2; dy++) {
                Pos pos = column.add(0, dy, 0);
                if (island.getBlock(pos).air() && island.getBlock(pos.add(0, 1, 0)).air()
                        && !island.getBlock(pos.sub(0, 1, 0)).air()) {
                    spots.add(pos);
                }
            }
        }
        return spots.isEmpty() ? minion.getPosition().add(1, 0, 0) : MathUtility.getRandomElement(spots);
    }
}
