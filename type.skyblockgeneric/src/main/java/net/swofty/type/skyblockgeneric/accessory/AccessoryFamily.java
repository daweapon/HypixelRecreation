package net.swofty.type.skyblockgeneric.accessory;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An upgrade line (Talisman, Ring, Artifact, Relic...) or an "only one counts" group. Members are
 * item ids, lowest first; some may not exist on this server yet.
 */
public record AccessoryFamily(String id, String name, Type type, List<String> members, @Nullable String pick) {
    public enum Type {
        UPGRADE_LINE,
        EXCLUSIVE_GROUP
    }

    public int tierOf(String itemId) {
        return members.indexOf(itemId);
    }

    /** What /maxprofile gives: the top of a line, or the chosen member of a group. */
    public String best() {
        return pick != null ? pick : members.getLast();
    }
}
