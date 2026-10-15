package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMob;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.mob.CrystalHollowsMobSpec.Drop;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Grunt (wiki: Grunt): throws a ball that spawns a Silvo, which despawns after a few seconds. The wiki gives no
 * throw interval, range or Silvo cap or lifetime; those are estimates.
 */
public class MobGrunt extends CrystalHollowsMob implements RegionPopulator {
    private static final int THROW_INTERVAL = 200;
    private static final double THROW_RANGE = 16;
    private static final int SILVO_DELAY = 15;
    private static final int SILVO_LIFETIME = 160;

    private MobSilvo silvo;

    private static final CrystalHollowsMobSpec SPEC = CrystalHollowsMobSpec.of("TEAM_TREASURITE_GRUNT", "Grunt", 54, 15_000, 300)
            .bestiary(1, 10, Material.SILVERFISH_SPAWN_EGG)
            .rewards(81, 18, 59)
            .drops(
                    Drop.always(ItemType.ROUGH_RUBY_GEM, 8, 15),
                    Drop.always(ItemType.ROUGH_JADE_GEM, 8, 15),
                    Drop.single(ItemType.TREASURITE, 1));

    public MobGrunt() {
        super(HollowsSkins.PLAYER_MODEL);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        HollowsSkins.dress(this, HollowsSkins.GRUNT);
        setItemInMainHand(ItemStack.of(Material.IRON_PICKAXE));
    }

    @Override
    protected CrystalHollowsMobSpec spec() {
        return SPEC;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % THROW_INTERVAL != 0 || silvo != null && !silvo.isDead() && !silvo.isRemoved()) return;
        nearestPlayer(THROW_RANGE).ifPresent(this::summonSilvo);
    }

    private void summonSilvo(SkyBlockPlayer target) {
        MobProjectile.item(this, target, ItemStack.of(Material.SNOWBALL), 0.8, hit -> {
        });
        Pos landing = target.getPosition();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isDead() || isRemoved() || getInstance() == null) return;
            MobSilvo summoned = new MobSilvo();
            summoned.setInstance(getInstance(), landing);
            silvo = summoned;
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (!summoned.isRemoved()) summoned.remove();
            }).delay(TaskSchedule.tick(SILVO_LIFETIME)).schedule();
        }).delay(TaskSchedule.tick(SILVO_DELAY)).schedule();
    }

    @Override
    public void remove() {
        if (silvo != null && !silvo.isRemoved()) silvo.remove();
        super.remove();
    }

    @Override
    protected RegionType homeRegion() {
        return RegionType.MITHRIL_DEPOSITS;
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.MITHRIL_DEPOSITS, 3));
    }
}
