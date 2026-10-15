package dev.daweapon.type.crystalhollows.gate;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crystalhollows.claim.CrystalClaims;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.quest.CrystalHollowsProgress;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public record EntryGate(RegionType region, String structure, Predicate<UUID> allowed, String denied,
                        java.util.function.Consumer<UUID> onEnter) {
    public static final String EXIT_ANCHOR = "entrance";

    public static final List<EntryGate> GATES = List.of(
            new EntryGate(RegionType.GOBLIN_QUEENS_DEN, CrystalClaims.DEN_STRUCTURE, CrystalHollowsProgress::hasKingsScent,
                    "<c>The Goblin Queen's guards throw you out! You need King Yolkar's scent to sneak past them.",
                    id -> CrystalHollowsProgress.mark(id, CrystalHollowsProgress.DEN_ACCESS)));

    public static boolean entered(RegionType region, RegionType previous, RegionType gated) {
        return region == gated && previous != gated;
    }

    public boolean apply(SkyBlockPlayer player, RegionType previous, RegionType current) {
        if (!entered(current, previous, region)) return true;
        Pos exit = CrystalHollowsLayout.anchor(structure, EXIT_ANCHOR);
        if (allowed.test(player.getUuid())) {
            onEnter.accept(player.getUuid());
            return true;
        }
        if (exit == null) return true;
        player.teleport(exit);
        player.sendMessage(Text.of(denied));
        return false;
    }
}
