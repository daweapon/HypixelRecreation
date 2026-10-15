package dev.daweapon.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Magma Slug splits into 3 Baby Magma Slugs when it dies (wiki: Magma_Slug). The slug's loot goes to whoever
 * kills the last baby; the babies never despawn and count as one Sea Creature toward the cap.
 */
public class MagmaSlugMob extends LavaSeaCreatureMob {
    static final int BABIES = 3;

    public MagmaSlugMob(@Nullable SkyBlockPlayer angler, boolean corrupted) {
        super(LavaSeaCreature.MAGMA_SLUG, angler, corrupted);
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> rollDrops() {
        return List.of();
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Pos at = getPosition();
        List<SkyBlockLootTable.LootRecord> loot = super.rollDrops();
        super.kill();
        if (instance == null) return;
        BabyMagmaSlugMob.Litter litter = new BabyMagmaSlugMob.Litter(new ArrayList<>(loot), getAngler());
        for (int i = 0; i < BABIES; i++) {
            BabyMagmaSlugMob baby = BabyMagmaSlugMob.create(litter);
            litter.babies().add(baby);
            baby.setInstance(instance, at.add((i - 1) * 0.8, 0.2, (i % 2) * 0.8));
        }
    }
}
