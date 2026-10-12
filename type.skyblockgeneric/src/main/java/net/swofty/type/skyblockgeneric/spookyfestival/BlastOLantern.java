package net.swofty.type.skyblockgeneric.spookyfestival;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.type.skyblockgeneric.consumable.TimedConsumables;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.gifts.SnowballProjectile;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.AbilityDamage;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.DamageIndicator;

public final class BlastOLantern {
    public static final String ID = "BLAST_O_LANTERN";
    public static final double SPOOKY_DAMAGE = 2;
    public static final double OTHER_DAMAGE = 3000;
    public static final double INTELLIGENCE_SCALING = 0.2;
    private static final double THROW_SPEED = 1.4;

    private BlastOLantern() {
    }

    public static double baseDamage(boolean spooky) {
        return spooky ? SPOOKY_DAMAGE : OTHER_DAMAGE;
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            TimedConsumables.consumeHeld(player, item);
            Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
            SnowballProjectile.shoot(player, eye, player.getPosition().direction(), THROW_SPEED,
                    mob -> explode(player, mob));
        }).build());
    }

    private static void explode(SkyBlockPlayer player, SkyBlockMob mob) {
        boolean spooky = mob.getMobTypes().contains(MobType.SPOOKY);
        float damage = (float) AbilityDamage.of(player, baseDamage(spooky), INTELLIGENCE_SCALING);
        mob.damage(new Damage(DamageType.MAGIC, player, player, mob.getPosition(), damage));
        new DamageIndicator().damage(damage).pos(mob.getPosition()).critical(false).display(player.getInstance());
    }
}
