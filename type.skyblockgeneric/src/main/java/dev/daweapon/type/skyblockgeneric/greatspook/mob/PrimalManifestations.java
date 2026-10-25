package dev.daweapon.type.skyblockgeneric.greatspook.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.greatspook.Manifestation;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PrimalManifestations {
    private static final double GROUP_RADIUS = 2.5;
    private static final int GROUP_SIZE = 5;

    private PrimalManifestations() {
    }

    public static void spawn(Manifestation kind, Instance instance, Pos position, SkyBlockPlayer owner) {
        UUID id = owner.getUuid();
        String name = owner.getUsername();
        switch (kind) {
            case DEADLINE -> new DeadlineMob(id, name).setInstance(instance, position);
            case DARKNESS_SHADE -> new DarknessShadeMob(id, name).setInstance(instance, position);
            case COMMITMENT_PHOBIA -> new CommitmentPhobiaMob(id, name).setInstance(instance, position);
            case MATH_TEACHER -> new MathTeacherMob(id, name).setInstance(instance, position);
            case VEGAN_CRAWLER -> new VeganCrawlerMob(id, name).setInstance(instance, position);
            case PUBLIC_SPEAKING_DEMON -> {
                PublicSpeakingGroup group = new PublicSpeakingGroup();
                for (int i = 0; i < GROUP_SIZE; i++) {
                    double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                    double radius = ThreadLocalRandom.current().nextDouble(GROUP_RADIUS);
                    PublicSpeakingDemonMob demon = new PublicSpeakingDemonMob(id, name, group);
                    group.add(demon);
                    demon.setInstance(instance, position.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
                }
            }
        }
    }
}
