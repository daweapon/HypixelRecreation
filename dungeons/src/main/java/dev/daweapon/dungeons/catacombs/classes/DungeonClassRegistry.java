package dev.daweapon.dungeons.catacombs.classes;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The five dungeon classes with their passives, Dungeon Orb abilities and ghost abilities. Names, cooldowns and
 * descriptions follow the wiki's class pages and the in-game tooltips transcribed in its Inventory slot module.
 */
public record DungeonClassRegistry(Map<DungeonClassType, DungeonClassDefinition> definitions) {
    public DungeonClassRegistry {
        definitions = Map.copyOf(definitions);
    }

    public DungeonClassDefinition definition(DungeonClassType type) {
        DungeonClassDefinition definition = definitions.get(type);
        if (definition == null) {
            throw new IllegalArgumentException("No dungeon class registered for " + type);
        }
        return definition;
    }

    public DungeonClassAbility ability(DungeonClassType type, DungeonClassAbilityType abilityType, int index) {
        return definition(type).abilities(abilityType).get(index);
    }

    public static DungeonClassRegistry defaults() {
        Map<DungeonClassType, DungeonClassDefinition> definitions = new EnumMap<>(DungeonClassType.class);
        definitions.put(DungeonClassType.HEALER, new DungeonClassDefinition(DungeonClassType.HEALER,
                "Healing and support",
                abilities(
                        passive("RENEW", "Renew", "Your healing abilities heal the target for 50% additional HP"),
                        passive("HEALING_AURA", "Healing Aura", "Passively heals all living teammates within 8 blocks for 1% HP per second"),
                        passive("REVIVE", "Revive", "A fairy follows you and revives dead teammates"),
                        passive("ORBIES", "Orbies", "Enemies you help kill can drop a healing orb"),
                        passive("SOUL_TETHER", "Soul Tether", "Tethered teammates are healed while near you"),
                        passive("OVERHEAL", "Overheal", "Healing a full-health player grants a shield"),
                        orb("HEALING_CIRCLE", "Healing Circle", 2, "Launches a healing orb that creates a healing circle where it lands"),
                        ultimate("WISH", "Wish", 120, "Heals everyone in your group to full health and grants them a shield"),
                        ghost("HEALING_POTION", "Healing Potion", 20, "Throw a potion which heals all teammates in a 10 block radius"),
                        ghost("REVIVE_SELF", "Revive", 50, "Revive yourself after 50 seconds"))));
        definitions.put(DungeonClassType.MAGE, new DungeonClassDefinition(DungeonClassType.MAGE,
                "Magic and area damage",
                abilities(
                        passive("MAGE_STAFF", "Mage Staff", "All your melee weapon attacks become ranged"),
                        passive("EFFICIENT_SPELLS", "Efficient Spells", "All abilities have a 25% shorter cooldown"),
                        orb("GUIDED_SHEEP", "Guided Sheep", 30, "Shoots a Guided Sheep, dealing damage based on your Mage level"),
                        ultimate("THUNDERSTORM", "Thunderstorm", 500, "Strikes enemies in a 10 block radius for 15 seconds"),
                        ghost("INSTANT_WALL", "Instant Wall", 20, "Create a 5x3 wall at the block you are looking at for 10 seconds"),
                        ghost("FIREBALL", "Fireball", 15, "Shoots a fireball, dealing damage based on your Mage level"))));
        definitions.put(DungeonClassType.BERSERK, new DungeonClassDefinition(DungeonClassType.BERSERK,
                "Melee damage",
                abilities(
                        passive("BLOODLUST", "Bloodlust", "Every hit heals 3% of your missing health"),
                        passive("LUST_FOR_BLOOD", "Lust for Blood", "Every hit increases the damage you deal to that enemy"),
                        passive("INDOMITABLE", "Indomitable", "Gain a percentage of your Strength as Defense"),
                        passive("WEAPON_MASTER", "Weapon Master", "Melee weapons hit up to 5 enemies in a frontal cone"),
                        orb("THROWING_AXE", "Throwing Axe", 10, "Throw an Axe, dealing your highest hit in the last minute"),
                        ultimate("RAGNAROK", "Ragnarok", 60, "Grants Attack Speed, Speed and 1.5x melee damage for 15 seconds"),
                        ghost("BUFF_POTION", "Buff Potion", 20, "Throw a potion which gives teammates in a 10 block radius +30 Strength"),
                        ghost("GHOST_AXE", "Ghost Axe", 10, "Throw an Axe, dealing damage based on your Berserk level"))));
        definitions.put(DungeonClassType.ARCHER, new DungeonClassDefinition(DungeonClassType.ARCHER,
                "Ranged damage",
                abilities(
                        passive("DOUBLESHOT", "Doubleshot", "50% chance to shoot a second arrow"),
                        passive("BONE_PLATING", "Bone Plating", "Reduces your next incoming hit"),
                        passive("BOUNCY_ARROWS", "Bouncy Arrows", "Arrows can bounce to an additional target"),
                        orb("EXPLOSIVE_SHOT", "Explosive Shot", 40, "Shoots 3 arrows that explode in a 4 block radius"),
                        ultimate("RAPID_FIRE", "Rapid Fire", 100, "Shoots 5 arrows per second for 4 seconds"),
                        ghost("STUN_BOW", "Stun Bow", 10, "Temporarily stuns any non-boss dungeon monster when shot"),
                        ghost("HEALING_BOW", "Healing Bow", 10, "Shoot a teammate to heal them and grant them +50 Strength"))));
        definitions.put(DungeonClassType.TANK, new DungeonClassDefinition(DungeonClassType.TANK,
                "Effective health and keeping mobs off teammates",
                abilities(
                        passive("PROTECTIVE_BARRIER", "Protective Barrier", "Gain permanent +25% Defense"),
                        passive("TAUNT", "Taunt", "Mobs are more likely to target you"),
                        passive("DIVERSION", "Diversion", "Divert 80% of the damage taken by teammates within 30 blocks"),
                        passive("DEFENSIVE_STANCE", "Defensive Stance", "Immunity to knockback from mobs"),
                        orb("SEISMIC_WAVE", "Seismic Wave", 15, "A Seismic Wave travels in a straight line, damaging enemies"),
                        ultimate("CASTLE_OF_STONE", "Castle of Stone", 150, "Reduces the damage you take by 70% for 20 seconds"),
                        ghost("STUN_POTION", "Stun Potion", 20, "Throw a potion which stuns all monsters in a 10 block radius"),
                        ghost("ABSORPTION_POTION", "Absorption Potion", 20, "Throw a potion which gives teammates absorption"))));
        return new DungeonClassRegistry(definitions);
    }

    private static DungeonClassAbility passive(String id, String displayName, String description) {
        return new DungeonClassAbility(id, displayName, DungeonClassAbilityType.PASSIVE, 0, description);
    }

    private static DungeonClassAbility orb(String id, String displayName, int cooldownSeconds, String description) {
        return new DungeonClassAbility(id, displayName, DungeonClassAbilityType.ORB, cooldownSeconds, description);
    }

    private static DungeonClassAbility ultimate(String id, String displayName, int cooldownSeconds, String description) {
        return new DungeonClassAbility(id, displayName, DungeonClassAbilityType.ULTIMATE, cooldownSeconds, description);
    }

    private static DungeonClassAbility ghost(String id, String displayName, int cooldownSeconds, String description) {
        return new DungeonClassAbility(id, displayName, DungeonClassAbilityType.GHOST, cooldownSeconds, description);
    }

    private static List<DungeonClassAbility> abilities(DungeonClassAbility... abilities) {
        return List.of(abilities);
    }
}
