package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class GreenhouseMinigames {
    public static final long HIDE_MILLIS = 60_000;
    public static final int[] HIDE_WARNINGS = {45, 30, 15, 5};
    public static final int PUNCHES_NEEDED = 10;
    public static final long LIGHT_GREEN_MILLIS = 2_000;
    public static final long LIGHT_YELLOW_MILLIS = 500;
    public static final long LIGHT_RED_MILLIS = 1_500;
    public static final long PUNCH_IDLE_MILLIS = 20_000;
    private static final long HEARTBEAT_MILLIS = 1_000;

    public enum Light {
        GREEN, YELLOW, RED
    }

    private static final class Hide {
        final int plot;
        final int phantom;
        final int hidden;
        final long expiresAt;
        int warnings;
        long lastBeat;

        Hide(int plot, int phantom, int hidden, long expiresAt) {
            this.plot = plot;
            this.phantom = phantom;
            this.hidden = hidden;
            this.expiresAt = expiresAt;
        }
    }

    private static final class Punch {
        final int plot;
        final int anchor;
        final long startedAt;
        long lastPunchAt;
        int progress;

        Punch(int plot, int anchor, long now) {
            this.plot = plot;
            this.anchor = anchor;
            this.startedAt = now;
            this.lastPunchAt = now;
        }
    }

    private static final Map<UUID, Hide> HIDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Punch> PUNCHING = new ConcurrentHashMap<>();

    private GreenhouseMinigames() {
    }

    public static Light light(long elapsedMillis) {
        long cycle = LIGHT_GREEN_MILLIS + LIGHT_YELLOW_MILLIS + LIGHT_RED_MILLIS;
        long at = Math.floorMod(elapsedMillis, cycle);
        if (at < LIGHT_GREEN_MILLIS) return Light.GREEN;
        return at < LIGHT_GREEN_MILLIS + LIGHT_YELLOW_MILLIS ? Light.YELLOW : Light.RED;
    }

    public static MutationBehaviour.Harvest startHide(MutationBehaviour.Ctx ctx) {
        List<Integer> candidates = new ArrayList<>();
        for (int anchor : ctx.bed().getPlants().keySet()) if (anchor != ctx.anchor()) candidates.add(anchor);
        SkyBlockPlayer player = ctx.player();
        if (candidates.isEmpty()) {
            player.sendMessage(Text.of("<e>[CROP] <9>Phantomleaf<f>: I have no where to hide!"));
            return MutationBehaviour.Harvest.BLOCKED;
        }
        int hidden = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        HIDING.put(player.getUuid(), new Hide(ctx.plot(), ctx.anchor(), hidden, System.currentTimeMillis() + HIDE_MILLIS));
        player.sendMessage(Text.of("<e>[CROP] <9>Phantomleaf<f>: Poof! Try and find me!"));
        return MutationBehaviour.Harvest.DEFERRED;
    }

    public static MutationBehaviour.Harvest startPunch(MutationBehaviour.Ctx ctx) {
        PUNCHING.put(ctx.player().getUuid(), new Punch(ctx.plot(), ctx.anchor(), System.currentTimeMillis()));
        ctx.player().sendMessage(Text.of("<e>[CROP] <9>Stoplight Petal<f>: Punch me while I am <a>green<f>!"));
        return MutationBehaviour.Harvest.DEFERRED;
    }

    public static boolean active(SkyBlockPlayer player) {
        return HIDING.containsKey(player.getUuid()) || PUNCHING.containsKey(player.getUuid());
    }

    public static boolean intercept(SkyBlockPlayer player, GreenhouseLayout.Spot spot, boolean punch) {
        Hide hide = HIDING.get(player.getUuid());
        if (hide != null && hide.plot == spot.plot()) return guess(player, hide, spot);
        Punch current = PUNCHING.get(player.getUuid());
        if (current != null && current.plot == spot.plot() && current.anchor == spot.slot()) {
            if (punch) punch(player, current, spot);
            return true;
        }
        return false;
    }

    private static boolean guess(SkyBlockPlayer player, Hide hide, GreenhouseLayout.Spot spot) {
        if (spot.slot() == hide.phantom) return true;
        HIDING.remove(player.getUuid());
        if (spot.slot() == hide.hidden) {
            player.sendMessage(Text.of("<e>[CROP] <9>Phantomleaf<f>: You found me!"));
            GreenhouseService.finishHarvest(player, new GreenhouseLayout.Spot(hide.plot, hide.phantom));
        } else {
            player.sendMessage(Text.of("<e>[CROP] <9>Phantomleaf<f>: That's not me! Better luck next time!"));
            destroy(player, hide);
        }
        return true;
    }

    private static void destroy(SkyBlockPlayer player, Hide hide) {
        var profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(hide.plot);
        if (bed == null || bed.getPlants().remove(hide.phantom) == null) return;
        Gardens.save(player, profile);
        GreenhouseService.render(player, hide.plot);
    }

    private static void punch(SkyBlockPlayer player, Punch current, GreenhouseLayout.Spot spot) {
        long now = System.currentTimeMillis();
        current.lastPunchAt = now;
        switch (light(now - current.startedAt)) {
            case RED -> {
                current.progress = 0;
                player.sendMessage(Text.of("<c>You punched it while it was red! The progress bar was reset."));
            }
            default -> current.progress++;
        }
        if (current.progress >= PUNCHES_NEEDED) {
            PUNCHING.remove(player.getUuid());
            GreenhouseService.finishHarvest(player, spot);
            return;
        }
        player.sendActionBar(Text.of("{} <8>{}<7>/<a>{}", lightColour(now - current.startedAt), current.progress, PUNCHES_NEEDED).asComponent());
    }

    private static String lightColour(long elapsed) {
        return switch (light(elapsed)) {
            case GREEN -> "<a><l>GREEN";
            case YELLOW -> "<e><l>YELLOW";
            case RED -> "<c><l>RED";
        };
    }

    public static void tick(Instance instance) {
        if (HIDING.isEmpty() && PUNCHING.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (Player online : instance.getPlayers()) {
            if (!(online instanceof SkyBlockPlayer player)) continue;
            Hide hide = HIDING.get(player.getUuid());
            if (hide != null) tickHide(player, hide, now);
            Punch punch = PUNCHING.get(player.getUuid());
            if (punch != null) tickPunch(player, punch, now);
        }
    }

    private static void tickHide(SkyBlockPlayer player, Hide hide, long now) {
        if (now >= hide.expiresAt) {
            HIDING.remove(player.getUuid());
            player.sendMessage(Text.of("<e>[CROP] <9>Phantomleaf<f>: Time's up! You didn't find me fast enough!"));
            destroy(player, hide);
            return;
        }
        long left = (hide.expiresAt - now) / 1000;
        if (hide.warnings < HIDE_WARNINGS.length && left <= HIDE_WARNINGS[hide.warnings]) {
            player.sendMessage(Text.of("<e>[CROP] <9>Phantomleaf<f>: You have {} seconds left!", HIDE_WARNINGS[hide.warnings]));
            hide.warnings++;
        }
        if (now - hide.lastBeat < HEARTBEAT_MILLIS) return;
        hide.lastBeat = now;
        Pos at = MutationBehaviours.centre(hide.plot, hide.hidden, 1).add(0, 1.2, 0);
        player.sendPacket(new ParticlePacket(Particle.HEART, false, false, (float) at.x(), (float) at.y(), (float) at.z(), 0.15f, 0.15f, 0.15f, 0, 2));
        player.playSound(Sound.sound(Key.key("block.note_block.basedrum"), Sound.Source.PLAYER, 0.6f, 0.6f), at.x(), at.y(), at.z());
    }

    private static void tickPunch(SkyBlockPlayer player, Punch punch, long now) {
        if (now - punch.lastPunchAt > PUNCH_IDLE_MILLIS) {
            PUNCHING.remove(player.getUuid());
            player.sendMessage(Text.of("<e>[CROP] <9>Stoplight Petal<f>: Too slow!"));
            return;
        }
        player.sendActionBar(Text.of("{} <8>{}<7>/<a>{}", lightColour(now - punch.startedAt), punch.progress, PUNCHES_NEEDED).asComponent());
    }

    public static void cancel(SkyBlockPlayer player) {
        HIDING.remove(player.getUuid());
        PUNCHING.remove(player.getUuid());
    }
}
