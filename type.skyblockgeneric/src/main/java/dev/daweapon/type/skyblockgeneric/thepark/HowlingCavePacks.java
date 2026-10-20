package dev.daweapon.type.skyblockgeneric.thepark;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark.MobHowlingSpirit;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.thepark.MobPackSpirit;

import java.util.List;
import java.util.function.Supplier;

/**
 * The four fixed Soul of the Alpha spots in the Howling Cave and the spirits grouped around each, picked from a scan
 * of SKYBLOCK_THE_PARK.polar: every spot stands on solid ground with at least three blocks of air above.
 */
public final class HowlingCavePacks {
    public static final long RESPAWN_MILLIS = 15_000;
    public static final Pos CAVE_CENTRE = new Pos(-384.5, 88, -64.5);
    public static final double ACTIVE_RANGE = 64;

    public enum SpiritKind {
        PACK(MobPackSpirit::new),
        HOWLING(MobHowlingSpirit::new);

        private final Supplier<SkyBlockMob> factory;

        SpiritKind(Supplier<SkyBlockMob> factory) {
            this.factory = factory;
        }

        public SkyBlockMob create() {
            return factory.get();
        }
    }

    public record Spirit(SpiritKind kind, Pos spot) {
    }

    public record Pack(String name, Pos alpha, List<Spirit> spirits) {
    }

    public static final List<Pack> PACKS = List.of(
            new Pack("east hollow", new Pos(-372.5, 76, -78.5), List.of(
                    new Spirit(SpiritKind.PACK, new Pos(-374.5, 76, -75.5)),
                    new Spirit(SpiritKind.PACK, new Pos(-371.5, 76, -81.5)),
                    new Spirit(SpiritKind.HOWLING, new Pos(-368.5, 76, -77.5)),
                    new Spirit(SpiritKind.HOWLING, new Pos(-373.5, 76, -74.5)))),
            new Pack("west chamber", new Pos(-395.5, 82, -51.5), List.of(
                    new Spirit(SpiritKind.PACK, new Pos(-398.5, 82, -53.5)),
                    new Spirit(SpiritKind.PACK, new Pos(-393.5, 82, -49.5)),
                    new Spirit(SpiritKind.HOWLING, new Pos(-392.5, 82, -52.5)))),
            new Pack("west chamber mouth", new Pos(-388.5, 82, -47.5), List.of(
                    new Spirit(SpiritKind.PACK, new Pos(-385.5, 82, -47.5)),
                    new Spirit(SpiritKind.PACK, new Pos(-390.5, 82, -46.5)),
                    new Spirit(SpiritKind.HOWLING, new Pos(-381.5, 82, -48.5)))),
            new Pack("central ledge", new Pos(-387.5, 83, -60.5), List.of(
                    new Spirit(SpiritKind.PACK, new Pos(-388.5, 83, -62.5)),
                    new Spirit(SpiritKind.PACK, new Pos(-389.5, 83, -56.5)),
                    new Spirit(SpiritKind.HOWLING, new Pos(-384.5, 81, -71.5)),
                    new Spirit(SpiritKind.PACK, new Pos(-384.5, 81, -69.5)))));

    private HowlingCavePacks() {
    }
}
