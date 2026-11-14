package dev.daweapon.pvp.feature.config;

import dev.daweapon.pvp.feature.FeatureType;
import dev.daweapon.pvp.feature.attack.VanillaCriticalFeature;

final class CombatFeatureTestSupport {
    private CombatFeatureTestSupport() {
    }

    static DefinedFeature<VanillaCriticalFeature> noDependencyCritical() {
        return new DefinedFeature<>(FeatureType.CRITICAL, VanillaCriticalFeature::new);
    }
}
