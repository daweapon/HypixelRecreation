package net.swofty.type.skyblockgeneric.garden.pest;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.npc.GardenGround;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class PestEntity extends EntityCreature {
    private static final double FLEE_RANGE = 7;
    private static final double FLEE_SPEED = 4.5;
    private static final double WANDER_SPEED = 1.6;
    private static final int PLOT_MARGIN = 3;
    private static final double FLIGHT_HEIGHT = 1.4;

    private final PestCatalogue.Pest pest;
    private final String aliveId;
    private final int plot;
    private final boolean flies;
    private double health;
    private Vec wander = Vec.ZERO;
    private long nextWander;

    public PestEntity(PestCatalogue.Pest pest, String aliveId, int plot) {
        super(EntityType.ARMOR_STAND);
        this.pest = pest;
        this.aliveId = aliveId;
        this.plot = plot;
        this.flies = pest.entity() == EntityType.BEE;
        this.health = PestCatalogue.get().health();
        setNoGravity(flies);
        setInvisible(true);
        ((ArmorStandMeta) getEntityMeta()).setSmall(true);
        ((ArmorStandMeta) getEntityMeta()).setHasNoBasePlate(true);
        setHelmet(ItemStacks.head(pest.texture(), "").build());
        setCustomNameVisible(true);
        refreshName();
    }

    public PestCatalogue.Pest pest() {
        return pest;
    }

    public String aliveId() {
        return aliveId;
    }

    public int plot() {
        return plot;
    }

    public double pestHealth() {
        return health;
    }

    public boolean hurt(double amount) {
        health -= amount;
        if (health > 0) refreshName();
        return health <= 0;
    }

    public void pullToward(Pos target, double speed) {
        Vec direction = target.sub(getPosition()).asVec();
        if (direction.length() < 0.5) return;
        setVelocity(direction.normalize().mul(speed));
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        return false;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (getInstance() == null || getAliveTicks() % 3 != 0) return;
        steer();
        if (ThreadLocalRandom.current().nextInt(120) == 0) ambient();
    }

    private void ambient() {
        EntityType type = pest.entity();
        SoundEvent sound = type == EntityType.BEE ? SoundEvent.ENTITY_BEE_LOOP
                : type == EntityType.SILVERFISH ? SoundEvent.ENTITY_SILVERFISH_AMBIENT
                : type == EntityType.ENDERMITE ? SoundEvent.ENTITY_ENDERMITE_AMBIENT
                : type == EntityType.RABBIT ? SoundEvent.ENTITY_RABBIT_AMBIENT
                : SoundEvent.ENTITY_SLIME_SQUISH_SMALL;
        getInstance().playSound(Sound.sound(sound, Sound.Source.HOSTILE, 0.5f, 1.2f), getPosition().x(), getPosition().y(), getPosition().z());
    }

    private void refreshName() {
        set(DataComponents.CUSTOM_NAME, Text.parse("<a>" + pest.name() + " <c>" + (int) Math.ceil(health) + "❤").asComponent());
    }

    private void steer() {
        Instance instance = getInstance();
        Pos position = getPosition();
        if (GardenLayout.plotAt(position.x(), position.z()) != plot) {
            teleport(home(instance));
            return;
        }
        Vec direction = Vec.ZERO;
        double speed = WANDER_SPEED;
        Player threat = nearestPlayer(instance, position);
        if (threat != null && pest.flees()) {
            direction = position.sub(threat.getPosition()).asVec().withY(0);
            speed = FLEE_SPEED;
        } else if (threat == null || !pest.flees()) {
            long now = System.currentTimeMillis();
            if (now >= nextWander) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                double angle = random.nextDouble() * Math.PI * 2;
                wander = random.nextInt(10) < 4 ? Vec.ZERO : new Vec(Math.cos(angle), 0, Math.sin(angle));
                nextWander = now + 1500 + random.nextInt(3500);
            }
            direction = wander;
        }
        if (direction.length() > 0.01) {
            direction = direction.normalize();
            Pos ahead = position.add(direction.mul(2));
            if (!insidePlot(ahead)) direction = direction.neg();
            setView((float) Math.toDegrees(Math.atan2(-direction.x(), direction.z())), 0);
        }
        double vertical = 0;
        if (flies) {
            double ground = GardenGround.onGround(instance, position).y();
            vertical = Math.max(-2, Math.min(2, (ground + FLIGHT_HEIGHT - position.y()) * 2));
        }
        Vec velocity = direction.mul(speed);
        setVelocity(flies ? velocity.withY(vertical) : velocity.withY(getVelocity().y()));
    }

    private boolean insidePlot(Pos position) {
        int[] origin = GardenLayout.plotOrigin(plot);
        if (origin == null) return false;
        return position.x() > origin[0] + PLOT_MARGIN && position.x() < origin[0] + GardenLayout.PLOT_SIZE - PLOT_MARGIN
                && position.z() > origin[1] + PLOT_MARGIN && position.z() < origin[1] + GardenLayout.PLOT_SIZE - PLOT_MARGIN;
    }

    private Pos home(Instance instance) {
        int[] origin = GardenLayout.plotOrigin(plot);
        double x = origin[0] + GardenLayout.PLOT_SIZE / 2.0;
        double z = origin[1] + GardenLayout.PLOT_SIZE / 2.0;
        return GardenGround.onGround(instance, new Pos(x, 72, z));
    }

    private static Player nearestPlayer(Instance instance, Pos position) {
        Player nearest = null;
        double best = FLEE_RANGE * FLEE_RANGE;
        for (Player player : instance.getPlayers()) {
            double distance = player.getPosition().distanceSquared(position);
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }
}
