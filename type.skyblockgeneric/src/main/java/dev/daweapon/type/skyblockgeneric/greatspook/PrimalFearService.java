package dev.daweapon.type.skyblockgeneric.greatspook;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.greatspook.mob.PrimalFearMob;
import dev.daweapon.type.skyblockgeneric.greatspook.mob.PrimalMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class PrimalFearService {
    private static final int PARTICLES = 40;

    private PrimalFearService() {
    }

    public static boolean onKill(SkyBlockPlayer player, SkyBlockMob killed) {
        if (!GreatSpookEvent.isActive() || killed instanceof PrimalMob || killed instanceof MinionSpawnedMob) return false;
        Instance instance = killed.getInstance();
        if (instance == null || player.getSkyblockDataHandler() == null) return false;
        if (PrimalFearProgress.remainingMillis(player) > 0) return false;
        double fear = GreatSpookFear.fear(player);
        if (!LootRoll.roll(player.getUuid(), null, PrimalFearRules.chancePercent(fear))) return false;
        spawn(player, instance, killed.getPosition());
        return true;
    }

    public static PrimalFearMob spawn(SkyBlockPlayer player, Instance instance, Pos position) {
        PrimalFearProgress.markSpawn(player, System.currentTimeMillis());
        PrimalFearMob mob = new PrimalFearMob(player.getUuid(), player.getUsername());
        mob.setInstance(instance, position);
        player.sendMessage(Text.parseLenient("<5><l>[FEAR] <r><d>A Primal Fear <e>has been summoned!"));
        player.playSound(Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.HOSTILE, 0.6f, 1.6f), Sound.Emitter.self());
        for (Particle particle : new Particle[]{Particle.WITCH, Particle.SMOKE}) {
            instance.sendGroupedPacket(new ParticlePacket(particle, false, false,
                    position.x(), position.y() + 1, position.z(), 0.5f, 0.8f, 0.5f, 0.05f, PARTICLES));
        }
        return mob;
    }
}
