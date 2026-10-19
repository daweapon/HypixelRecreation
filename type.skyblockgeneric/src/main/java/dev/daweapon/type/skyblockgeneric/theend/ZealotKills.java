package dev.daweapon.type.skyblockgeneric.theend;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobSpecialZealot;
import dev.daweapon.type.skyblockgeneric.essence.CataloguePerk;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShops;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public final class ZealotKills {
    private static final AtomicLong TOTAL = new AtomicLong();
    private static final String ZEALUCK_PERK = "inc_zealots_odds";

    private ZealotKills() {
    }

    public static AtomicLong total() {
        return TOTAL;
    }

    public static void onKill(SkyBlockPlayer player, Instance instance, Pos position, boolean bruiser) {
        TOTAL.incrementAndGet();
        if (player.getSkyblockDataHandler() == null || instance == null) return;
        int meter = ZealotPity.increment(player);
        double chance = SpecialZealotOdds.chancePercent(bruiser, petBonusPercent(player), zealuckPercent(player));
        boolean rolled = LootRoll.roll(player.getUuid(), ItemType.SUMMONING_EYE, chance);
        if (!rolled && !SpecialZealotOdds.pityReached(meter)) return;
        ZealotPity.reset(player);
        new MobSpecialZealot().summonedBy(player).setInstance(instance, position);
        announce(player);
    }

    static double petBonusPercent(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.ENDERMAN_PET, "Zealot Madness", 0);
    }

    static double zealuckPercent(SkyBlockPlayer player) {
        return EssenceShops.shop(EssenceType.DRAGON)
                .flatMap(shop -> shop.perks().stream().filter(ZealotKills::isZealuck).findFirst())
                .map(perk -> EssencePerks.value(player, perk))
                .orElse(0D);
    }

    private static boolean isZealuck(CataloguePerk perk) {
        return ZEALUCK_PERK.equals(perk.id());
    }

    private static void announce(SkyBlockPlayer player) {
        player.sendMessage(Text.of("<5><l>A special Zealot has spawned nearby!"));
        player.playSound(Sound.sound().type(Key.key("entity.wither.spawn")).volume(1f).build());
        player.showTitle(Text.of("<5><l>SPECIAL ZEALOT"), Text.of(""),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500)));
    }
}
