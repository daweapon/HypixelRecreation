package dev.daweapon.type.crimsonisle.dojo;

import dev.daweapon.type.crimsonisle.dojo.tests.ControlChallenge;
import dev.daweapon.type.crimsonisle.dojo.tests.DisciplineChallenge;
import dev.daweapon.type.crimsonisle.dojo.tests.ForceChallenge;
import dev.daweapon.type.crimsonisle.dojo.tests.MasteryChallenge;
import dev.daweapon.type.crimsonisle.dojo.tests.StaminaChallenge;
import dev.daweapon.type.crimsonisle.dojo.tests.SwiftnessChallenge;
import dev.daweapon.type.crimsonisle.dojo.tests.TenacityChallenge;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
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
            case STAMINA -> StaminaChallenge::new;
            case MASTERY -> MasteryChallenge::new;
            case DISCIPLINE -> DisciplineChallenge::new;
            case SWIFTNESS -> SwiftnessChallenge::new;
            case CONTROL -> ControlChallenge::new;
            case TENACITY -> TenacityChallenge::new;
        };
    }
}
