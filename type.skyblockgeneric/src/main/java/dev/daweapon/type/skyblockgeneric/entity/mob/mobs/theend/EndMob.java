package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

abstract class EndMob extends CrystalHollowsMob {
    protected EndMob(EntityType entityType) {
        super(entityType);
    }

    protected List<EndMobSpecs.Extra> extras() {
        return List.of();
    }

    protected void onKilledBy(SkyBlockPlayer player, Instance instance, Pos position) {
    }

    protected void onDeath(Instance instance, Pos position) {
    }

    @Override
    public SkyBlockItem createDrop(ItemType type, int amount) {
        int bonus = 0;
        for (EndMobSpecs.Extra extra : extras()) {
            if (extra.item() != type) continue;
            for (double chance : extra.chancesPercent()) {
                if (ThreadLocalRandom.current().nextDouble() * 100 < chance) bonus++;
            }
        }
        return super.createDrop(type, amount + bonus);
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Pos position = getPosition();
        Optional<SkyBlockPlayer> killer = getLastDamageSource() != null && getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player
                ? Optional.of(player) : Optional.empty();
        super.kill();
        killer.ifPresent(player -> onKilledBy(player, instance, position));
        onDeath(instance, position);
    }
}
