package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;

import java.util.function.Predicate;

public abstract class AbstractLoneAdventurer extends AbstractEndNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQzNGYzOGMxYmIxMDZlMTE5MDhhZDNjYzkwMTYyYzE4Yjg2M2Q2NzgyNjVjODRhODRhMzU4OTAzZjhmN2ExYyIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJlYjNlNmRlMDMyY2EyMWU2YTJlZGMxMTkyYjEyNWJjZiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzUyMTU4MDI2ODk2fQ==";

    protected AbstractLoneAdventurer(Pos position, Predicate<LoneAdventurerStage> standsHere) {
        super(null, "Lone Adventurer", "<9>", TEXTURE, position,
                player -> standsHere.test(LoneAdventurerProgress.stage(player)));
    }
}
