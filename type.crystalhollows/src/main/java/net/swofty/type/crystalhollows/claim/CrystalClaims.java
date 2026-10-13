package net.swofty.type.crystalhollows.claim;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.crystalhollows.temple.JungleTemple;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public final class CrystalClaims {
    public static final String CLAIM_ANCHOR = "crystal_claim";
    public static final String DEN_STRUCTURE = "goblin_queens_den";

    public enum Result {
        GRANTED,
        NOT_EARNED,
        ALREADY_HAVE
    }

    private CrystalClaims() {
    }

    public static Result amethyst(boolean activeKey, boolean parkoursDone, CrystalNucleus.State state) {
        if (state != CrystalNucleus.State.NOT_FOUND) return Result.ALREADY_HAVE;
        return activeKey && parkoursDone ? Result.GRANTED : Result.NOT_EARNED;
    }

    public static Result amber(boolean denAccess, CrystalNucleus.State state) {
        if (state != CrystalNucleus.State.NOT_FOUND) return Result.ALREADY_HAVE;
        return denAccess ? Result.GRANTED : Result.NOT_EARNED;
    }

    public static NucleusCrystal crystalAt(Point point) {
        if (matches(JungleTemple.STRUCTURE, point)) return NucleusCrystal.AMETHYST;
        if (matches(DEN_STRUCTURE, point)) return NucleusCrystal.AMBER;
        return null;
    }

    private static boolean matches(String structure, Point point) {
        Pos claim = CrystalHollowsLayout.anchor(structure, CLAIM_ANCHOR);
        return claim != null && claim.blockX() == point.blockX() && claim.blockY() == point.blockY()
                && claim.blockZ() == point.blockZ();
    }

    public static void claim(SkyBlockPlayer player, NucleusCrystal crystal) {
        UUID id = player.getUuid();
        Result result = crystal == NucleusCrystal.AMETHYST
                ? amethyst(CrystalHollowsProgress.hasActiveJungleKey(id), JungleTemple.allParkoursComplete(id),
                CrystalNucleus.state(player, crystal))
                : amber(CrystalHollowsProgress.has(id, CrystalHollowsProgress.DEN_ACCESS), CrystalNucleus.state(player, crystal));
        switch (result) {
            case NOT_EARNED -> player.sendMessage(Text.of("<c>You haven't earned this Crystal! Come back when you have >:-)"));
            case ALREADY_HAVE -> player.sendMessage(Text.of("<c>You already have this Crystal! Place it at the Crystal Nucleus."));
            case GRANTED -> grant(player, crystal);
        }
    }

    private static void grant(SkyBlockPlayer player, NucleusCrystal crystal) {
        if (!CrystalNucleus.claim(player, crystal)) return;
        UUID id = player.getUuid();
        if (crystal == NucleusCrystal.AMETHYST) CrystalHollowsProgress.consumeJungleKey(id);
        else CrystalHollowsProgress.clear(id, CrystalHollowsProgress.DEN_ACCESS);
        player.sendMessage(Text.of("<a>You claimed the {}<a>! Place it at the Crystal Nucleus.", crystal.coloured()));
    }
}
