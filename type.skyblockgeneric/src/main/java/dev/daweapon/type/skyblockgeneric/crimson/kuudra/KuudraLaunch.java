package dev.daweapon.type.skyblockgeneric.crimson.kuudra;

import dev.daweapon.type.skyblockgeneric.launch.LaunchFlag;

import java.nio.file.Path;
import java.util.function.LongSupplier;

/** The Kuudra launch flag, {@code public} in {@code configuration/skyblock/kuudra/launch.yml}. */
public final class KuudraLaunch extends LaunchFlag {
    public static final Path FILE = Path.of("./configuration/skyblock/kuudra/launch.yml");

    public KuudraLaunch(Path file, LongSupplier clock) {
        super("Kuudra", file, clock);
    }
}
