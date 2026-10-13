package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Consumer;

public final class GreenhouseMobs {
    private GreenhouseMobs() {
    }

    public abstract static class Summoned extends MinionSpawnedMob {
        private Consumer<SkyBlockPlayer> onSlain = killer -> {
        };

        protected Summoned(EntityType type) {
            super(type);
        }

        public void spawnAt(Instance instance, Pos position, Consumer<SkyBlockPlayer> onSlain) {
            this.onSlain = onSlain;
            setInstance(instance, position);
        }

        @Override
        public void kill() {
            SkyBlockPlayer killer = getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player ? player : null;
            super.kill();
            if (killer != null) onSlain.accept(killer);
        }

        @Override
        public List<MobType> getMobTypes() {
            return List.of();
        }
    }

    public static final class Zombuddy extends Summoned {
        public Zombuddy() {
            super(EntityType.ZOMBIE);
        }

        @Override
        public String getDisplayName() {
            return "Zombuddy";
        }

        @Override
        protected double health() {
            return 500;
        }
    }

    public static final class TimestalkClone extends Summoned {
        public TimestalkClone() {
            super(EntityType.ZOMBIE);
        }

        @Override
        public String getDisplayName() {
            return "Timestalk Clone";
        }

        @Override
        protected double health() {
            return 2_000;
        }
    }

    public static final class Rat extends Summoned {
        public Rat() {
            super(EntityType.SILVERFISH);
        }

        @Override
        public String getDisplayName() {
            return "Rat";
        }

        @Override
        protected double health() {
            return 100;
        }

        @Override
        protected boolean meleeAttacker() {
            return false;
        }
    }
}
