package dev.daweapon.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import org.jetbrains.annotations.NotNull;

// Fired when a player kills a SkyBlock mob; updates bestiary progress and dispatches kill enchantments/pet effects
public class PlayerKilledSkyBlockMobEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    @Getter
    private final SkyBlockMob killedMob;

    public PlayerKilledSkyBlockMobEvent(SkyBlockPlayer player, SkyBlockMob killedMob) {
        this.player = player;
        this.killedMob = killedMob;

        if (killedMob instanceof BestiaryMob bestiaryMob && bestiaryMob.countsForBestiary()) {
            DatapointBestiary.PlayerBestiary playerBestiary = player.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
            playerBestiary.increase(bestiaryMob, 1);
        }

        SkyBlockItem mainHandItem = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);

        for (SkyBlockEnchantment enchantment : mainHandItem.getAttributeHandler().getEnchantments().toList()) {
            if (enchantment.type().getEnch() instanceof KillEventEnchant killEventEnchant) {
                killEventEnchant.onMobKilled(player, killedMob,
                        ReforgeLoadout.effectiveEnchantLevel(player, enchantment.type(), enchantment.level()));
            }
        }

        double reaperXp = dev.daweapon.type.skyblockgeneric.slayer.zombie.ZombieSlayerGear.bonusCombatXp(player, killedMob,
                killedMob.getOtherLoot() == null ? 0 : killedMob.getOtherLoot().getSkillXPAmount());
        if (reaperXp > 0) {
            var skills = player.getSkills();
            skills.setRaw(player, dev.daweapon.type.skyblockgeneric.skill.SkillCategories.COMBAT,
                    skills.getRaw(dev.daweapon.type.skyblockgeneric.skill.SkillCategories.COMBAT) + reaperXp);
        }

        // Handle kill event pets
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        player.getPetData().dispatch(new PetEvent.Kill(player, pet, killedMob));
    }

    @Override
    public @NotNull SkyBlockPlayer getPlayer() {
        return player;
    }
}
