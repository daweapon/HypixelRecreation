package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.type.skyblockgeneric.elections.ElectionData;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor.Perk;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ActivePerks {
    public static final ActivePerks NONE = new ActivePerks(null, null, null, null, null,
            EnumSet.noneOf(Perk.class), null, EnumSet.noneOf(Perk.class));

    private final ElectionData source;
    private final String sourceMayor;
    private final List<String> sourceMayorPerks;
    private final String sourceMinisterPerk;
    private final SkyBlockMayor perkpocalypseMayor;
    private final Set<Perk> mayorPerks;
    private final Perk ministerPerk;
    private final Set<Perk> active;

    private ActivePerks(ElectionData source, String sourceMayor, List<String> sourceMayorPerks,
                        String sourceMinisterPerk, SkyBlockMayor perkpocalypseMayor,
                        Set<Perk> mayorPerks, Perk ministerPerk, Set<Perk> active) {
        this.source = source;
        this.sourceMayor = sourceMayor;
        this.sourceMayorPerks = sourceMayorPerks;
        this.sourceMinisterPerk = sourceMinisterPerk;
        this.perkpocalypseMayor = perkpocalypseMayor;
        this.mayorPerks = mayorPerks;
        this.ministerPerk = ministerPerk;
        this.active = active;
    }

    public static ActivePerks of(@Nullable ElectionData data, @Nullable SkyBlockMayor perkpocalypseMayor) {
        if (data == null) return NONE;
        Set<Perk> mayorPerks = EnumSet.noneOf(Perk.class);
        mayorPerks.addAll(data.getCurrentMayorPerkEnums());
        Perk ministerPerk = data.getMinisterPerkEnum();

        Set<Perk> active = EnumSet.copyOf(mayorPerks);
        if (ministerPerk != null) active.add(ministerPerk);
        if (perkpocalypseMayor != null) active.addAll(List.of(perkpocalypseMayor.getAllPerks()));

        return new ActivePerks(data, data.getCurrentMayor(), data.getCurrentMayorPerks(), data.getMinisterPerk(),
                perkpocalypseMayor, mayorPerks, ministerPerk, active);
    }

    boolean reflects(ElectionData data, SkyBlockMayor perkpocalypse) {
        return source == data
                && data != null
                && sourceMayor == data.getCurrentMayor()
                && sourceMayorPerks == data.getCurrentMayorPerks()
                && sourceMinisterPerk == data.getMinisterPerk()
                && perkpocalypseMayor == perkpocalypse;
    }

    public boolean isActive(Perk perk) {
        return active.contains(perk);
    }

    public boolean isMayorPerk(Perk perk) {
        return mayorPerks.contains(perk);
    }

    public boolean isMinisterPerk(Perk perk) {
        return ministerPerk == perk;
    }

    public double bonus(Perk perk) {
        return isActive(perk) ? PerkMagnitudes.of(perk) : 0D;
    }

    public double multiplier(Perk perk) {
        return 1D + bonus(perk) / 100D;
    }

    public double factor(Perk perk) {
        return isActive(perk) ? PerkMagnitudes.of(perk) : 1D;
    }

    public Set<Perk> all() {
        return EnumSet.copyOf(active);
    }
}
