package net.swofty.type.skyblockgeneric.crystalhollows;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class CrystalPlacementEffects {
    private static final int BURST_PARTICLES = 3;

    private CrystalPlacementEffects() {
    }

    static void play(SkyBlockPlayer player, NucleusCrystal crystal) {
        Vec at = NucleusConfig.get().pedestal(crystal);
        if (at == null) {
            player.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.BLOCK, 1f, 1f));
            return;
        }
        player.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.BLOCK, 1.5f, 1f), at.x(), at.y(), at.z());
        player.sendPacket(new ParticlePacket(Particle.EXPLOSION_EMITTER, false, true,
                (float) at.x(), (float) at.y(), (float) at.z(), 0.5f, 0.5f, 0.5f, 0f, BURST_PARTICLES));
        player.sendPacket(new ParticlePacket(Particle.END_ROD, false, true,
                (float) at.x(), (float) at.y(), (float) at.z(), 1f, 1f, 1f, 0.2f, 40));
    }
}
