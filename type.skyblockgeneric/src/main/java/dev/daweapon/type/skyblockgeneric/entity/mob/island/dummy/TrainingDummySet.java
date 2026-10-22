package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class TrainingDummySet {
    public static final int MAX_DUMMIES = 4;
    public static final double MIN_SPACING = 5;

    private final List<PlacedTrainingDummy> dummies = new ArrayList<>();

    public static TrainingDummySet parse(@Nullable String raw) {
        TrainingDummySet set = new TrainingDummySet();
        if (raw == null || raw.isBlank()) return set;
        for (String entry : raw.split(";")) {
            PlacedTrainingDummy dummy = PlacedTrainingDummy.deserialize(entry);
            if (dummy != null) set.dummies.add(dummy);
        }
        return set;
    }

    public synchronized String serialize() {
        return dummies.stream().map(PlacedTrainingDummy::serialize).collect(Collectors.joining(";"));
    }

    public synchronized List<PlacedTrainingDummy> all() {
        return List.copyOf(dummies);
    }

    public synchronized boolean isFull() {
        return dummies.size() >= MAX_DUMMIES;
    }

    public synchronized boolean tooClose(double x, double y, double z) {
        return dummies.stream().anyMatch(other -> Math.sqrt(
                Math.pow(other.x() - x, 2) + Math.pow(other.y() - y, 2) + Math.pow(other.z() - z, 2)) < MIN_SPACING);
    }

    public synchronized void add(PlacedTrainingDummy dummy) {
        dummies.add(dummy);
    }

    public synchronized boolean remove(PlacedTrainingDummy dummy) {
        return dummies.remove(dummy);
    }

    public synchronized void replace(PlacedTrainingDummy old, PlacedTrainingDummy updated) {
        int index = dummies.indexOf(old);
        if (index >= 0) dummies.set(index, updated);
    }
}
