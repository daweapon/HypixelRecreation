package dev.daweapon.type.kuudra.arena;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.SharedInstance;
import dev.daweapon.type.generic.world.HypixelWorldLoader;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** One run's own copy of Kuudra's Hollow: the captured map when it is installed, otherwise the placeholder. */
public record KuudraHollow(InstanceContainer container, SharedInstance instance, boolean placeholder) {
    public static final Path WORLD_DIRECTORY = Path.of("./configuration/world");
    private static final Key FULLBRIGHT_DIMENSION = Key.key("skyblock:island");

    public static KuudraHollow create(KuudraArena arena) {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        Path world = WORLD_DIRECTORY.resolve(arena.world());
        if (Files.isRegularFile(world)) {
            InstanceContainer container = manager.createInstanceContainer();
            try {
                return new KuudraHollow(container, HypixelWorldLoader.loadFrom(world, container, manager), false);
            } catch (IOException exception) {
                manager.unregisterInstance(container);
                Logger.error(exception, "Couldn't load {}; using the placeholder Kuudra's Hollow", world);
            }
        }
        InstanceContainer container = manager.createInstanceContainer(
                MinecraftServer.getDimensionTypeRegistry().getKey(FULLBRIGHT_DIMENSION));
        container.setGenerator(new KuudraPlaceholderGenerator(new KuudraPlaceholderLayout(arena)));
        return new KuudraHollow(container, manager.createSharedInstance(container), true);
    }

    public void destroy() {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        manager.unregisterInstance(instance);
        manager.unregisterInstance(container);
    }
}
