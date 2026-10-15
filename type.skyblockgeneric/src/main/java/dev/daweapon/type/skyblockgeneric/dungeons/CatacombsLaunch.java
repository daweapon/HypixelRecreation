package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.type.skyblockgeneric.launch.LaunchFlag;

import java.nio.file.Path;
import java.util.function.LongSupplier;

/** The Catacombs launch flag, {@code public} in {@code configuration/skyblock/dungeons/launch.yml}. */
public final class CatacombsLaunch extends LaunchFlag {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/launch.yml");

    public CatacombsLaunch(Path file, LongSupplier clock) {
        super("Catacombs", file, clock);
    }
}
