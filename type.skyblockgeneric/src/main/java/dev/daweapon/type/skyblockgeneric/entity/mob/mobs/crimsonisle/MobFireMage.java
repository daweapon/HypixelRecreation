package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.crimson.mob.LavaBeam;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

/**
 * Fire Mage (wiki: Fire_Mage): neutral until provoked, then alternates an 8 s fire beam and a 2 s fire barrage on
 * a 3 s cooldown, holding Enchanted Blaze Powder while hostile. Killing one costs 5 Mage Reputation. The
 * beam/barrage split and beam tick damage are estimates.
 */
public class MobFireMage extends CrimsonIsleMob {
    public MobFireMage() {
        super(CrimsonMobs.FIRE_MAGE);
    }

    private static final int BARRAGE_COOLDOWN_TICKS = 60;
    private static final int BEAM_TICKS = 160;
    private int beam;
    private int cooldown;

    @Override
    protected Temper temper() {
        return Temper.NEUTRAL;
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected void onAiTick(int ticks) {
        Optional<SkyBlockPlayer> target = rangedTarget(16);
        setItemInMainHand(target.isPresent() ? ItemStack.of(Material.BLAZE_POWDER) : ItemStack.AIR);
        if (target.isEmpty()) return;
        SkyBlockPlayer player = target.get();
        lookAt(player);
        if (beam > 0) {
            beam--;
            LavaBeam.draw(getInstance(), getPosition().add(0, 1.6, 0), player.getPosition().add(0, 1, 0));
            if (beam % 20 == 0) strike(player, getProfile().damage());
            return;
        }
        if (--cooldown > 0) return;
        if (LootRoll.roll(null, null, 30)) {
            beam = BEAM_TICKS;
        } else {
            for (int i = 0; i < 4; i++) fireball(player, getProfile().damage());
            cooldown = BARRAGE_COOLDOWN_TICKS;
        }
    }
}
