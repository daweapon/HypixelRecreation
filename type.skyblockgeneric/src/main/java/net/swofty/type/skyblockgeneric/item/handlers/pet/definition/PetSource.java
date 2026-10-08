package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Where a pet comes from. Hypixel pet tooltips never name a source, so the line is only shown
 * while the source is not built yet, telling players why they can't get the pet.
 */
public record PetSource(String text, boolean active) {

    public static @Nullable PetSource parse(@Nullable Object raw, String where) {
        if (raw == null) return null;
        if (raw instanceof String text) return new PetSource(text, true);
        if (!(raw instanceof Map<?, ?> map) || !(map.get("text") instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(where + ": source needs a text");
        }
        return new PetSource(text, !Boolean.FALSE.equals(map.get("active")));
    }

    public @Nullable String loreLine() {
        return active ? null : "<8>Source: " + text + " (Not active yet)";
    }
}
