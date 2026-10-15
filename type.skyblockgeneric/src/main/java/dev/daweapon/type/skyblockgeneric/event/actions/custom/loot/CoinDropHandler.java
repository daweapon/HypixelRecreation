package dev.daweapon.type.skyblockgeneric.event.actions.custom.loot;

import dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentScavenger;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalCoins;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Awards coin drops for mob kills, boosted by the Scavenger enchantment and bestiary tier
public class CoinDropHandler implements HypixelEventClass {
    BestiaryData bestiaryData = new BestiaryData();

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {

        SkyBlockPlayer player = event.getPlayer();
        SkyBlockMob mob = event.getKilledMob();

        if (mob == null) return;

        int coinsDropped;

        //Base Mob Coins (additive)
        int baseCoins = mob.getOtherLoot().getCoinAmount();

        //Scavenger Enchant (additive)
        EnchantmentScavenger enchantmentScavenger = new EnchantmentScavenger();
        int scavengedCoins = enchantmentScavenger.getScavengedCoins(event);

        //Bestiary Boost (multiplicative)
        float bestiaryBoost = 1;
        if (mob instanceof BestiaryMob bestiaryMob) {
            int tier = bestiaryData.getCurrentBestiaryTier(bestiaryMob, player.getBestiaryData().getAmount(bestiaryMob));
            bestiaryBoost += (float) bestiaryData.getTotalExtraCoinPercentage(tier) / 100;
        }

        int accessoryCoins = AccessoryEconomyHooks.mobLevelCoins(player, mob);
        coinsDropped = Math.round((baseCoins + scavengedCoins + accessoryCoins) * bestiaryBoost);
        if (mob instanceof MythologicalCreatureMob) {
            coinsDropped = (int) MythologicalCoins.fromMob(player, coinsDropped);
        }

        player.addCoins(coinsDropped);
    }
}
