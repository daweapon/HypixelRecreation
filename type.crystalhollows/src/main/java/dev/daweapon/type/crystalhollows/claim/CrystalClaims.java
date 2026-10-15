package dev.daweapon.type.crystalhollows.claim;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crystalhollows.quest.CrystalHollowsProgress;
import dev.daweapon.type.crystalhollows.temple.JungleTemple;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public final class CrystalClaims {
    public static final String CLAIM_ANCHOR = "crystal_claim";
    public static final String DEN_STRUCTURE = "goblin_queens_den";
    public static final double REACH = 8;

    public enum Result {
        GRANTED,
        NOT_EARNED,
        ALREADY_HAVE
    }

    private CrystalClaims() {
    }

    public static Result amethyst(boolean activeKey, boolean parkoursDone, CrystalNucleus.State state) {
        return resolve(activeKey && parkoursDone, state);
    }

    public static Result amber(boolean denAccess, CrystalNucleus.State state) {
        return resolve(denAccess, state);
    }

    public static Result resolve(boolean earned, CrystalNucleus.State state) {
        if (state != CrystalNucleus.State.NOT_FOUND) return Result.ALREADY_HAVE;
        return earned ? Result.GRANTED : Result.NOT_EARNED;
    }

    public static NucleusCrystal crystalAt(Point point) {
        return ClaimSpots.crystalAt(point);
    }

    public static boolean earned(SkyBlockPlayer player, NucleusCrystal crystal) {
        UUID id = player.getUuid();
        return switch (crystal) {
            case AMETHYST -> CrystalHollowsProgress.hasActiveJungleKey(id) && JungleTemple.allParkoursComplete(id);
            case AMBER -> CrystalHollowsProgress.has(id, CrystalHollowsProgress.DEN_ACCESS);
            case SAPPHIRE -> CrystalHollowsProgress.has(id, CrystalHollowsProgress.SAPPHIRE_STORAGE_OPEN);
            case JADE -> CrystalHollowsProgress.has(id, CrystalHollowsProgress.JADE_CRYSTAL_CLAIMED);
            case TOPAZ -> CrystalNucleus.isAvailable(player, crystal);
        };
    }

    public static boolean collectable(SkyBlockPlayer player, NucleusCrystal crystal) {
        return resolve(earned(player, crystal), CrystalNucleus.state(player, crystal)) == Result.GRANTED;
    }

    public static boolean inReach(SkyBlockPlayer player, NucleusCrystal crystal) {
        ClaimSpot spot = ClaimSpots.of(crystal);
        Pos at = spot == null ? null : spot.crystalPosition();
        return at != null && player.getInstance() != null && player.getPosition().distance(at) <= REACH;
    }

    public static void claim(SkyBlockPlayer player, NucleusCrystal crystal) {
        if (!inReach(player, crystal)) return;
        switch (resolve(earned(player, crystal), CrystalNucleus.state(player, crystal))) {
            case NOT_EARNED -> player.sendMessage(Text.of("<c>You haven't earned this Crystal! Come back when you have >:-)"));
            case ALREADY_HAVE -> player.sendMessage(Text.of("<c>You have already obtained this Crystal!"));
            case GRANTED -> grant(player, crystal);
        }
    }

    private static void grant(SkyBlockPlayer player, NucleusCrystal crystal) {
        if (!CrystalNucleus.claim(player, crystal)) return;
        UUID id = player.getUuid();
        switch (crystal) {
            case AMETHYST -> CrystalHollowsProgress.consumeJungleKey(id);
            case AMBER -> CrystalHollowsProgress.clear(id, CrystalHollowsProgress.DEN_ACCESS);
            default -> {
            }
        }
        CrystalDisplays.refresh(player);
    }
}
