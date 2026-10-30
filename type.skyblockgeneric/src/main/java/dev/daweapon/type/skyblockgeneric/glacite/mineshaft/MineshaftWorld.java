package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.SharedInstance;
import dev.daweapon.type.generic.world.HypixelWorldLoader;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** One shaft's own copy of its layout: the captured map when it is installed, otherwise the placeholder cavern. */
public record MineshaftWorld(InstanceContainer container, SharedInstance instance, boolean placeholder) {
    public static final Path WORLD_DIRECTORY = Path.of("./configuration/world/mineshaft");
    private static final Key FULLBRIGHT_DIMENSION = Key.key("skyblock:island");

    public static Path worldFile(ShaftLayout layout) {
        return WORLD_DIRECTORY.resolve(layout.code() + ".polar");
    }

    public static MineshaftWorld create(ShaftLayout layout) {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        Path world = worldFile(layout);
        if (Files.isRegularFile(world)) {
            InstanceContainer container = manager.createInstanceContainer();
            try {
                return new MineshaftWorld(container, HypixelWorldLoader.loadFrom(world, container, manager), false);
            } catch (IOException exception) {
                manager.unregisterInstance(container);
                Logger.error(exception, "Couldn't load {}; using the placeholder mineshaft", world);
            }
        }
        InstanceContainer container = manager.createInstanceContainer(
                MinecraftServer.getDimensionTypeRegistry().getKey(FULLBRIGHT_DIMENSION));
        container.setGenerator(new MineshaftPlaceholderGenerator(layout));
        return new MineshaftWorld(container, manager.createSharedInstance(container), true);
    }

    public void destroy() {
        InstanceManager manager = MinecraftServer.getInstanceManager();
        manager.unregisterInstance(instance);
        manager.unregisterInstance(container);
    }
}
