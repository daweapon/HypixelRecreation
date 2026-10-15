package dev.daweapon.type.skyblockgeneric.entity;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PetEntityImpl extends LivingEntity {
    private final String url;
    private final SkyBlockPlayer player;
    private final Particle particle;
    private Task upAndDownTask;
    private Task moveTowardsPlayer;
    @Getter
    private float yLevel = 0f;
    @Getter
    private boolean goingDown = false;

    public PetEntityImpl(@NotNull SkyBlockPlayer player, @NotNull String url, @NotNull SkyBlockItem pet) {
        super(EntityType.ARMOR_STAND);

        this.collidesWithEntities = false;
        this.hasPhysics = false;

        this.player = player;
        this.url = url;
        this.particle = pet.getComponent(PetComponent.class).getParticleId();

        var attributeHandler = pet.getAttributeHandler();
        var rarity = attributeHandler.getRarity();
        var level = PetLevels.level(pet);
        var petName = pet.getComponent(PetComponent.class).getPetName();

        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.set(MetadataDef.CUSTOM_NAME, Text.of("<8>[<7>Lvl{}<8>] <color:{}>{}'s {}",
                    level, rarity.getColor(), player.getUsername(), petName
            ).asComponent());
        });
    }

    public SkyBlockPlayer getOwner() {
        return player;
    }

    public static void refreshFor(SkyBlockPlayer viewer) {
        if (viewer.getInstance() == null) return;
        viewer.getInstance().getEntities().forEach(entity -> {
            if (!(entity instanceof PetEntityImpl pet)) return;
            pet.updateViewableRule();
            if (pet.isViewer(viewer)) pet.sendNameTagState(viewer);
        });
    }

    private boolean canBeSeenBy(Player viewer) {
        if (!(viewer instanceof SkyBlockPlayer skyBlockViewer)) return true;
        return switch (skyBlockViewer.getPetData().getVisibility()) {
            case ALL -> true;
            case NONE -> false;
            case OWN -> viewer == player;
        };
    }

    private void sendNameTagState(Player viewer) {
        boolean visible = !(viewer instanceof SkyBlockPlayer skyBlockViewer) || skyBlockViewer.getPetData().isNameTags();
        viewer.sendPacket(new EntityMetaDataPacket(getEntityId(),
                Map.of(MetadataDef.CUSTOM_NAME_VISIBLE.index(), Metadata.Boolean(visible))));
    }

    @Override
    public void updateNewViewer(@NotNull Player viewer) {
        super.updateNewViewer(viewer);
        sendNameTagState(viewer);
    }

    @Override
    public void spawn() {
        super.spawn();
        updateViewableRule(this::canBeSeenBy);

        ArmorStandMeta meta = (ArmorStandMeta) getEntityMeta();
        meta.setSmall(true);
        meta.setInvisible(true);
        meta.setHasNoBasePlate(true);
        meta.setHasNoGravity(true);
        meta.setHasNoGravity(true);
        getEntityMeta().setCustomNameVisible(true);

        setHelmet(ItemStacks.head(url, "").build());

        upAndDownTask = MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (isDead() || !player.isOnline()) {
                upAndDownTask.cancel();
                if (!isDead()) {
                    this.kill();
                }
                return;
            }
            yLevel = goingDown ? yLevel - 0.02f : yLevel + 0.02f;
            if (yLevel >= 0.12f) {
                goingDown = true;
            } else if (yLevel <= -0.12f) {
                goingDown = false;
            }
            teleport(getPosition().add(0, yLevel, 0));
            getViewers().forEach(viewer -> {
                viewer.sendPacket(new ParticlePacket(
                        particle,
                        false,
                        false,
                        getPosition().x(),
                        getPosition().y(),
                        getPosition().z(),
                        0.1f,
                        0.1f,
                        0.1f,
                        0f,
                        3
                ));
            });
        }, TaskSchedule.tick(5), TaskSchedule.tick(3));
        moveTowardsPlayer = MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (isDead() || !player.isOnline()) {
                moveTowardsPlayer.cancel();
                if (!isDead()) {
                    this.kill();
                }
                return;
            }
            Pos location = getPosition().withYaw(MathUtility.getYawNeededToLookAt(getPosition(), player.getPosition()));
            double distance = getPosition().distance(player.getPosition());

            if (distance > 10) {
                teleport(player.getPosition().add(0, 1.5, 0));
                return;
            }
            if (distance > 3) {
                teleport(location.add(player.getPosition().add(0, 1.5, 0).sub(getPosition()).asVec().normalize().mul(0.7)));
            }
        }, TaskSchedule.tick(5), TaskSchedule.tick(3));
    }
}
