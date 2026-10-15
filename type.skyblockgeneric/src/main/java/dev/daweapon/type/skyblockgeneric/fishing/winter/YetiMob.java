package dev.daweapon.type.skyblockgeneric.fishing.winter;

import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

/**
 * The Yeti barely moves and attacks only once every 10 seconds, hurling a hunk of the terrain it
 * stands on at a player, like the Yeti Sword's ability (wiki: Yeti). The angler is the preferred
 * target; anyone else in range is targeted when the angler isn't.
 */
public class YetiMob extends WinterSeaCreatureMob {
    private static final int THROW_INTERVAL_TICKS = 200;
    private static final double THROW_RANGE = 30;
    private static final double HUNK_SPEED = 0.8;

    public YetiMob(@Nullable SkyBlockPlayer angler) {
        super(WinterSeaCreature.YETI, angler);
    }

    @Override
    protected void onTick(int ticks) {
        if (ticks % THROW_INTERVAL_TICKS != 0) return;
        target().ifPresent(target -> MobProjectile.item(this, target, ItemStack.of(terrainUnderfoot()), HUNK_SPEED,
                hit -> hit.damage(new EntityDamage(this, (float) getCreature().getDamage()))));
    }

    private Optional<SkyBlockPlayer> target() {
        SkyBlockPlayer angler = getAngler();
        if (angler != null && angler.getInstance() == getInstance() && !angler.isDead()
                && angler.getPosition().distance(getPosition()) <= THROW_RANGE) {
            return Optional.of(angler);
        }
        return MythologicalCombat.nearby(this, THROW_RANGE).stream()
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())));
    }

    private Material terrainUnderfoot() {
        Instance instance = getInstance();
        if (instance == null) return Material.SNOW_BLOCK;
        Block below = instance.getBlock(getPosition().sub(0, 1, 0));
        Material material = below.registry().material();
        return below.isSolid() && material != null ? material : Material.SNOW_BLOCK;
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        killer.getAchievementHandler().completeAchievement("skyblock.frozen_monster");
    }
}
