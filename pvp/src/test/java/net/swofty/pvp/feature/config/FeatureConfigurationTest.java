package net.swofty.pvp.feature.config;

import net.swofty.pvp.feature.CombatFeature;
import net.swofty.pvp.feature.CombatFeatureSet;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.attack.CriticalFeature;
import net.swofty.pvp.feature.attack.VanillaCriticalFeature;
import net.swofty.pvp.utils.CombatVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureConfigurationTest {
    @Test
    void missingFeatureFallsBackToTypeDefault() {
        FeatureConfiguration configuration = new FeatureConfiguration();
        assertSame(CriticalFeature.NO_OP, configuration.get(FeatureType.CRITICAL));
        assertNull(configuration.getDirect(FeatureType.CRITICAL));
    }

    @Test
    void addedFeatureOverridesDefaultAndReplacesPrevious() {
        FeatureConfiguration configuration = FeatureConfiguration.of(FeatureType.VERSION, CombatVersion.LEGACY);
        assertSame(CombatVersion.LEGACY, configuration.get(FeatureType.VERSION));
        configuration.add(FeatureType.VERSION, CombatVersion.MODERN);
        assertSame(CombatVersion.MODERN, configuration.get(FeatureType.VERSION));
        assertEquals(1, configuration.size());
    }

    @Test
    void overlayPrefersLocalFeaturesThenFallsBackToBacking() {
        FeatureConfiguration backing = FeatureConfiguration.of(FeatureType.VERSION, CombatVersion.LEGACY);
        FeatureConfiguration overlay = backing.overlay();
        assertSame(CombatVersion.LEGACY, overlay.get(FeatureType.VERSION));
        overlay.add(FeatureType.VERSION, CombatVersion.MODERN);
        assertSame(CombatVersion.MODERN, overlay.get(FeatureType.VERSION));
        assertSame(CombatVersion.LEGACY, backing.get(FeatureType.VERSION));
    }

    @Test
    void overlayFallsBackToDefaultWhenNeitherHasFeature() {
        FeatureConfiguration overlay = new FeatureConfiguration().overlay();
        assertSame(CriticalFeature.NO_OP, overlay.get(FeatureType.CRITICAL));
    }

    @Test
    void combatConfigurationBuildsSetWithConfiguredVersion() {
        CombatFeatureSet set = new CombatConfiguration().version(CombatVersion.LEGACY).build();
        assertSame(CombatVersion.LEGACY, set.get(FeatureType.VERSION));
        assertSame(CriticalFeature.NO_OP, set.get(FeatureType.CRITICAL));
    }

    @Test
    void combatConfigurationConstructsDefinedFeaturesAndInitialisesDependencies() {
        CombatFeatureSet set = new CombatConfiguration()
                .version(CombatVersion.LEGACY)
                .add(CombatFeatureTestSupport.noDependencyCritical())
                .build();
        CombatFeature critical = set.get(FeatureType.CRITICAL);
        assertInstanceOf(VanillaCriticalFeature.class, critical);
    }

    @Test
    void featureSetRejectsAdditionsAfterBuild() {
        CombatFeatureSet set = new CombatConfiguration().build();
        assertThrows(UnsupportedOperationException.class,
                () -> set.add(FeatureType.VERSION, CombatVersion.LEGACY));
    }

    @Test
    void overridingNonDependencyIsRejected() {
        DefinedFeature<VanillaCriticalFeature> noDeps = CombatFeatureTestSupport.noDependencyCritical();
        assertThrows(RuntimeException.class, () -> CombatConfiguration.wrap(noDeps,
                FeatureConfiguration.of(FeatureType.VERSION, CombatVersion.LEGACY)));
    }

    @Test
    void removeDropsFeatureFromBuild() {
        CombatFeatureSet set = new CombatConfiguration()
                .add(CombatFeatureTestSupport.noDependencyCritical())
                .remove(FeatureType.CRITICAL)
                .build();
        assertSame(CriticalFeature.NO_OP, set.get(FeatureType.CRITICAL));
    }
}
