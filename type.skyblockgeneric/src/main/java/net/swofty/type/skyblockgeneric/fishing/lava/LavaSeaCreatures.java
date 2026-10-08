package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Builds the right mob class for each lava Sea Creature and applies the spawn rules shared by all of them: the
 * per-angler cap of 10 and the Corrupted roll.
 */
public final class LavaSeaCreatures {
    private LavaSeaCreatures() {
    }

    public static LavaSeaCreatureMob create(LavaSeaCreature creature, @Nullable SkyBlockPlayer angler, boolean corrupted) {
        return switch (creature) {
            case MAGMA_SLUG -> new MagmaSlugMob(angler, corrupted);
            case THUNDER -> new ThunderMob(angler, corrupted);
            case LORD_JAWBUS -> new LordJawbusMob(angler);
            case RAGNAROK -> new RagnarokMob(angler, corrupted);
            case VOLCANIC_SNAIL -> new VolcanicSnailMob(angler, corrupted);
            case FRIED_CHICKEN, FIREPROOF_WITCH -> new IgnitingSeaCreatureMob(creature, angler, corrupted);
            default -> new LavaSeaCreatureMob(creature, angler, corrupted);
        };
    }

    public static @Nullable SkyBlockMob spawnFor(LavaSeaCreature creature, @Nullable SkyBlockPlayer angler) {
        if (angler == null) return create(creature, null, false);
        if (LavaSeaCreatureMob.ownedBy(angler) >= LavaSeaCreatureMob.PER_PLAYER_CAP) {
            angler.sendMessage(LavaSeaCreatureMob.CAP_MESSAGE);
            return null;
        }
        boolean corrupted = creature.canBeCorrupted()
                && CorruptedSeaCreatures.roll(angler, new SkyBlockItem(angler.getItemInMainHand()));
        return create(creature, angler, corrupted);
    }
}
