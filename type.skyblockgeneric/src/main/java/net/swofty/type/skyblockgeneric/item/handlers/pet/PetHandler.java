package net.swofty.type.skyblockgeneric.item.handlers.pet;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.gifts.ReindeerPet;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.*;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;
import java.util.Set;

public enum PetHandler {
    BEE(List.of(
            at(new HiveAbility(), Rarity.COMMON),
            at(new BusyBuzzBuzzAbility(), Rarity.RARE),
            at(new HoneyHarvesterAbility(), Rarity.LEGENDARY),
            at(new PoweredByPollenAbility(), Rarity.MYTHIC)
    )),
    GRANDMA_WOLF(List.of(
            at(new KillComboAbility(), Rarity.COMMON)
    )),
    CHICKEN(List.of(
            at(new FreeRangeAbility(), Rarity.COMMON),
            at(new EggstraLootAbility(), Rarity.RARE),
            at(new LightFeetAbility(), Rarity.LEGENDARY)
    )),
    LION(List.of(
            at(ScaledPetAbility.stats("Primal Force", scaling(3, 5, 10, 15, 20),
                    List.of(ItemStatistic.DAMAGE, ItemStatistic.STRENGTH),
                    "<7>Adds <c>+{0} ❁ Damage <7>and <c>+{0} ❁ Strength", "<7>to your weapons."), Rarity.COMMON),
            at(ScaledPetAbility.notActive("First Pounce", scaling(0, 0, 75, 100, 100),
                    "<7>First Strike, Triple-Strike, and", "<7>Combo are <a>{0}% <7>more effective."), Rarity.RARE),
            at(ScaledPetAbility.notActive("King of the Jungle", scaling(0, 0, 0, 0, 150),
                    "<7>Deal <c>+{0}% ❁ Damage <7>against mobs", "<7>that have attacked you."), Rarity.LEGENDARY)
    )),
    MONKEY(List.of(
            at(ScaledPetAbility.stat("Treeborn", scaling(40, 50, 50, 60, 60), ItemStatistic.FORAGING_FORTUNE,
                    "<7>Grants <6>+{0} ☘ Foraging Fortune<7>, which", "<7>increases your chance at double logs."), Rarity.COMMON),
            at(ScaledPetAbility.statIn(Regions.THE_PARK, "Vine Swing", scaling(0, 0, 75, 100, 100), ItemStatistic.SPEED,
                    "<7>Grants <f>+{0} ✦ Speed <7>while in", "<7>The Park."), Rarity.RARE),
            at(ScaledPetAbility.statIn(Regions.THE_PARK, "Evolved Axes", scaling(0, 0, 0, 0, 10), ItemStatistic.SWEEP,
                    "<7>Grants <2>+{0} ∮ Sweep <7>while in", "<7>The Park."), Rarity.LEGENDARY)
    )),
    ELEPHANT(List.of(
            at(ScaledPetAbility.notActive("Stomp", scaling(10, 15, 15, 20, 20),
                    "<7>Gain <a>{0} ❈ Defense <7>for every", "<f>100 ✦ Speed<7>."), Rarity.COMMON),
            at(ScaledPetAbility.notActive("Walking Fortress", scaling(0, 0, 0.75, 1, 1),
                    "<7>Gain <c>{0} ❤ Health <7>for every", "<a>10 ❈ Defense<7>."), Rarity.RARE),
            at(ScaledPetAbility.stat("Trunk Efficiency", scaling(0, 0, 0, 0, 150), ItemStatistic.FARMING_FORTUNE,
                    "<7>Grants <6>+{0} ☘ Farming Fortune<7>, which", "<7>increases your chance for multiple drops."), Rarity.LEGENDARY)
    )),
    GIRAFFE(List.of(
            at(ScaledPetAbility.stat("Good Heart", scaling(100, 150, 150, 200, 200), ItemStatistic.HEALTH_REGENERATION,
                    "<7>Grants <c>+{0} ❣ Health Regen<7>."), Rarity.COMMON),
            at(ScaledPetAbility.notActive("Higher Ground", scaling(0, 0, 0.15, 0.15, 0.15),
                    "<7>Increases your <9>☠ Crit Damage <7>and <c>❁ Strength", "<7>by <a>{0}% <7>for every 0.1 Swing Range", "<7>over 3 (up to 6)."), Rarity.RARE),
            at(ScaledPetAbility.notActive("Long Neck", scaling(0, 0, 0, 0, 100),
                    "<7>Increases your melee damage by <a>{0}%", "<7>if you are more than 3 blocks away", "<7>from the target."), Rarity.LEGENDARY)
    )),
    BLUE_WHALE(List.of(
            at(ScaledPetAbility.notActive("Ingest", scaling(50, 100, 150, 200, 250),
                    "<7>If you have any absorption active,", "<7>gain <a>+{0}% <7>Damage Reduction."), Rarity.COMMON),
            at(ScaledPetAbility.notActive("Bulk", scaling(0, 0, 1, 1, 1),
                    "<7>Gain <a>{0} ❈ Defense <7>per <c>30/25/20", "<c>Max ❤ Health <7>(Rare/Epic/Legendary)."), Rarity.RARE),
            at(ScaledPetAbility.flatPercentage("Archimedes", 20, ItemStatistic.HEALTH,
                    "<7>Gain <c>+{0}% Max ❤ Health<7>."), Rarity.LEGENDARY)
    )),
    TIGER(List.of(
            at(ScaledPetAbility.percentage("Merciless Swipe", scaling(10, 20, 20, 30, 30), ItemStatistic.FEROCITY,
                    "<7>Gain <c>+{0}% ⫽ Ferocity<7>."), Rarity.COMMON),
            at(ScaledPetAbility.notActive("Hemorrhage", scaling(0, 0, 30, 55, 55),
                    "<7>Melee attacks reduce healing", "<7>by <6>{0}% <7>for <a>10s<7>."), Rarity.RARE),
            at(ScaledPetAbility.notActive("Apex Predator", scaling(0, 0, 0, 0, 100),
                    "<7>Deal <c>+{0}% <7>damage against targets", "<7>with no other mobs within <a>15 <7>blocks."), Rarity.LEGENDARY)
    )),
    GRIFFIN(List.of(
            at(new GameplayPetAbility("Odyssey", scaling(2, 4, 6, 8, 10, 12),
                    "<c>{0} <7>types of <2>✿ Mythological <7>mobs can", "<7>spawn from <e>Griffin Burrows<7>. Their",
                    "<7>stats scale with your Griffin's", "<7>rarity."), Rarity.COMMON),
            at(ScaledPetAbility.percentageWhen(player -> player.getHealth() > player.getMaxHealth() * 0.85,
                    "Sacred Strength", scaling(0, 0, 10, 15, 15, 15), ItemStatistic.STRENGTH,
                    "<7>Gain <c>+{0}% ❁ Strength <7>when above", "<c>85% ❤ Health<7>."), Rarity.RARE),
            at(ScaledPetAbility.notActive("King of Kings", scaling(0, 0, 0, 0, 20, 20),
                    "<7>Grants <b>+{0}✯ Magic Find <7>on <2>✿", "<2>Mythological <7>mobs."), Rarity.LEGENDARY),
            at(new GameplayPetAbility("Ancient Earth", scaling(0, 0, 0, 0, 0, 1),
                    "<7>Grants <d>+1 Tracking <7>on <e>Griffin", "<e>Burrows <7>for each burrow excavated",
                    "<7>in your current chain."), Rarity.MYTHIC)
    )),
    SNOWMAN(List.of(
            at(LeveledPetAbility.notActive("Blizzard", new double[]{8, 0}, new double[]{0.08, 0.1},
                    "<7>Enemies within <a>{0} <7>blocks are slowed", "<7>by <a>25% <7>and deal <a>{1}% <7>less damage."),
                    Rarity.LEGENDARY),
            at(LeveledPetAbility.notActive("Frostbite", new double[]{0}, new double[]{0.1},
                    "<7>Your freezing aura slows enemy", "<7>attacks causing you to take <a>{0}%", "<7>reduced damage"),
                    Rarity.LEGENDARY),
            at(LeveledPetAbility.notActive("Snow Cannon", new double[]{10}, new double[]{0.1},
                    "<7>Shoots a snowball towards an enemy", "<7>when you attack dealing <a>{0}% <7>of",
                    "<7>your last dealt melee damage,", "<7>capped at <f>200,000<7>. <8>(1s cooldown)"),
                    Rarity.LEGENDARY)
    )),
    REINDEER(List.of(
            at(LeveledPetAbility.gameplay("Winter Sprint", new double[0], new double[0],
                    "<7>Gain <d>double <7>pet <a>EXP<7>."), Rarity.LEGENDARY),
            at(LeveledPetAbility.statistics("Infused", new double[]{0},
                    new double[]{ReindeerPet.INFUSED_FISHING_SPEED_PER_LEVEL},
                    (player, values) -> ReindeerPet.infused(values[0]),
                    "<7>Gives <b>+{0} ☂ Fishing Speed <7>and <6>+5", "<6>⛃ Treasure Chance <7>while on <c>Jerry's",
                    "<c>Workshop<7>."), Rarity.LEGENDARY),
            at(LeveledPetAbility.gameplay("Snow Power", new double[]{0}, new double[]{ReindeerPet.SNOW_POWER_PER_LEVEL},
                    "<7>Grants <a>+{0}% <7>bonus gift chance", "<7>during the <c>Gift Attack <7>event."), Rarity.LEGENDARY),
            at(LeveledPetAbility.gameplay("Icy Wind", new double[]{0}, new double[]{ReindeerPet.ICY_WIND_PER_LEVEL},
                    "<7>Grants <a>+{0}% <7>chance of getting", "<7>double <b>Ice Essence<7>."), Rarity.LEGENDARY)
    )),
    // Black Cat and Skeleton Horse (Spooky Festival rewards) - their ability trees are not
    // implemented, so they carry stats only.
    SPOOKY(List.of());

    private final List<AbilityEntry> abilities;

    PetHandler(List<AbilityEntry> abilities) {
        this.abilities = abilities;
    }

    public List<PetAbility> getAbilities(SkyBlockItem item) {
        Rarity rarity = item.getAttributeHandler().getRarity();
        return abilities.stream()
                .filter(e -> rarity.isAtLeast(e.minimumRarity()))
                .map(AbilityEntry::ability)
                .toList();
    }

    public record AbilityEntry(PetAbility ability, Rarity minimumRarity) {
    }

    private static AbilityEntry at(PetAbility ability, Rarity rarity) {
        return new AbilityEntry(ability, rarity);
    }

    private static RarityValue<Double> scaling(double common, double uncommon, double rare, double epic, double legendary) {
        return new RarityValue<>(common, uncommon, rare, epic, legendary, legendary, 0.0);
    }

    private static RarityValue<Double> scaling(double common, double uncommon, double rare, double epic, double legendary,
                                               double mythic) {
        return new RarityValue<>(common, uncommon, rare, epic, legendary, mythic, 0.0);
    }

    private static final class Regions {
        static final Set<RegionType> THE_PARK = Set.of(
                RegionType.BIRCH_PARK, RegionType.HOWLING_CAVE, RegionType.SPRUCE_WOODS, RegionType.VIKING_LONGHOUSE,
                RegionType.DARK_THICKET, RegionType.TRIALS_OF_FIRE, RegionType.SAVANNA_WOODLAND, RegionType.SOUL_CAVE,
                RegionType.MELODY_PLATEAU, RegionType.JUNGLE_ISLAND, RegionType.SPIRIT_CAVE);
    }
}
