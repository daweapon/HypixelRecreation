package dev.daweapon.type.skyblockgeneric.farming.chum;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbilities;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ChumDrops {
    private ChumDrops() {
    }

    public static void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!(mob instanceof SeaCreatureMob) || mob.getInstance() == null) return;
        int amount = ChumBuckets.bucketsNear(mob.getInstance(), mob.getPosition());
        amount += stiffBonus(player);
        if (amount <= 0) return;
        player.addAndUpdateItem(ItemType.CHUM, amount);
        player.sendMessage("<a>+{} Chum", amount);
    }

    private static int stiffBonus(SkyBlockPlayer player) {
        var reforge = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getReforge();
        if (reforge == null) return 0;
        return ReforgeAbilities.of(reforge)
                .filter(ability -> ability instanceof ReforgeAbility.SeaCreatureChum)
                .map(ability -> ((ReforgeAbility.SeaCreatureChum) ability).extraChum())
                .orElse(0);
    }
}
