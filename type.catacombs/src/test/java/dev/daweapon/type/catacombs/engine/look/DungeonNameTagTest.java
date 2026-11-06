package dev.daweapon.type.catacombs.engine.look;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Our name tags render to the same legacy text Hypixel sent in the captured runs. */
class DungeonNameTagTest {
    private static String legacy(String markup) {
        return LegacyComponentSerializer.legacySection().serialize(Text.parse(markup).asComponent());
    }

    private static String plain(String legacy) {
        return legacy.replaceAll("§.", "");
    }

    @Test
    void aStarredCryptDreadlord() {
        String tag = legacy(DungeonNameTag.format(List.of(MobType.UNDEAD, MobType.WITHER, MobType.SUBTERRANEAN), true, "<c>", false,
                "Crypt Dreadlord", 35_840, 35_840, null, false));
        assertEquals(plain("§2§8§6 §6✯ §cCrypt Dreadlord §a35,840§c❤"), plain(tag));
        assertEquals(List.of("2", "8", "6", "c", "a", "c"), colours(tag));
    }

    @Test
    void aWatcherSummonHasNoStarAndAGoldName() {
        String tag = legacy(DungeonNameTag.format(List.of(MobType.UNDEAD), false, "<6>", false, "Reaper", 23_000, 23_000, null, false));
        assertEquals(plain("§2 §6Reaper §a23,000§c❤"), plain(tag));
        assertEquals(List.of("2", "6", "a", "c"), colours(tag));
    }

    @Test
    void minibossesAreBoldAndBigHealthIsShortened() {
        String tag = legacy(DungeonNameTag.format(List.of(MobType.HUMANOID, MobType.ARCANE), true, "<d>", true, "Lost Adventurer",
                149_400, 234_000, null, false));
        assertEquals(plain("§e§5 §6✯ §d§lLost Adventurer §a149.4k§c❤"), plain(tag));
    }

    @Test
    void healthTurnsYellowUnderHalfAndRedAtZero() {
        assertEquals("<e>", DungeonNameTag.healthColour(14_649, 50_000));
        assertEquals("<a>", DungeonNameTag.healthColour(44_968, 50_000));
        assertEquals("<c>", DungeonNameTag.healthColour(0, 50_000));
        assertEquals("103.7k", DungeonNameTag.number(103_700, 250_000));
        assertEquals("1,584", DungeonNameTag.number(1_584, 1_584));
    }

    @Test
    void undeadSkeletonsShowTheirLevelAndMaxHealth() {
        String tag = legacy(DungeonNameTag.format(List.of(MobType.UNDEAD, MobType.SKELETAL), false, "<c>", false, "Undead Skeleton",
                48_920, 50_000, 94, true));
        assertEquals(plain("§8[§7Lv94§8] §2§f §cUndead Skeleton §a48,920§f/§a50,000§c❤"), plain(tag));
    }

    private static List<String> colours(String legacy) {
        return java.util.regex.Pattern.compile("§([0-9a-f])").matcher(legacy).results().map(match -> match.group(1)).toList();
    }
}
