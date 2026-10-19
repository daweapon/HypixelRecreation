package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class DragonEssencePerks {
    public static final String ONE_PUNCH = "flat_damage_vs_ender";
    public static final String RECHARGE = "mana_after_ender_kill";
    public static final String RAGEBORN = "fero_vs_dragons";
    public static final String ZEALUCK = "inc_zealots_odds";
    public static final String DRAGON_PIPER = "increased_sup_chances";

    private static final Map<SkyBlockMob, Set<UUID>> STRUCK = Collections.synchronizedMap(new WeakHashMap<>());

    private DragonEssencePerks() {
    }

    public static double value(SkyBlockPlayer player, String perkId) {
        return EssenceShops.shop(EssenceType.DRAGON)
                .flatMap(shop -> shop.perks().stream().filter(perk -> perk.id().equals(perkId)).findFirst())
                .map(perk -> EssencePerks.value(player, perk))
                .orElse(0D);
    }

    public static boolean isEnderfolk(SkyBlockMob mob) {
        if (!(mob instanceof BestiaryMob bestiary) || !mob.getMobTypes().contains(MobType.ENDER)) return false;
        String id = bestiary.getMobID();
        return id.startsWith("ENDERMAN") || id.startsWith("ENDERMITE") || id.startsWith("NEST_ENDERMITE");
    }

    public static boolean isDragon(SkyBlockMob mob) {
        return mob instanceof BestiaryMob bestiary && bestiary.getMobID().startsWith("ENDER_DRAGON_");
    }

    public static double firstStrikeBonus(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!isEnderfolk(mob)) return 0;
        double bonus = value(player, ONE_PUNCH);
        if (bonus <= 0) return 0;
        return STRUCK.computeIfAbsent(mob, ignored -> new HashSet<>()).add(player.getUuid()) ? bonus : 0;
    }

    public static double dragonFerocity(SkyBlockPlayer player, SkyBlockMob mob) {
        return isDragon(mob) ? value(player, RAGEBORN) : 0;
    }

    public static void onEnderKill(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!isEnderfolk(mob)) return;
        double mana = value(player, RECHARGE);
        if (mana > 0) player.setMana((float) Math.min(player.getMaxMana(), player.getMana() + mana));
    }
}
