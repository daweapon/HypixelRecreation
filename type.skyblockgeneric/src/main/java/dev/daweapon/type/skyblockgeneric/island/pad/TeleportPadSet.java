package dev.daweapon.type.skyblockgeneric.island.pad;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TeleportPadSet {
    public static final int MAX_PADS = 56;

    private final Map<String, TeleportPad> pads = new LinkedHashMap<>();

    public static TeleportPadSet parse(@Nullable String raw) {
        TeleportPadSet set = new TeleportPadSet();
        if (raw == null || raw.isBlank()) return set;
        for (String entry : raw.split(";")) {
            TeleportPad pad = TeleportPad.deserialize(entry);
            if (pad != null) set.pads.put(pad.key(), pad);
        }
        return set;
    }

    public synchronized String serialize() {
        return pads.values().stream().map(TeleportPad::serialize).collect(Collectors.joining(";"));
    }

    public synchronized int size() {
        return pads.size();
    }

    public synchronized boolean isFull() {
        return pads.size() >= MAX_PADS;
    }

    public synchronized @Nullable TeleportPad at(int x, int y, int z) {
        return pads.get(TeleportPad.key(x, y, z));
    }

    public synchronized @Nullable TeleportPad byKey(@Nullable String key) {
        return key == null ? null : pads.get(key);
    }

    public synchronized List<TeleportPad> all() {
        return new ArrayList<>(pads.values());
    }

    public synchronized List<TeleportPad> othersThan(TeleportPad pad) {
        return pads.values().stream().filter(other -> other != pad).toList();
    }

    public synchronized @Nullable TeleportPad add(int x, int y, int z) {
        if (isFull()) return null;
        TeleportPad pad = new TeleportPad(x, y, z, firstUnusedIcon());
        pads.put(pad.key(), pad);
        return pad;
    }

    public synchronized boolean remove(int x, int y, int z) {
        TeleportPad removed = pads.remove(TeleportPad.key(x, y, z));
        if (removed == null) return false;
        pads.values().stream()
                .filter(pad -> removed.key().equals(pad.destination()))
                .forEach(pad -> pad.destination(null));
        return true;
    }

    private int firstUnusedIcon() {
        BitSet used = new BitSet();
        pads.values().forEach(pad -> used.set(pad.icon()));
        int free = used.nextClearBit(0);
        return free < TeleportPadIcon.all().size() ? free : 0;
    }
}
