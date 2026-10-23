package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Pos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TrainingDummies {
    public static final String STORAGE_KEY = "training_dummies";
    public static final int RESPAWN_TICKS = 100;
    private static final Map<UUID, TrainingDummySet> LOADED = new ConcurrentHashMap<>();

    private TrainingDummies() {
    }

    public static TrainingDummySet of(SkyBlockIsland island) {
        return LOADED.computeIfAbsent(island.getIslandID(),
                _ -> TrainingDummySet.parse(String.valueOf(island.getDatabase().get(STORAGE_KEY, ""))));
    }

    public static void save(SkyBlockIsland island) {
        island.getDatabase().insertOrUpdate(STORAGE_KEY, of(island).serialize());
    }

    public static void spawnAll(SkyBlockIsland island) {
        of(island).all().forEach(dummy -> spawn(island, dummy));
    }

    public static boolean place(SkyBlockPlayer player, SkyBlockIsland island, Pos position) {
        TrainingDummySet set = of(island);
        if (set.isFull()) {
            player.sendMessage("<c>You can only place up to {} Training Dummies on your island!", TrainingDummySet.MAX_DUMMIES);
            return false;
        }
        if (set.tooClose(position.x(), position.y(), position.z())) {
            player.sendMessage("<c>Training Dummies must be at least {} blocks apart!", (int) TrainingDummySet.MIN_SPACING);
            return false;
        }
        PlacedTrainingDummy dummy = new PlacedTrainingDummy(position.x(), position.y(), position.z(), position.yaw(),
                TrainingDummyForm.DEFAULT);
        set.add(dummy);
        save(island);
        spawn(island, dummy);
        return true;
    }

    public static void changeForm(SkyBlockIsland island, TrainingDummyMob mob, TrainingDummyForm form) {
        PlacedTrainingDummy old = mob.placed();
        PlacedTrainingDummy updated = old.withForm(form);
        of(island).replace(old, updated);
        save(island);
        mob.remove();
        spawn(island, updated);
    }

    public static void pickUp(SkyBlockIsland island, TrainingDummyMob mob, SkyBlockPlayer player) {
        if (!of(island).remove(mob.placed())) return;
        save(island);
        mob.remove();
        player.addAndUpdateItem(new SkyBlockItem(ItemType.TRAINING_DUMMY));
        player.sendMessage("<a>You picked up the Training Dummy!");
    }

    public static void respawnLater(SkyBlockIsland island, PlacedTrainingDummy dummy) {
        MathUtility.delay(() -> {
            if (island.getIslandInstance() == null || !of(island).all().contains(dummy)) return;
            spawn(island, dummy);
        }, RESPAWN_TICKS);
    }

    private static void spawn(SkyBlockIsland island, PlacedTrainingDummy dummy) {
        TrainingDummyMob mob = new TrainingDummyMob(dummy.form());
        mob.placed(dummy);
        mob.setInstance(island.getIslandInstance(), dummy.position());
    }
}
