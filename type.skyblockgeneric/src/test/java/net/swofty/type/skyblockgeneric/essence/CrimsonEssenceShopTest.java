package net.swofty.type.skyblockgeneric.essence;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraLaunch;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CrimsonEssenceShopTest {
    @TempDir
    Path directory;

    @Test
    void skyBlockXPAddsUpToTheSource() {
        assertEquals(SkyBlockXPSource.CRIMSON_ESSENCE_SHOP.getMaxXP(), CrimsonEssenceShop.SHOP.maxSkyBlockXP());
    }

    @Test
    void maxingEveryPerkCostsTheWikiTotal() {
        assertEquals(37_500, Arrays.stream(CrimsonEssencePerk.values()).mapToLong(EssencePerk::totalCost).sum());
    }

    @AfterEach
    void restore() {
        KuudraAvailability.use(new KuudraLaunch(KuudraLaunch.FILE, System::currentTimeMillis));
    }

    @Test
    void kuudraPerksFollowTheLaunchSwitch() throws IOException {
        KuudraAvailability.use(new KuudraLaunch(launchFile("public: false\n"), () -> 0));
        assertEquals(EssenceShop.Result.UNAVAILABLE,
                CrimsonEssenceShop.SHOP.check(0, CrimsonEssencePerk.HEADSTART, Long.MAX_VALUE));
        assertEquals(EssenceShop.Result.BOUGHT,
                CrimsonEssenceShop.SHOP.check(0, CrimsonEssencePerk.FUNGUS_FORTUNA, 100));

        KuudraAvailability.use(new KuudraLaunch(launchFile("public: true\n"), () -> 0));
        assertEquals(EssenceShop.Result.BOUGHT,
                CrimsonEssenceShop.SHOP.check(0, CrimsonEssencePerk.HEADSTART, 100));
        assertNull(CrimsonEssencePerk.KUUDRA_MASTER.getUnavailableReason());
    }

    private Path launchFile(String content) throws IOException {
        return Files.writeString(directory.resolve("launch-" + content.hashCode() + ".yml"), content);
    }
}
