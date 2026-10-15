package dev.daweapon.type.skyblockgeneric.slayer.inferno;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.crimson.CrimsonLocation;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Blaze Slayer flares: a deployable that hangs in the sky for 3 minutes and buffs up to 8 players
 * within 40 blocks. Its healing only works on the Crimson Isle. Only one deployable buff applies, so a
 * new flare's buff replaces the old one.
 */
public final class BlazeFlares {
    public static final String BUFF_PREFIX = "Flare: ";
    private static final int DURATION_SECONDS = 180;
    private static final double RANGE = 40;
    private static final int MAX_PLAYERS = 8;
    private static final double SKY_OFFSET = 12;

    public enum Flare {
        WARNING(ItemType.WARNING_FLARE, "<a>Warning Flare", 10, 10, 30, 0, 0),
        ALERT(ItemType.ALERT_FLARE, "<9>Alert Flare", 15, 20, 40, 10, 0),
        SOS(ItemType.SOS_FLARE, "<5>SOS Flare", 25, 30, 50, 10, 5);

        private final ItemType item;
        private final String displayName;
        private final double vitality;
        private final double trueDefense;
        private final double healPerSecond;
        private final double ferocity;
        private final double attackSpeed;

        Flare(ItemType item, String name, double vitality, double trueDefense, double healPerSecond,
              double ferocity, double attackSpeed) {
            this.item = item;
            this.displayName = name;
            this.vitality = vitality;
            this.trueDefense = trueDefense;
            this.healPerSecond = healPerSecond;
            this.ferocity = ferocity;
            this.attackSpeed = attackSpeed;
        }

        public static @Nullable Flare of(@Nullable ItemType type) {
            for (Flare flare : values()) {
                if (flare.item == type) return flare;
            }
            return null;
        }

        ItemStatistics statistics() {
            ItemStatistics.Builder builder = ItemStatistics.builder()
                .withBase(ItemStatistic.VITALITY, vitality)
                .withBase(ItemStatistic.TRUE_DEFENSE, trueDefense);
            if (ferocity > 0) builder.withBase(ItemStatistic.FEROCITY, ferocity);
            if (attackSpeed > 0) builder.withBase(ItemStatistic.BONUS_ATTACK_SPEED, attackSpeed);
            return builder.build();
        }

        public String displayName() {
            return displayName;
        }
    }

    private BlazeFlares() {
    }

    public static boolean deploy(SkyBlockPlayer player, Flare flare) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos sky = player.getPosition().add(0, SKY_OFFSET, 0);
        AtomicInteger remaining = new AtomicInteger(DURATION_SECONDS);
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (remaining.getAndDecrement() <= 0 || player.getInstance() != instance) {
                task[0].cancel();
                return;
            }
            ParticlePacket packet = new ParticlePacket(Particle.FIREWORK, true, true, sky.x(), sky.y(), sky.z(),
                0.3f, 0.3f, 0.3f, 0.02f, 10);
            instance.getPlayers().forEach(viewer -> viewer.sendPacket(packet));
            instance.getPlayers().stream()
                .filter(viewer -> viewer instanceof SkyBlockPlayer)
                .map(viewer -> (SkyBlockPlayer) viewer)
                .filter(viewer -> viewer.getPosition().distance(sky) <= RANGE)
                .sorted(Comparator.comparingDouble(viewer -> viewer.getPosition().distance(sky)))
                .limit(MAX_PLAYERS)
                .forEach(viewer -> buff(viewer, flare));
        }).repeat(TaskSchedule.seconds(1)).schedule();
        player.sendMessage("<a>You deployed a " + flare.displayName() + "<a>!");
        return true;
    }

    private static void buff(SkyBlockPlayer player, Flare flare) {
        player.getStatistics().removeTemporaryStatistics(stat -> stat.getDisplayName() != null
            && stat.getDisplayName().startsWith(BUFF_PREFIX));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
            .withStatistics(flare.statistics())
            .withExpirationInMs(1_500)
            .withDisplayName(BUFF_PREFIX + flare.displayName().replaceAll("<[^>]*>", ""))
            .build());
        if (CrimsonLocation.isOnCrimsonIsle(player)) {
            player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + flare.healPerSecond));
        }
    }
}
