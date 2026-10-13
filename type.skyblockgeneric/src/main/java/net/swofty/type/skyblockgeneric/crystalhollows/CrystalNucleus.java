package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrystalHollows;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CrystalNucleus {
    public static final int XP_PER_RUN = 10;

    private static final Map<UUID, Set<NucleusCrystal>> FOUND = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<NucleusCrystal>> PLACED = new ConcurrentHashMap<>();

    private CrystalNucleus() {
    }

    public enum State {
        NOT_FOUND("<c>Not Found"),
        NOT_PLACED("<e>Not Placed"),
        PLACED("<a>Placed");

        private final String label;

        State(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public static State state(SkyBlockPlayer player, NucleusCrystal crystal) {
        if (PLACED.getOrDefault(player.getUuid(), Set.of()).contains(crystal)) return State.PLACED;
        if (FOUND.getOrDefault(player.getUuid(), Set.of()).contains(crystal)) return State.NOT_PLACED;
        return State.NOT_FOUND;
    }

    public static int placedCount(SkyBlockPlayer player) {
        return PLACED.getOrDefault(player.getUuid(), Set.of()).size();
    }

    public static boolean markFound(SkyBlockPlayer player, NucleusCrystal crystal) {
        if (state(player, crystal) != State.NOT_FOUND) return false;
        FOUND.computeIfAbsent(player.getUuid(), _ -> EnumSet.noneOf(NucleusCrystal.class)).add(crystal);
        return true;
    }

    public static void place(SkyBlockPlayer player, NucleusCrystal crystal) {
        State state = state(player, crystal);
        if (state == State.PLACED) {
            player.sendMessage(Text.of("<c>You have already placed the {}<c>!", crystal.coloured()));
            return;
        }
        if (state == State.NOT_FOUND) {
            player.sendMessage(Text.of("<c>You haven't found the {}<c> yet!", crystal.coloured()));
            return;
        }
        FOUND.get(player.getUuid()).remove(crystal);
        PLACED.computeIfAbsent(player.getUuid(), _ -> EnumSet.noneOf(NucleusCrystal.class)).add(crystal);
        player.sendMessage(Text.of("<a>You placed the {}<a>!", crystal.coloured()));
        if (placedCount(player) < NucleusCrystal.values().length) {
            player.sendMessage(Text.of("<7>Keep exploring the Crystal Hollows to find the rest!"));
            return;
        }
        complete(player);
    }

    private static void complete(SkyBlockPlayer player) {
        PLACED.remove(player.getUuid());
        FOUND.remove(player.getUuid());
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        data.setNucleusRuns(data.getNucleusRuns() + 1);
        CrystalHollowsAccess.save(player, data);
        NucleusRngMeter.addRun(player);
        syncSkyBlockXp(player);
        CrystalBundle.award(player);
    }

    public static void forget(SkyBlockPlayer player) {
        FOUND.remove(player.getUuid());
        PLACED.remove(player.getUuid());
    }

    public static void syncSkyBlockXp(SkyBlockPlayer player) {
        double xp = Math.min(SkyBlockXPSource.CRYSTAL_NUCLEUS_RUNS.getMaxXP(),
                (double) XP_PER_RUN * CrystalHollowsAccess.data(player).getNucleusRuns());
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.CRYSTAL_NUCLEUS_RUNS, xp);
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = CrystalHollowsAccess.data(player);
        int needed = SkyBlockXPSource.CRYSTAL_NUCLEUS_RUNS.getMaxXP() / XP_PER_RUN;
        data.setNucleusRuns(Math.max(data.getNucleusRuns(), needed));
        CrystalHollowsAccess.save(player, data);
        syncSkyBlockXp(player);
    }
}
