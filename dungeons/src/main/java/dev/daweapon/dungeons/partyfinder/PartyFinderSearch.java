package dev.daweapon.dungeons.partyfinder;

import java.util.Comparator;
import java.util.Locale;

/**
 * A player's Party Finder search settings: the floor or tier ({@code null} for any), text matched against the
 * note and leader, the range of required levels to show, and the sort order.
 */
public record PartyFinderSearch<K>(K key, String text, int minimumLevel, int maximumLevel, Sort sort) {
    public enum Sort {
        RECENTLY_CREATED("Recently Created"),
        HIGHEST_LEVEL("Highest Level Required"),
        LARGEST_GROUP("Largest Group");

        private final String displayName;

        Sort(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public Sort next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public PartyFinderSearch {
        text = text == null ? "" : text.trim();
        if (maximumLevel < minimumLevel) maximumLevel = minimumLevel;
        if (sort == null) sort = Sort.RECENTLY_CREATED;
    }

    public static <K> PartyFinderSearch<K> any(int maximumLevel) {
        return new PartyFinderSearch<>(null, "", 0, maximumLevel, Sort.RECENTLY_CREATED);
    }

    public boolean matches(PartyFinderGroup<K, ?> group) {
        if (!group.open()) return false;
        if (key != null && !key.equals(group.key())) return false;
        if (group.minimumLevel() < minimumLevel || group.minimumLevel() > maximumLevel) return false;
        if (text.isEmpty()) return true;
        String needle = text.toLowerCase(Locale.ROOT);
        return group.note().toLowerCase(Locale.ROOT).contains(needle)
                || group.leader().name().toLowerCase(Locale.ROOT).contains(needle);
    }

    public <C> Comparator<PartyFinderGroup<K, C>> order() {
        Comparator<PartyFinderGroup<K, C>> newest = Comparator.comparingLong(PartyFinderGroup<K, C>::createdAt).reversed();
        return switch (sort) {
            case RECENTLY_CREATED -> newest;
            case HIGHEST_LEVEL -> Comparator.<PartyFinderGroup<K, C>>comparingInt(PartyFinderGroup::minimumLevel).reversed().thenComparing(newest);
            case LARGEST_GROUP -> Comparator.<PartyFinderGroup<K, C>>comparingInt(PartyFinderGroup::size).reversed().thenComparing(newest);
        };
    }

    public PartyFinderSearch<K> withKey(K key) {
        return new PartyFinderSearch<>(key, text, minimumLevel, maximumLevel, sort);
    }

    public PartyFinderSearch<K> withText(String text) {
        return new PartyFinderSearch<>(key, text, minimumLevel, maximumLevel, sort);
    }

    public PartyFinderSearch<K> withLevels(int minimumLevel, int maximumLevel) {
        return new PartyFinderSearch<>(key, text, minimumLevel, maximumLevel, sort);
    }

    public PartyFinderSearch<K> withSort(Sort sort) {
        return new PartyFinderSearch<>(key, text, minimumLevel, maximumLevel, sort);
    }
}
