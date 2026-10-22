package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusRngMeter;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.drill.DrillFuel;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.experiments.EnchantingXpService;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentRngMeter;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentTable;
import dev.daweapon.type.skyblockgeneric.forge.ForgeService;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastService;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorService;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityEggService;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryLevel;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;

import java.util.List;

import static dev.daweapon.type.skyblockgeneric.gui.inventories.centauri.ToyBoxButton.of;
import static dev.daweapon.type.skyblockgeneric.gui.inventories.centauri.ToyBoxButton.unavailable;

final class ToyBoxButtons {
    private ToyBoxButtons() {}

    static List<ToyBoxButton> general() {
        return List.of(
                of(Material.COOKIE, "<6>Cookie Jar", "<8>I'd give you a cookie, but I ate it.", "<7>Claim free infinite <6>Booster Cookies<7>!", p -> ToyBoxActions.give(p, ItemType.BOOSTER_COOKIE)),
                of(Material.POTION, "<6>Potion Seller", "<8>My potions are too strong for you.", "<7>Claim a <c>God Potion<7>!", p -> ToyBoxActions.give(p, ItemType.GOD_POTION)),
                of(Material.DIAMOND_PICKAXE, "<6>Max Items", "<8>The Great Sword begotten in dragon's breath...", "<7>Maximise the stack in your hand.", ToyBoxActions::maxItem),
                of(Material.GOLD_BLOCK, "<6>Duplicate Item", "<8>The original is impossible to discern.", "<7>Duplicate the item you are holding.", ToyBoxActions::duplicate),
                of(Material.PAINTING, "<6>Max Collections", "<8>Gotta collect 'em all.", "<7>Max every available collection.", ToyBoxActions::maxCollections),
                of(Material.DIAMOND_SWORD, "<6>Max Skills", "<8>Gotta collect 'em all.", "<7>Max every skill.", ToyBoxActions::maxSkills),
                of(Material.ROTTEN_FLESH, "<6>Max Slayers", "<8>Gotta collect 'em all.", "<7>Impress Maddox!", ToyBoxActions::maxSlayers));
    }

    static List<ToyBoxButton> uncategorised() {
        return List.of(
                of(Material.OAK_LOG, "<a>Fill Agatha's Rewards", "<8>Agatha never has to know.", "<7>Grant 25 top-bracket rewards from <b>Agatha's Contests<7>.",
                        p -> dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests.fillRewards(p, dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister.AGATHA, 25)),
                of(Material.JUNGLE_LOG, "<6>Fill Miria's Rewards", "<8>Torrhus sends its regards.", "<7>Grant 25 top-bracket rewards from <b>Miria's Contests<7>.",
                        p -> dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests.fillRewards(p, dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister.MIRIA, 25)));
    }

    static List<ToyBoxButton> mining() {
        return List.of(
                of(Material.PRISMARINE_CRYSTALS, "<5>50k HOTM Exp", "<8>The power of a pickaxe.", "<7>Gain <5>50,000 HOTM Experience<7>.", p -> HotmService.addExperience(p, 50_000)),
                of(Material.DIAMOND_ORE, "<b>Mining Event Item", "<8>Garry's favourite.", "<7>Roll one <b>Mining Event<7> reward item.", dev.daweapon.type.skyblockgeneric.miningevent.MiningEventRewards::giveMiningItem),
                of(Material.GUNPOWDER, "<a>Give 2m powder of each type", "<8>Mindcraft.", "<7>Get 2,000,000 of each powder type: <2>Mithril<7>, <d>Gemstone <7>and <b>Glacite<7>.", p -> HotmService.addPowderToAll(p, 2_000_000)),
                of(Material.BEACON, "<d>Power Crystals", "<8>Light the way.", "<7>Get <d>10 Power Crystals <7>and <6>2 Scorched Power Crystals<7> for your <b>Beacon<7>.", p -> {
                    ToyBoxActions.give(p, ItemType.POWER_CRYSTAL, 10);
                    ToyBoxActions.give(p, ItemType.SCORCHED_POWER_CRYSTAL, 2);
                }),
                of(Material.NETHER_STAR, "<5>Tokens of the Mountain", "<8>Spend them wisely.", "<7>Gain <5>10 Tokens of the Mountain<7>.", p -> HotmService.addTokens(p, 10)),
                of(Material.BEACON, "<5>Max Core of the Mountain", "<8>The heart of it all.", "<7>Set the Core of the Mountain to level 10.", HotmService::maxCore),
                of(Material.REDSTONE_BLOCK, "<c>Reset HOTM Perks", "<8>A fresh start.", "<7>Reset every Heart of the Mountain perk.", HotmService::reset),
                of(Material.FURNACE, "<6>Finish Forge Processes", "<8>Patience is overrated.", "<7>Finish every running <6>Forge<7> process.", ForgeService::finishAll),
                of(Material.BUCKET, "<2>Fill Drill Tank", "<8>Never run dry.", "<7>Refill the <a>Drill <7>in your hand to its maximum <2>Fuel<7>.", DrillFuel::fillHeld),
                of(Material.MINECART, "<d>Crystal Hollows Access", "<8>Skip the queue at Gwendolyn.", "<7>Gain permanent <d>Crystal Hollows<7> access.", CrystalHollowsAccess::grantFullAccess),
                of(Material.EXPERIENCE_BOTTLE, "<a>Set RNG Meter Progress", "<8>Luck, on demand.", "<7>Fill the <d>Crystal Nucleus<7> RNG Meter.", p -> {
                    var data = CrystalHollowsAccess.data(p);
                    data.setRngXp(10_000_000L);
                    CrystalHollowsAccess.save(p, data);
                }),
                of(Material.GLASS_BOTTLE, "<a>Clear RNG Meter Progress", "<8>Back to zero.", "<7>Clear the <d>Crystal Nucleus<7> RNG Meter.", NucleusRngMeter::reset));
    }

    static List<ToyBoxButton> garden() {
        return List.of(
                of(Material.SUNFLOWER, "<a>\"Organically Grown\" Garden Levels!", "<8>Fertiliser for your progress.", "<7>Gain <a>10,000 Garden Experience<7>.", p -> GardenService.addGardenXp(p, 10_000)),
                of(Material.GREEN_DYE, "<2>Pile of Sowdust", "<8>Farmed the hard way.", "<7>Give yourself <2>1,000,000,000 Sowdust<7>.", p -> ToyBoxActions.garden(p, profile -> profile.addSowdust(1_000_000_000D))),
                of(Material.BRICKS, "<c>Vault of Copper", "<8>Shiny and reddish.", "<7>Give yourself <c>1,000,000 Copper<7>.", p -> ToyBoxActions.garden(p, profile -> profile.addCopper(1_000_000D))),
                of(Material.VILLAGER_SPAWN_EGG, "<a>Visitors, Right Now!", "<8>Impatient customers.", "<7>Call the next <a>Garden Visitor <7>immediately.", p -> VisitorService.callNow(p, null)),
                of(Material.SPIDER_EYE, "<2>Infestation!", "<8>Gross.", "<7>Spawn <2>4 Pests <7>in the plot you are standing in.", ToyBoxActions::infestation),
                of(Material.HOPPER, "<2>Full Vacuum Bag", "<8>Hoovered up.", "<7>Add <2>1,000 Pests <7>to your vacuum bag.", p -> ToyBoxActions.garden(p, profile -> profile.pests().setStored(profile.pests().getStored() + 1_000))),
                of(Material.BREWING_STAND, "<a>Mutation Analyst", "<8>Skip the minigame.", "<7>Mark <a>every Mutation <7>as analyzed.", ToyBoxActions::analyseEveryMutation),
                of(Material.PLAYER_HEAD, "<2>Chip Stash", "<8>Silicon and green fingers.", "<7>Get <a>21 of every Garden Chip<7>.", ToyBoxActions::giveEveryChip),
                of(Material.PUMPKIN_PIE, "<6>Seasoning Rush", "<8>Season to taste.", "<7>Donate <a>50 Seasoning <7>to the Communal Stew.", p -> FeastService.addSeasoning(p, 50)),
                of(Material.CLOCK, "<a>Greenhouse Time Warp", "<8>Hours go by.", "<7>Simulate <a>6 hours <7>of Greenhouse growth.", p -> GreenhouseService.simulate(p, 6)),
                of(Material.WHITE_CONCRETE, "<a>Greenhouse Blueprint Shortcut", "<8>Sam says yes.", "<7>Skip Sam's Greenhouse quest so you can <a>convert a plot<7>.", ToyBoxActions::skipGreenhouseQuest));
    }

    static List<ToyBoxButton> experimentation() {
        return List.of(
                of(Material.ENCHANTING_TABLE, "<d>Enchanting Exp", "<8>Knowledge is power.", "<7>Gain <d>1,000,000 Enchanting Experience<7>.", p -> EnchantingXpService.award(p, 1_000_000)),
                of(Material.EXPERIENCE_BOTTLE, "<a>Experience Bottles", "<8>Smash them open.", "<7>Get <a>16 <7>of every <a>Experience Bottle<7>.", ToyBoxActions::giveBottles),
                of(Material.ENCHANTING_TABLE, "<5>Recharge Experimentation Table", "<8>Another round of experiments.", "<7>Restore every <a>Superpairs charge<7> and reset today's purchases.", ExperimentTable::resetCharges),
                of(Material.ENDER_EYE, "<d>Fill Experimentation RNG Meter", "<8>Luck, on demand.", "<7>Fill the <d>Experimentation Table<7> RNG Meter.", p -> ExperimentRngMeter.add(p, 5_000_000)));
    }

    static List<ToyBoxButton> currencies() {
        return List.of(
                of(Material.EMERALD, "<a>5k Gems", "<8>Get rich quick.", "<7>Gain <a>5,000 Gems<7>.", p -> p.addGems(5_000)),
                of(Material.SEA_LANTERN, "<a>Big ol' chunk o' Whispers!", "<8>To turn your tears to roses.", "<7>Get 1,000,000 Forest Whispers.", p -> {
                    p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FOREST_WHISPERS, DatapointLong.class).setValue(1_000_000L);
                    HotfService.syncSkyBlockXp(p);
                }),
                of(Material.NAME_TAG, "<2>Safari Tickets", "<8>Ticket to ride.", "<7>Get one of every <2>Critter Safari <7>ticket.", p -> {
                    for (String ticket : List.of("BASIC_SAFARI_TICKET", "ECONOMY_SAFARI_TICKET", "PREMIUM_SAFARI_TICKET", "FIRST_CLASS_SAFARI_TICKET")) {
                        p.addAndUpdateItem(ItemType.get(ticket));
                    }
                }),
                of(Material.SAND, "<6>Desert Whispers", "<8>Torrhus sends its regards.", "<7>Get 1,000,000 Desert Whispers.", p -> {
                    HotfService.setWhispers(p, HotfCurrency.DESERT, 1_000_000L);
                    HotfService.syncSkyBlockXp(p);
                }),
                of(Material.BONE_BLOCK, "<d>Fossil Essence", "<8>Older than time itself.", "<7>Gain <d>100,000 Fossil Essence<7>.", p -> Essences.add(p, EssenceType.FOSSIL, 100_000)),
                of(Material.NETHER_STAR, "<d>Every Essence", "<8>The full collection.", "<7>Gain <d>100,000<7> of every essence type.", p -> {
                    for (EssenceType type : EssenceType.values()) Essences.add(p, type, 100_000);
                }),
                of(Material.ENDER_EYE, "<5>Summoning Eyes", "<8>Wake the dragon.", "<7>Get <5>8 Summoning Eyes <7>for the Dragon's Nest altar.", p -> ToyBoxActions.give(p, ItemType.SUMMONING_EYE, 8)),
                of(Material.HOPPER, "<d>Max Community Upgrades", "<8>Elizabeth is thrilled.", "<7>Finish every <d>Account <7>and <d>Profile Upgrade <7>at its top tier.", dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgrades::maxAll),
                unavailable(Material.PRISMARINE_SHARD, "<a>Shards!", "<7>Get 10 of each available Shard."));
    }

    static List<ToyBoxButton> foraging() {
        return List.of(
                of(Material.JUNGLE_SAPLING, "<2>Unlock Heart of the Forest", "<8>Hina would be proud.", "<7>Unlock the <2>Heart of the Forest<7> without talking to Hina.", HotfService::unlock),
                of(Material.OAK_LOG, "<2>50k HotF Exp", "<8>The power of an axe.", "<7>Gain <2>50,000 HotF Experience<7>.", p -> HotfService.addExperience(p, 50_000)),
                of(Material.NETHER_STAR, "<2>Tokens of the Forest", "<8>Spend them wisely.", "<7>Gain <2>10 Tokens of the Forest<7>.", p -> HotfService.addTokens(p, 10)),
                of(Material.DARK_OAK_LOG, "<2>Max Center of the Forest", "<8>The heart of it all.", "<7>Set the Center of the Forest to level 5.", HotfService::maxCore),
                of(Material.BEACON, "<2>Max Heart of the Forest", "<8>Every perk at the top.", "<7>Max the tier, the Center and every available perk.", HotfService::maxOut),
                of(Material.WRITABLE_BOOK, "<2>Complete Hina's Chapters", "<8>Hina couldn't be prouder.", "<7>Complete every available <2>Moonglade Marsh <7>Chapter task.", dev.daweapon.type.skyblockgeneric.chapters.ChapterService::maxOut),
                of(Material.REDSTONE_BLOCK, "<c>Reset HotF Perks", "<8>A fresh start.", "<7>Reset every Heart of the Forest perk.", HotfService::reset));
    }

    static List<ToyBoxButton> hoppity() {
        return List.of(
                of(Material.COCOA_BEANS, "<6>1B Chocolate", "<8>Sweet tooth.", "<7>Gain <6>1,000,000,000 Chocolate<7>.", p -> ChocolateFactoryHelper.mutateData(p, data -> data.addChocolate(1_000_000_000L))),
                of(Material.RABBIT_FOOT, "<d>Complete Hoppity's Collection", "<8>Gotta catch 'em all.", "<7>Add every <a>Chocolate Rabbit <7>to your collection.", p -> ChocolateFactoryHelper.mutateData(p, data -> {
                    for (ChocolateRabbit rabbit : ChocolateRabbit.values()) data.addFoundRabbit(rabbit);
                })),
                of(Material.EGG, "<a>Roll a Chocolate Egg", "<8>Skip the hunt.", "<7>Open one <a>Chocolate Egg <7>and roll a rabbit.", p -> HoppityEggService.open(p, null, null, java.util.concurrent.ThreadLocalRandom.current())),
                of(Material.PLAYER_HEAD, "<a>Egglocator", "<8>Where did that egg go?", "<7>Claim an <a>Egglocator<7>.", p -> ToyBoxActions.give(p, ItemType.EGGLOCATOR)),
                of(Material.DROPPER, "<6>Prestige Now", "<8>Reset for glory.", "<7>Fill the prestige requirement and <6>prestige <7>your Chocolate Factory.", p -> ChocolateFactoryHelper.mutateData(p, data -> {
                    data.setChocolateThisPrestige(Math.max(data.getChocolateThisPrestige(), data.getNextPrestigeThreshold()));
                    data.prestige();
                })),
                of(Material.CLOCK, "<d>Fill Time Tower Charges", "<8>Time on tap.", "<7>Set your <d>Time Tower <7>to <a>3 charges<7>.", p -> ChocolateFactoryHelper.mutateData(p, data -> data.setTimeTowerCharges(DatapointChocolateFactory.ChocolateFactoryData.TIME_TOWER_MAX_CHARGES))),
                of(Material.BOW, "<c>Max Rabbit Hitman Slots", "<8>Hire the whole crew.", "<7>Buy every <c>Rabbit Hitman <7>slot your factory allows.", p -> ChocolateFactoryHelper.mutateData(p, data ->
                        data.setHitmanSlots(ChocolateFactoryLevel.forPrestige(data.getPrestigeLevel()).getMaxHitmanSlots()))));
    }

    static List<ToyBoxButton> dungeons() {
        return List.of(
                of(Material.WITHER_SKELETON_SKULL, "<c>Catacombs XP", "<8>The dead keep their secrets.", "<7>Gain <c>5,000,000 Catacombs Experience<7>.", p -> CatacombsProgress.addXp(p, 5_000_000L)));
    }
}
