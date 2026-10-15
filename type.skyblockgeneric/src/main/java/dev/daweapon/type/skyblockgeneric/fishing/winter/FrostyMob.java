package dev.daweapon.type.skyblockgeneric.fishing.winter;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Frosty throws a snowball at every player within 32 blocks three seconds after being fished up,
 * knocking them back hard (wiki: Frosty).
 */
public class FrostyMob extends WinterSeaCreatureMob {
    private static final int VOLLEY_TICK = 60;
    private static final double VOLLEY_RANGE = 32;
    private static final double SNOWBALL_SPEED = 1.2;
    private static final double KNOCKBACK_HORIZONTAL = 30;
    private static final double KNOCKBACK_VERTICAL = 12;

    public FrostyMob(@Nullable SkyBlockPlayer angler) {
        super(WinterSeaCreature.FROSTY_THE_SNOWMAN, angler);
    }

    @Override
    protected void onTick(int ticks) {
        if (ticks != VOLLEY_TICK) return;
        ItemStack snowball = ItemStack.of(Material.SNOWBALL);
        for (SkyBlockPlayer player : MythologicalCombat.nearby(this, VOLLEY_RANGE)) {
            MobProjectile.item(this, player, snowball, SNOWBALL_SPEED, hit -> MythologicalCombat.launch(hit,
                    MythologicalCombat.away(this, hit, KNOCKBACK_HORIZONTAL, KNOCKBACK_VERTICAL)));
        }
    }
}
