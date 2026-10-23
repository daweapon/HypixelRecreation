package dev.daweapon.type.skyblockgeneric.technoblade;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ShrineSnapshot(Map<Integer, String> relics, boolean built, long completedAt, @Nullable ActiveEffect effect) {
    public static final ShrineSnapshot EMPTY = new ShrineSnapshot(Map.of(), false, 0, null);

    public record ActiveEffect(AncientPigEffect type, long startedAt, long endsAt) {
        public boolean activeAt(long now) {
            return now < endsAt;
        }

        public String serialize() {
            return type.name() + "|" + startedAt + "|" + endsAt;
        }

        public static @Nullable ActiveEffect parse(@Nullable String raw) {
            if (raw == null) return null;
            String[] parts = raw.split("\\|");
            AncientPigEffect type = AncientPigEffect.parse(parts[0]);
            if (type == null || parts.length < 3) return null;
            return new ActiveEffect(type, Long.parseLong(parts[1]), Long.parseLong(parts[2]));
        }
    }

    public boolean complete(int relicCount) {
        return relicCount > 0 && relics.size() >= relicCount;
    }

    public boolean relicFound(int index) {
        return relics.containsKey(index);
    }

    public @Nullable AncientPigEffect activeEffect(long now) {
        return effect != null && effect.activeAt(now) ? effect.type() : null;
    }

    public boolean standing(boolean perkActive, int relicCount, long windowMillis, long now) {
        if (perkActive) return true;
        if (!built) return false;
        return !complete(relicCount) || now - completedAt < windowMillis;
    }

    public boolean technoshopOpen(boolean perkActive, int relicCount) {
        return perkActive || (built && !complete(relicCount));
    }
}
