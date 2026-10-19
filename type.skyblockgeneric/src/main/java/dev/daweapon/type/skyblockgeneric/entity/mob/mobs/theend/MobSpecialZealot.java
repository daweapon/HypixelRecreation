package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

public class MobSpecialZealot extends EndMob {
    private UUID summoner;

    public MobSpecialZealot() {
        super(EntityType.ENDERMAN);
    }

    public MobSpecialZealot summonedBy(SkyBlockPlayer player) {
        this.summoner = player.getUuid();
        return this;
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return EndMobSpecs.SPECIAL_ZEALOT;
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.DRAGONS_NEST;
    }

    @Override
    protected List<EndMobSpecs.Extra> extras() {
        return EndMobSpecs.ZEALOT_EXTRAS;
    }

    @Override
    protected void onDeath(Instance instance, Pos position) {
        if (summoner == null) return;
        if (MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(summoner) instanceof SkyBlockPlayer player) {
            CrimsonMobLoot.deliver(player, this, new SkyBlockItem(ItemType.SUMMONING_EYE, 1), true);
        }
    }
}
