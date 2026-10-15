package dev.daweapon.type.skyblockgeneric.shop.catalogue;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.faction.ReputationTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A catalogue entry's faction reputation gate, written in shop YAML as
 * {@code requires: { faction: MAGE, tier: HERO }} or {@code requires: { faction: MAGE, reputation: 12000 }}.
 */
public record ReputationRequirement(CrimsonFaction faction, int reputation) {

    public static @Nullable ReputationRequirement parse(@Nullable Object raw) {
        if (!(raw instanceof Map<?, ?> map) || map.get("faction") == null) return null;
        CrimsonFaction faction = CrimsonFaction.byName(String.valueOf(map.get("faction")));
        if (faction == null) return null;
        int reputation = map.get("reputation") instanceof Number number ? number.intValue() : 0;
        if (map.get("tier") != null) {
            reputation = ReputationTier.valueOf(String.valueOf(map.get("tier")).toUpperCase()).getRequiredReputation();
        }
        return new ReputationRequirement(faction, reputation);
    }

    public boolean isMet(SkyBlockPlayer player) {
        return CrimsonReputationService.get(player, faction) >= reputation;
    }

    public String lockLine() {
        return "<4>❣ <c>Requires <c>" + StringUtility.commaify(reputation) + " " + faction.getDisplayName() + " Reputation.";
    }
}
