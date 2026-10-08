package net.swofty.type.skyblockgeneric.item.handlers.ability;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.AbilityDamage;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.BuildersWandAbility;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.CrimsonAbilities;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.ForwardTeleport;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.JingleBellsAbility;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.ShadowWarpAbility;
import net.swofty.type.skyblockgeneric.item.handlers.ability.abilities.WitherShieldEffect;
import net.swofty.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityRegistry {
    private static final Map<String, RegisteredAbility> REGISTERED_ABILITIES = new HashMap<>();

    static {
        register(new RegisteredAbility(
                "DRAGON_RAGE",
                "Dragon Rage",
                (_, _) -> "<7>All Monsters in front of you take <a>12,000 <7>damage. Hit monsters take large knockback.",
                RegisteredAbility.AbilityActivation.RIGHT_CLICK,
                0,
                new RegisteredAbility.AbilityManaCost(100),
                (player, _, _, _) -> {
                    for (Entity entity : player.getInstance().getNearbyEntities(player.getPosition(), 6)) {
                        if (!(entity instanceof SkyBlockMob mob)) continue;
                        mob.damage(new Damage(DamageType.MAGIC, player, player, player.getPosition(), ElectionPerks.spellDamage(12000)));
                        mob.setVelocity(mob.getPosition().sub(player.getPosition()).asVec().normalize().mul(14).withY(7));
                    }
                    return true;
                }
        ));

        register(new RegisteredAbility(
                "PARLEY",
                "Parley",
                (_, _) -> "<7>Channel your inner Jerry.",
                RegisteredAbility.AbilityActivation.RIGHT_CLICK,
                20,
                new RegisteredAbility.AbilityManaCost(10),
                (player, _, _, _) -> {
                    player.sendMessage("<a>Jerry!");
                    return true;
                }
        ));

        register(new RegisteredAbility(
            "WITHER_IMPACT",
            "Wither Impact",
            (player, item) -> "<7>Teleports <a>10 Blocks <7>ahead of you dealing <c>10,000 <7>damage to nearby enemies within a <a>6 block <7>radius. Also reduces your damage taken by <a>10% <7>and grants an absorption shield for <e>5 <7>seconds.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            3, // Intentionally near-instant - real Hypixel's known "Hyperion jump" mobility mechanic
            new RegisteredAbility.AbilityManaCost(300),
            (player, _, _, _) -> {
                Pos landed = ForwardTeleport.teleport(player, 10);
                AbilityDamage.hitArea(player, landed, 6, "Implosion", 10000, 0.3);
                WitherShieldEffect.apply(player);
                return true;
            }
        ));

        register(new RegisteredAbility(
            "IMPLOSION",
            "Implosion",
            (_, _) -> "<7>Deals <c>10,000 <7>damage to all mobs in a <a>6 block <7>radius.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            200,
            new RegisteredAbility.AbilityManaCost(300),
            (player, _, _, _) -> {
                AbilityDamage.hitArea(player, player.getPosition(), 6, "Implosion", 10000, 0.3);
                return true;
            }
        ));

        register(new RegisteredAbility(
            "JINGLE_BELLS",
            "Jingle Bells",
            (_, _) -> "<7>Angers all monsters in a <a>10 block <7>range, consuming <a>50%<7> of your max mana and causing them to run towards you.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            20,
            new RegisteredAbility.NoAbilityCost(),
            (player, _, _, _) -> JingleBellsAbility.ring(player)
        ));

        register(new ShadowWarpAbility());

        register(new RegisteredAbility(
            "WITHER_SHIELD",
            "Wither Shield",
            (_, _) -> "<7>Reduces damage taken by <a>10% <7>for <e>5 <7>seconds. Also grants an absorption shield.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            200,
            new RegisteredAbility.AbilityManaCost(150),
            (player, _, _, _) -> {
                WitherShieldEffect.apply(player);
                return true;
            }
        ));

        register(new RegisteredAbility(
            "INSTANT_TRANSMISSION",
            "Instant Transmission",
            (player, item) -> "<7>Teleports <a>8 Blocks <7>ahead of you and gain <a>+50 <f>Speed for <a>3 seconds<7>.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            5,
            new RegisteredAbility.AbilityManaCost(50),
            (player, _, _, _) -> {
                ForwardTeleport.teleport(player, 8);
                ItemStatistics speedStats = ItemStatistics.builder().withBase(ItemStatistic.SPEED, 50.0).build();
                TemporaryStatistic speedBoost = TemporaryStatistic.builder().withStatistics(speedStats).withExpirationInMs(3000).withDisplayName("Instant Transmission").build();
                player.getStatistics().boostStatistic(speedBoost);
                return true;
            }
        ));

        register(new RegisteredAbility(
            "ETHER_TRANSMISSION",
            "Ether Transmission",
            (_, _) -> "<7>Teleport to your targeted block up to <a>57 <7>blocks away.",
            RegisteredAbility.AbilityActivation.SNEAK_RIGHT_CLICK,
            5,
            new RegisteredAbility.AbilityManaSoulflowCost(180, 1),
            (player, _, _, _) -> {
                // TODO: should not be able to go through 1 block holes.
                Point targetedBlock = player.getTargetBlockPosition(57);
                if (targetedBlock == null) return false;
                BlockVec tpPos = targetedBlock.asBlockVec();
                if (!player.getInstance().getBlock(tpPos.add(0, 1, 0)).air() || !player.getInstance().getBlock(tpPos.add(0, 2, 0)).air())
                    return false;
                player.teleport(tpPos.add(0, 1, 0).asPos(player.getPosition().yaw(), player.getPosition().pitch()));
                return true;
            }
        ));

        register(new RegisteredAbility(
            "TRUE_DWARTH",
            "True Dwarth",
            (_, _) -> "<7>Shows the way towards the nearest <6>Emissary <7>while in the <2>Dwarven Mines<7>.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            20 * 3,
            new RegisteredAbility.NoAbilityCost(),
            (player, _, _, _) -> {
                player.notImplemented();
                return false;
            }
        ));

        registerEcho("ECHO", 3);
        registerEcho("ECHO_ARCHAIC", 2);
        registerEcho("ECHO_DEIFIC", 1);

        register(new RegisteredAbility(
            "SPEED_BOOST",
            "Speed Boost",
            (_, _) -> "<7>Grants <7>+100 Speed",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            20 * 5,
            new RegisteredAbility.AbilityManaCost(50),
            (player, _, _, _) -> {
                ItemStatistics speedStats = ItemStatistics.builder().withBase(ItemStatistic.SPEED, 100.0).build();
                TemporaryStatistic speedBoost = TemporaryStatistic.builder().withStatistics(speedStats).withExpirationInMs(30000).withDisplayName("Speed Boost").build();
                player.getStatistics().boostStatistic(speedBoost);
                return true;
            }
        ));

        register(new RegisteredPassiveAbility(
            "STORED_POTENTIAL",
            "Stored Potential",
            (_, item) -> {
                int blocksMined = item.getAttributeHandler().getStoredPotential();
                int miningSpeed = Math.min((blocksMined / 100) * 10, 250);
                return """
                    <7>Grants <6>+10⸕ Mining Speed <7>for every
                    100 blocks mined.
                    <8>(Max +250⸕ Mining Speed)

                    <7>Blocks Mined: <a>%d
                    <7>Current Bonus: <a>%d""".formatted(blocksMined, miningSpeed);
            },
            List.of(new RegisteredPassiveAbility.Action<>(
                CustomBlockBreakEvent.class,
                EventNodes.CUSTOM,
                event -> {
                    SkyBlockPlayer player = event.getPlayer();
                    player.updateItemInSlot(player.getHeldSlot(), (item) -> {
                        item.getAttributeHandler().setStoredPotential(item.getAttributeHandler().getStoredPotential() + 1);
                        item.getAttributeHandler().setExtraDynamicStatistics(
                            ItemStatistics.builder().withAdditive(
                                ItemStatistic.MINING_SPEED, (double) Math.min((item.getAttributeHandler().getStoredPotential() / 100) * 10, 250)
                            ).build()
                        );
                    });
                },
                RegisteredPassiveAbility.Action.createDefaultCondition("STORED_POTENTIAL")
            ))
        ));

//		register(new RegisteredPassiveAbility(
//				"BEJEWELED_BLADE",
//				"Bejeweled Blade",
//				"<7>Deals <a>+150% <7>damage to mobs on <b>Mining Islands.",
//				List.of(new RegisteredPassiveAbility.Action<>(
//
//				))
//		));

        register(new BuildersWandAbility());
        CrimsonAbilities.register();
    }

    public static void register(RegisteredAbility ability) {
        REGISTERED_ABILITIES.put(ability.getId(), ability);
    }

    public static RegisteredAbility getAbility(String id) {
        return REGISTERED_ABILITIES.get(id);
    }

    public static Map<String, RegisteredAbility> getRegisteredAbilities() {
        return REGISTERED_ABILITIES;
    }

    private static void registerEcho(String id, int cooldownSeconds) {
        register(new RegisteredAbility(
            id,
            "Echo",
            (_, _) -> "<7>Show the way to the next or nearby Griffin burrow.",
            RegisteredAbility.AbilityActivation.RIGHT_CLICK,
            20 * cooldownSeconds,
            new RegisteredAbility.NoAbilityCost(),
            (player, _, _, _) -> GriffinBurrowService.INSTANCE.echo(player)
        ));
    }
}
