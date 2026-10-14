package net.swofty.commons.reflection;

import net.swofty.commons.reflection.fixtures.ConcretePlugin;
import net.swofty.commons.reflection.fixtures.Plugin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PackageScannerTest {
    @Test
    void instantiatesOnlyConcreteSubtypesWithAnAccessibleNoArgConstructor() {
        List<Plugin> plugins = PackageScanner
                .instantiateSubtypes("net.swofty.commons.reflection.fixtures", Plugin.class)
                .toList();

        assertEquals(1, plugins.size());
        assertInstanceOf(ConcretePlugin.class, plugins.getFirst());
    }
}
