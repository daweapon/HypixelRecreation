package net.swofty.type.kuudra.mob;

import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraMobSpec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/** Builds a Kuudra minion from its {@code mobs.yml} id. */
public final class KuudraMobs {
    /** The ids a wave can spawn; Magma Bacteria only live in Kuudra's stomach (Burning and up). */
    public static final Set<String> WAVE_MOBS = Set.of("KUUDRA_FOLLOWER", "KUUDRA_LANDMINE", "INFERNO_MAGMA_CUBE",
            "KUUDRA_BERSERKER", "BLIGHT", "KUUDRA_KNOCKER", "WANDERING_BLAZE", "DROPSHIP", "KUUDRA_SLASHER",
            "WITHER_SENTRY", "MAGMA_FOLLOWER", "BLAZING_GOLEM", "CHAOSMITE");

    private KuudraMobs() {
    }

    public static @Nullable KuudraMob create(KuudraFight fight, KuudraMobSpec spec, List<KuudraSpecialists.MagmaFollower> group) {
        return switch (spec.id()) {
            case "KUUDRA_FOLLOWER" -> new KuudraRushers.Follower(fight, spec);
            case "KUUDRA_LANDMINE" -> new KuudraRushers.Landmine(fight, spec);
            case "INFERNO_MAGMA_CUBE" -> new KuudraRushers.InfernoMagmaCube(fight, spec);
            case "KUUDRA_BERSERKER" -> new KuudraRushers.Berserker(fight, spec);
            case "EXPLOSIVE_IMP" -> new KuudraRushers.ExplosiveImp(fight, spec);
            case "BLIGHT" -> new KuudraRushers.Blight(fight, spec);
            case "KUUDRA_KNOCKER" -> new KuudraRushers.Knocker(fight, spec);
            case "CHAOSMITE" -> new KuudraRushers.Chaosmite(fight, spec);
            case "WANDERING_BLAZE" -> new KuudraSpecialists.WanderingBlaze(fight, spec);
            case "DROPSHIP" -> new KuudraSpecialists.Dropship(fight, spec);
            case "KUUDRA_SLASHER" -> new KuudraSpecialists.Slasher(fight, spec);
            case "WITHER_SENTRY" -> new KuudraSpecialists.WitherSentry(fight, spec);
            case "BLAZING_GOLEM" -> new KuudraSpecialists.BlazingGolem(fight, spec);
            case "MAGMA_FOLLOWER" -> new KuudraSpecialists.MagmaFollower(fight, spec, group);
            case "OMEGAGMA" -> new KuudraSpecialists.Omegagma(fight, spec);
            case MagmaBacteria.ID -> new MagmaBacteria(fight, spec);
            default -> null;
        };
    }
}
