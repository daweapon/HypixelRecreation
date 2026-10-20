package dev.daweapon.type.theend;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.events.ActionEndAccessGate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SepulturePads {
    public static final Pos ENTRANCE = new Pos(-563, 6, -315);
    public static final Pos INSIDE = new Pos(-568, 6, -320);
    public static final Pos EXIT = new Pos(-560.5, 7, -312.5);
    private static final double PAD_TOP = 0.8125;
    private static final int BUILD_DELAY_SECONDS = 5;

    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    private SepulturePads() {
    }

    public static void install() {
        MinecraftServer.getSchedulerManager().buildTask(() -> buildHolograms(HypixelConst.getInstanceContainer()))
                .delay(TaskSchedule.seconds(BUILD_DELAY_SECONDS)).schedule();
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, event -> {
            if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
            Pos at = event.getNewPosition();
            if (PadRules.isOnPad(at.x(), at.y(), at.z(), ENTRANCE.blockX(), ENTRANCE.blockY(), ENTRANCE.blockZ())) {
                use(player, true);
            } else if (PadRules.isOnPad(at.x(), at.y(), at.z(), INSIDE.blockX(), INSIDE.blockY(), INSIDE.blockZ())) {
                use(player, false);
            }
        });
    }

    public static void markUsed(SkyBlockPlayer player) {
        LAST_USE.put(player.getUuid(), System.currentTimeMillis());
    }

    private static void use(SkyBlockPlayer player, boolean entering) {
        long now = System.currentTimeMillis();
        if (PadRules.coolingDown(LAST_USE.getOrDefault(player.getUuid(), 0L), now)) return;
        markUsed(player);
        if (entering && !player.getRank().isStaff()
                && !ActionEndAccessGate.meetsSepultureLevel(player.getSkills().getCurrentLevel(SkillCategories.COMBAT))) {
            player.sendMessage("<c>You must have Combat " + ActionEndAccessGate.SEPULTURE_COMBAT_LEVEL
                    + " unlocked to enter the Void Sepulture!");
            return;
        }
        Pos target = entering ? INSIDE : ENTRANCE;
        Pos from = player.getPosition();
        Pos landing = new Pos(target.blockX() + 0.5, target.blockY() + PAD_TOP, target.blockZ() + 0.5, from.yaw(), from.pitch());
        effects(player.getInstance(), from);
        player.teleport(landing);
        effects(player.getInstance(), landing);
    }

    private static void effects(Instance instance, Pos at) {
        if (instance == null) return;
        instance.sendGroupedPacket(new ParticlePacket(Particle.PORTAL, true, true,
                at.x(), at.y() + 1, at.z(), 0.4f, 0.8f, 0.4f, 0.6f, 60));
        instance.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.PLAYER, 1f, 1f), at.x(), at.y(), at.z());
    }

    private static void buildHolograms(Instance instance) {
        if (instance == null) return;
        hologram(instance, ENTRANCE, "<c><l>DANGEROUS!", 2.95);
        hologram(instance, ENTRANCE, "<b>Travel to:", 2.55);
        hologram(instance, ENTRANCE, "<a>Void Sepulture", 2.25);
        hologram(instance, INSIDE, "<b>Travel to:", 2.55);
        hologram(instance, INSIDE, "<a>The End", 2.25);
    }

    private static void hologram(Instance instance, Pos pad, String line, double height) {
        Pos at = new Pos(pad.blockX() + 0.5, pad.blockY() + height, pad.blockZ() + 0.5);
        if (!instance.isChunkLoaded(at)) instance.loadChunk(at).join();
        new TextDisplayEntity(Text.parse(line), meta -> {
        }).setInstance(instance, at);
    }
}
