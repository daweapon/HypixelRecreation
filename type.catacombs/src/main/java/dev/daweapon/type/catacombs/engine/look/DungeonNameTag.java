package dev.daweapon.type.catacombs.engine.look;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Hypixel's dungeon name tag, as captured: each mob type as its coloured pack glyph, a gold ✯ on starred mobs, the
 * name, then the health in green, yellow under half and a red 0 when dead. Health is written out in full
 * ({@code 24,000}) and shortened ({@code 130k}) from {@value #SHORT_FROM} max health up, and a few mobs add a level and
 * their max health ({@code [Lv94] ... 50,000/50,000}).
 */
public final class DungeonNameTag {
    static final long SHORT_FROM = 100_000;
    private static final Map<MobType, String> GLYPHS = Map.of(
            MobType.UNDEAD, "mobs_undead",
            MobType.SKELETAL, "mobs_skeletal",
            MobType.SUBTERRANEAN, "mobs_subterranean",
            MobType.WITHER, "mobs_wither",
            MobType.HUMANOID, "mobs_humanoid",
            MobType.ARCANE, "mobs_arcane",
            MobType.TIMID, "mobs_timid");

    private DungeonNameTag() {
    }

    public static String types(List<MobType> types) {
        StringBuilder out = new StringBuilder();
        for (MobType type : types) {
            String glyph = GLYPHS.get(type);
            out.append(type.getColor()).append(glyph == null ? type.getSymbol() : "<glyph:" + glyph + ">");
        }
        return out.toString();
    }

    public static String format(List<MobType> types, boolean starred, String nameColour, boolean bold, String name,
                                double health, double maxHealth, @Nullable Integer level, boolean showMax) {
        StringBuilder out = new StringBuilder();
        if (level != null) out.append("<8>[<7>Lv").append(level).append("<8>] ");
        out.append(types(types)).append(' ');
        if (starred) out.append("<6>✯ ");
        out.append(nameColour);
        out.append(bold ? "<l>" + name + "</l>" : name);
        out.append(' ').append(healthColour(health, maxHealth)).append(number(health, maxHealth));
        if (showMax) out.append("<f>/<a>").append(number(maxHealth, maxHealth));
        return out.append("<c>❤").toString();
    }

    static String healthColour(double health, double maxHealth) {
        if (health <= 0) return "<c>";
        return health * 2 < maxHealth ? "<e>" : "<a>";
    }

    static String number(double health, double maxHealth) {
        double shown = Math.max(0, Math.ceil(health));
        if (maxHealth >= SHORT_FROM) return SkyBlockMob.shortHealth(shown);
        return String.format(Locale.US, "%,d", (long) shown);
    }
}
