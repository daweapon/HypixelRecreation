package dev.daweapon.type.skyblockgeneric.torrhus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class StatuePuzzle {
    public enum Animal {
        BIRD(0, 0), SNAKE(1, 0), FROG(1, 1), FLY(0, 1);

        private final int column;
        private final int row;

        Animal(int column, int row) {
            this.column = column;
            this.row = row;
        }

        public int column() {
            return column;
        }

        public int row() {
            return row;
        }
    }

    public enum Facing {
        NORTH(0, -1, 180), EAST(1, 0, 270), SOUTH(0, 1, 0), WEST(-1, 0, 90);

        private final int dx;
        private final int dz;
        private final float yaw;

        Facing(int dx, int dz, float yaw) {
            this.dx = dx;
            this.dz = dz;
            this.yaw = yaw;
        }

        public float yaw() {
            return yaw;
        }

        public Facing clockwise() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private final Map<Animal, Facing> facing = new EnumMap<>(Animal.class);

    public StatuePuzzle(Map<Animal, Facing> initial) {
        facing.putAll(initial);
    }

    public static StatuePuzzle scrambled(RandomGenerator random) {
        while (true) {
            Map<Animal, Facing> initial = new EnumMap<>(Animal.class);
            for (Animal animal : Animal.values()) initial.put(animal, Facing.values()[random.nextInt(Facing.values().length)]);
            StatuePuzzle puzzle = new StatuePuzzle(initial);
            if (!puzzle.solved()) return puzzle;
        }
    }

    public Facing facing(Animal animal) {
        return facing.get(animal);
    }

    public void rotate(Animal animal) {
        facing.put(animal, facing.get(animal).clockwise());
    }

    public Optional<Animal> target(Animal animal) {
        Facing direction = facing.get(animal);
        Animal best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Animal other : Animal.values()) {
            if (other == animal) continue;
            int deltaColumn = other.column - animal.column;
            int deltaRow = other.row - animal.row;
            boolean onLine = direction.dx != 0 ? deltaRow == 0 && deltaColumn * direction.dx > 0
                    : deltaColumn == 0 && deltaRow * direction.dz > 0;
            int distance = Math.abs(deltaColumn) + Math.abs(deltaRow);
            if (onLine && distance < bestDistance) {
                best = other;
                bestDistance = distance;
            }
        }
        return Optional.ofNullable(best);
    }

    public boolean solved() {
        return target(Animal.BIRD).filter(Animal.SNAKE::equals).isPresent()
                && target(Animal.SNAKE).filter(Animal.FROG::equals).isPresent()
                && target(Animal.FROG).filter(Animal.FLY::equals).isPresent()
                && target(Animal.FLY).isEmpty();
    }
}
