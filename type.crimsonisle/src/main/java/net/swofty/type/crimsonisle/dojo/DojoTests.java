package net.swofty.type.crimsonisle.dojo;

import net.swofty.type.crimsonisle.dojo.tests.ForceChallenge;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** Which challenge class runs each test. A test without one is "not safe right now". */
public final class DojoTests {
    public static final String DISABLED = "It is not safe to do that challenge right now. Come back later!";

    private DojoTests() {
    }

    public static boolean isAvailable(DojoTest test) {
        return factory(test) != null;
    }

    public static DojoChallenge create(DojoTest test, SkyBlockPlayer player) {
        Function<SkyBlockPlayer, DojoChallenge> factory = factory(test);
        if (factory == null) throw new IllegalStateException("No challenge for " + test);
        return factory.apply(player);
    }

    private static @Nullable Function<SkyBlockPlayer, DojoChallenge> factory(DojoTest test) {
        return switch (test) {
            case FORCE -> ForceChallenge::new;
            default -> null;
        };
    }
}
