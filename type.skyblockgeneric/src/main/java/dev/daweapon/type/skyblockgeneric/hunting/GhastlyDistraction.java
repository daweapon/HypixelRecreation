package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerMinibossMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GhastlyDistraction {
    private static final AttributeId ID = AttributeId.parse("E43");
    private static final Map<UUID, Long> PACIFIED_UNTIL = new ConcurrentHashMap<>();

    private GhastlyDistraction() {
    }

    public static boolean isOpalGemstone(Material material, RegionType region) {
        return region == RegionType.SMOLDERING_TOMB
                && (material == Material.WHITE_STAINED_GLASS || material == Material.WHITE_STAINED_GLASS_PANE);
    }

    public static void onMined(SkyBlockPlayer player, Material material, RegionType region) {
        if (!isOpalGemstone(material, region)) return;
        double seconds = AttributeEffectService.value(player.getHuntingData(), ID);
        if (seconds <= 0) return;
        PACIFIED_UNTIL.put(player.getUuid(), System.currentTimeMillis() + Math.round(seconds * 1000));
    }

    public static boolean pacifies(long until, long now, boolean exempt) {
        return !exempt && until > now;
    }

    public static boolean isPacified(SkyBlockPlayer player, SkyBlockMob mob) {
        Long until = PACIFIED_UNTIL.get(player.getUuid());
        if (until == null) return false;
        return pacifies(until, System.currentTimeMillis(), mob instanceof SlayerBossMob || mob instanceof SlayerMinibossMob);
    }

    public static void onAttack(SkyBlockPlayer player) {
        PACIFIED_UNTIL.remove(player.getUuid());
    }
}
