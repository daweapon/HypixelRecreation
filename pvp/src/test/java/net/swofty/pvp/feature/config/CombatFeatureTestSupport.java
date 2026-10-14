package net.swofty.pvp.feature.config;

import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.attack.VanillaCriticalFeature;

final class CombatFeatureTestSupport {
    private CombatFeatureTestSupport() {
    }

    static DefinedFeature<VanillaCriticalFeature> noDependencyCritical() {
        return new DefinedFeature<>(FeatureType.CRITICAL, VanillaCriticalFeature::new);
    }
}
