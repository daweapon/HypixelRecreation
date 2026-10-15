package dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.ClubbedTailHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.ContrabandHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.FerociousRoarHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.HemorrhageHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TrampleHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CoinsPerKillHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatWisdomTowardsHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.DragonsGreedHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.FourthFlareHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.GoldsPowerHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.HeldItemStatsHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.HorseStampedeHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.LastStandHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.LegendaryTreasureHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.MobTypeDamageReductionHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PackLeaderHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.RekindleHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.RicochetHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.ShiningScalesHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.SkeletonComboHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.SymbiosisHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TimedBuffHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TossHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TurtleShellHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TurtleTacticsHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TuskLuckHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.TyrantHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.WebBattlefieldHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.WebWeaverHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.ArmorBoostHandler;

import java.util.Set;

/**
 * Handlers for the Combat pets added in the Pets update.
 */
final class CombatPetAbilities implements PetAbilityRegistry.Provider {
    @Override
    public void register(PetAbilityRegistry.Registrar registrar) {
        registrar.register("TRAMPLE", new TrampleHandler());
        registrar.register("TYRANNOSAURUS_FEROCIOUS_ROAR", new FerociousRoarHandler());
        registrar.register("TIGER_HEMORRHAGE", new HemorrhageHandler());
        registrar.register("PRECURSOR_DRONE_CONTRABAND", new ContrabandHandler());
        registrar.register("BLAZE_BLING_ARMOR", new ArmorBoostHandler(Set.of(ItemType.BLAZE_HELMET,
                ItemType.BLAZE_CHESTPLATE, ItemType.BLAZE_LEGGINGS, ItemType.BLAZE_BOOTS)));
        registrar.register("COMBAT_WISDOM_TOWARDS", new CombatWisdomTowardsHandler());
        registrar.register("COINS_PER_KILL", new CoinsPerKillHandler());
        registrar.register("ANKYLOSAURUS_CLUBBED_TAIL", new ClubbedTailHandler());
        registrar.register("CROW_CAMOUFLAGE", new TimedBuffHandler(ItemStatistic.DEFENSE));
        registrar.register("ENDERMAN_ENDERIAN", new MobTypeDamageReductionHandler(MobType.ENDER));
        registrar.register("ENDERMAN_TELEPORT_SAVVY", new TimedBuffHandler(ItemStatistic.DAMAGE));
        registrar.register("ENDER_DRAGON_ONE_WITH_THE_DRAGONS",
                new HeldItemStatsHandler(ItemType.ASPECT_OF_THE_DRAGON, ItemStatistic.DAMAGE, ItemStatistic.STRENGTH));
        registrar.register("MAGMA_CUBE_HOT_EMBER", new ArmorBoostHandler(Set.of(ItemType.REKINDLED_EMBER_HELMET,
                ItemType.REKINDLED_EMBER_CHESTPLATE, ItemType.REKINDLED_EMBER_LEGGINGS, ItemType.REKINDLED_EMBER_BOOTS)));
        registrar.register("GOLDEN_DRAGON_GOLDS_POWER", new GoldsPowerHandler());
        registrar.register("GOLDEN_DRAGON_SHINING_SCALES", new ShiningScalesHandler());
        registrar.register("GOLDEN_DRAGON_DRAGONS_GREED", new DragonsGreedHandler());
        registrar.register("GOLDEN_DRAGON_LEGENDARY_TREASURE", new LegendaryTreasureHandler());
        registrar.register("GOLDEN_DRAGON_SYMBIOSIS", new SymbiosisHandler());
        registrar.register("GOLEM_LAST_STAND", new LastStandHandler());
        registrar.register("GOLEM_RICOCHET", new RicochetHandler());
        registrar.register("GOLEM_TOSS", new TossHandler());
        registrar.register("HORSE_STAMPEDE", new HorseStampedeHandler());
        registrar.register("JERRY_ASPECT_OF_THE_JERRY", new HeldItemStatsHandler(ItemType.ASPECT_OF_THE_JERRY, ItemStatistic.DAMAGE));
        registrar.register("MAMMOTH_TUSK_LUCK", new TuskLuckHandler());
        registrar.register("PHOENIX_REKINDLE", new RekindleHandler());
        registrar.register("PHOENIX_FOURTH_FLARE", new FourthFlareHandler());
        registrar.register("PIGMAN_PORK_MASTER", new HeldItemStatsHandler(ItemType.PIGMAN_SWORD, ItemStatistic.DAMAGE, ItemStatistic.STRENGTH));
        registrar.register("SKELETON_COMBO", new SkeletonComboHandler());
        registrar.register("SPIDER_WEB_WEAVER", new WebWeaverHandler());
        registrar.register("WEB_BATTLEFIELD", new WebBattlefieldHandler());
        registrar.register("TREX_TYRANT", new TyrantHandler());
        registrar.register("TURTLE_TACTICS", new TurtleTacticsHandler());
        registrar.register("TURTLE_SHELL", new TurtleShellHandler());
        registrar.register("WOLF_PACK_LEADER", new PackLeaderHandler());
    }
}
