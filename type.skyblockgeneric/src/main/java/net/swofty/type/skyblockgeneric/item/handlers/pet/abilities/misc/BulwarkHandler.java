package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Wisps' Bulwark: each Blaze the pet is out for adds to a kill counter stored on the pet, and the counter's tier
 * gives Defense and True Defense against Blazes and the Inferno Demonlord's demons.
 */
public final class BulwarkHandler implements PetAbilityHandler {
    public static boolean isTarget(SkyBlockMob mob) {
        if (mob.getEntityType() == EntityType.BLAZE) return true;
        return mob instanceof SlayerMinionMob && mob.getMobTypes().contains(MobType.INFERNAL);
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (event instanceof PetEvent.Kill kill) {
            if (kill.mob().getEntityType() != EntityType.BLAZE) return;
            SkyBlockItem pet = context.pet();
            pet.getAttributeHandler().setPetBulwarkKills(pet.getAttributeHandler().getPetBulwarkKills() + 1);
        } else if (event instanceof PetEvent.DamagedByMob hit && isTarget(hit.mob())) {
            int kills = context.pet().getAttributeHandler().getPetBulwarkKills();
            if (kills <= 0) return;
            double defense = hit.player().getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE);
            hit.damage(PetPerkMath.bulwarkDamage(hit.damage(), defense, PetPerkMath.bulwarkDefense(kills),
                    PetPerkMath.bulwarkTrueDefense(kills)));
        }
    }

    @Override
    public @Nullable List<String> description(PetAbilityContext context) {
        return lines(context.pet().getAttributeHandler().getPetBulwarkKills());
    }

    public static List<String> lines(int kills) {
        String bonus = "<7>Bonus: <a>+" + PetPerkMath.bulwarkDefense(kills) + "<glyph:stat_defense> & <f>+"
                + PetPerkMath.bulwarkTrueDefense(kills) + "<glyph:stat_true_defense>";
        int next = PetPerkMath.bulwarkNextKills(kills);
        if (next < 0) return List.of("<7>Kill Blazes to gain defense against", "<7>them and demons.", bonus);
        return List.of("<7>Kill Blazes to gain defense against", "<7>them and demons.", bonus,
                "<7>Next Upgrade: <a>+" + PetPerkMath.bulwarkNextDefense(kills) + "<glyph:stat_defense> & <f>+"
                        + PetPerkMath.bulwarkNextTrueDefense(kills) + "<glyph:stat_true_defense> <8>(<a>" + kills
                        + "<7>/<c>" + next + "<8>)");
    }
}
