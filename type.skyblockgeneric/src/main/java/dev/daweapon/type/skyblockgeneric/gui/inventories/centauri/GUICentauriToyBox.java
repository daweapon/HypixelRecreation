package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkills;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Developer/cheat GUI granting items, currencies, and maxed progression for testing
public final class GUICentauriToyBox implements View<GUICentauriToyBox.State> {
    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Toy Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        info(layout, 4, Material.CHEST, "<a>Toy Box", "<7>A variety of useful and cheaty", "<7>utilities to make your life easier!");
        action(layout, 10, Material.COOKIE, "<6>Cookie Jar", "<8>I'd give you a cookie, but I ate it.", "<7>Claim free infinite <6>Booster Cookies<7>!", p -> give(p, ItemType.BOOSTER_COOKIE)); //should give years worth of cookie buff once cookie is impl
        action(layout, 11, Material.POTION, "<6>Potion Seller", "<8>My potions are too strong for you.", "<7>Claim a <c>God Potion<7>!", p -> give(p, ItemType.GOD_POTION)); //should give all potion effects instead of god potion for a long time
        action(layout, 12, Material.DIAMOND_PICKAXE, "<6>Max Items", "<8>The Great Sword begotten in dragon's breath...", "<7>Maximise the stack in your hand.", this::maxItem);
        action(layout, 13, Material.GOLD_BLOCK, "<6>Duplicate Item", "<8>The original is impossible to discern.", "<7>Duplicate the item you are holding.", this::duplicate);
        action(layout, 14, Material.PAINTING, "<6>Max Collections", "<8>Gotta collect 'em all.", "<7>Max every available collection.", this::maxCollections);
        action(layout, 15, Material.DIAMOND_SWORD, "<6>Max Skills", "<8>Gotta collect 'em all.", "<7>Max every skill.", this::maxSkills);
        action(layout, 16, Material.ROTTEN_FLESH, "<6>Max Slayers", "<8>Gotta collect 'em all.", "<7>Impress Maddox!", this::maxSlayers);
        action(layout, 17, Material.ENCHANTING_TABLE, "<d>Enchanting Exp", "<8>Knowledge is power.", "<7>Gain <d>1,000,000 Enchanting Experience<7>.", p -> dev.daweapon.type.skyblockgeneric.experiments.EnchantingXpService.award(p, 1_000_000));
        action(layout, 18, Material.EXPERIENCE_BOTTLE, "<a>Experience Bottles", "<8>Smash them open.", "<7>Get <a>16 <7>of every <a>Experience Bottle<7>.", this::giveBottles);
        action(layout, 26, Material.ENCHANTING_TABLE, "<5>Recharge Experimentation Table", "<8>Another round of experiments.", "<7>Restore every <a>Superpairs charge<7> and reset today's purchases.", dev.daweapon.type.skyblockgeneric.experiments.ExperimentTable::resetCharges);
        action(layout, 27, Material.ENDER_EYE, "<d>Fill Experimentation RNG Meter", "<8>Luck, on demand.", "<7>Fill the <d>Experimentation Table<7> RNG Meter.", p -> dev.daweapon.type.skyblockgeneric.experiments.ExperimentRngMeter.add(p, 5_000_000));
        action(layout, 19, Material.PRISMARINE_CRYSTALS, "<5>50k HOTM Exp", "<8>The power of a pickaxe.", "<7>Gain <5>50,000 HOTM Experience<7>.", p -> HotmService.addExperience(p, 50_000));
        action(layout, 20, Material.GUNPOWDER, "<a>Give 2m powder of each type", "<8>Mindcraft.", "<7>Get 2,000,000 of each powder type: <2>Mithril<7>, <d>Gemstone <7>and <b>Glacite<7>.", p -> HotmService.addPowderToAll(p, 2_000_000));
        action(layout, 21, Material.EMERALD, "<a>5k Gems", "<8>Get rich quick.", "<7>Set your gem purse to 5,000.", p -> p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).setValue(5_000));
        action(layout, 22, Material.EXPERIENCE_BOTTLE, "<a>Set RNG Meter Progress", "<8>Luck, on demand.", "<7>Fill the <d>Crystal Nucleus<7> RNG Meter.", p -> { var data = dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess.data(p); data.setRngXp(10_000_000L); dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess.save(p, data); });
        action(layout, 23, Material.GLASS_BOTTLE, "<a>Clear RNG Meter Progress", "<8>Back to zero.", "<7>Clear the <d>Crystal Nucleus<7> RNG Meter.", dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusRngMeter::reset);
        unavailable(layout, 24, Material.OAK_LOG, "<a>Fill Agatha's Rewards", "<7>Grant 25 contest rewards.");
        action(layout, 25, Material.SEA_LANTERN, "<a>Big ol' chunk o' Whispers!", "<8>To turn your tears to roses.", "<7>Get 1,000,000 Forest Whispers.", p -> p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FOREST_WHISPERS, DatapointLong.class).setValue(1_000_000L));
        action(layout, 34, Material.NETHER_STAR, "<5>Tokens of the Mountain", "<8>Spend them wisely.", "<7>Gain <5>10 Tokens of the Mountain<7>.", p -> HotmService.addTokens(p, 10));
        action(layout, 37, Material.BEACON, "<5>Max Core of the Mountain", "<8>The heart of it all.", "<7>Set the Core of the Mountain to level 10.", HotmService::maxCore);
        action(layout, 32, Material.REDSTONE_BLOCK, "<c>Reset HOTM Perks", "<8>A fresh start.", "<7>Reset every Heart of the Mountain perk.", HotmService::reset);
        action(layout, 33, Material.FURNACE, "<6>Finish Forge Processes", "<8>Patience is overrated.", "<7>Finish every running <6>Forge<7> process.", p -> dev.daweapon.type.skyblockgeneric.forge.ForgeService.finishAll(p));
        action(layout, 38, Material.BONE_BLOCK, "<d>Fossil Essence", "<8>Older than time itself.", "<7>Gain <d>100,000 Fossil Essence<7>.", p -> dev.daweapon.type.skyblockgeneric.essence.Essences.add(p, dev.daweapon.type.skyblockgeneric.essence.EssenceType.FOSSIL, 100_000));
        action(layout, 39, Material.NETHER_STAR, "<d>Every Essence", "<8>The full collection.", "<7>Gain <d>100,000<7> of every essence type.", p -> { for (dev.daweapon.type.skyblockgeneric.essence.EssenceType type : dev.daweapon.type.skyblockgeneric.essence.EssenceType.values()) dev.daweapon.type.skyblockgeneric.essence.Essences.add(p, type, 100_000); });
        unavailable(layout, 28, Material.PRISMARINE_SHARD, "<a>Shards!", "<7>Get 10 of each available Shard.");
        action(layout, 29, Material.GREEN_DYE, "<2>Pile of Sowdust", "<8>Farmed the hard way.", "<7>Give yourself <2>1,000,000,000 Sowdust<7>.", p -> addGarden(p, profile -> profile.addSowdust(1_000_000_000D)));
        action(layout, 30, Material.BRICKS, "<c>Vault of Copper", "<8>Shiny and reddish.", "<7>Give yourself <c>1,000,000 Copper<7>.", p -> addGarden(p, profile -> profile.addCopper(1_000_000D)));
        action(layout, 40, Material.VILLAGER_SPAWN_EGG, "<a>Visitors, Right Now!", "<8>Impatient customers.", "<7>Call the next <a>Garden Visitor <7>immediately.", p -> dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorService.callNow(p, null));
        action(layout, 41, Material.SPIDER_EYE, "<2>Infestation!", "<8>Gross.", "<7>Spawn <2>4 Pests <7>in the plot you are standing in.", p -> {
            int plot = dev.daweapon.type.skyblockgeneric.garden.GardenLayout.plotAt(p.getPosition().x(), p.getPosition().z());
            if (plot == dev.daweapon.type.skyblockgeneric.garden.GardenLayout.NONE || plot == dev.daweapon.type.skyblockgeneric.garden.GardenLayout.BARN) { p.sendMessage("<c>Stand in a Garden plot first!"); return; }
            dev.daweapon.type.skyblockgeneric.garden.pest.PestService.spawn(p, plot, 4, null);
        });
        action(layout, 42, Material.HOPPER, "<2>Full Vacuum Bag", "<8>Hoovered up.", "<7>Add <2>1,000 Pests <7>to your vacuum bag.", p -> addGarden(p, profile -> profile.pests().setStored(profile.pests().getStored() + 1_000)));
        action(layout, 35, Material.BREWING_STAND, "<a>Mutation Analyst", "<8>Skip the minigame.", "<7>Mark <a>every Mutation <7>as analyzed.", p -> addGarden(p, profile -> { for (var mutation : dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationCatalogue.get().all()) profile.greenhouse().getAnalyzed().add(mutation.id()); }));
        action(layout, 36, Material.PLAYER_HEAD, "<2>Chip Stash", "<8>Silicon and green fingers.", "<7>Get <a>21 of every Garden Chip<7>.", p -> { for (var chip : dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.values()) { var type = dev.daweapon.commons.skyblock.item.ItemType.get(chip.getItemId()); if (type != null) p.addAndUpdateItem(new dev.daweapon.type.skyblockgeneric.item.SkyBlockItem(type, 21)); } });
        action(layout, 45, Material.PUMPKIN_PIE, "<6>Seasoning Rush", "<8>Season to taste.", "<7>Donate <a>50 Seasoning <7>to the Communal Stew.", p -> dev.daweapon.type.skyblockgeneric.garden.feast.FeastService.addSeasoning(p, 50));
        action(layout, 43, Material.CLOCK, "<a>Greenhouse Time Warp", "<8>Hours go by.", "<7>Simulate <a>6 hours <7>of Greenhouse growth.", p -> dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService.simulate(p, 6));
        action(layout, 44, Material.WHITE_CONCRETE, "<a>Greenhouse Blueprint Shortcut", "<8>Sam says yes.", "<7>Skip Sam's Greenhouse quest so you can <a>convert a plot<7>.", p -> addGarden(p, profile -> { profile.greenhouse().setBlueprintGiven(true); profile.greenhouse().setBlueprintOffered(true); profile.greenhouse().setBlueprintDelivered(true); profile.greenhouse().setResourcesDelivered(true); }));
        action(layout, 31, Material.SUNFLOWER, "<a>\"Organically Grown\" Garden Levels!", "<8>Fertiliser for your progress.", "<7>Gain <a>10,000 Garden Experience<7>.", p -> dev.daweapon.type.skyblockgeneric.garden.GardenService.addGardenXp(p, 10_000));
        action(layout, 46, Material.MINECART, "<d>Crystal Hollows Access", "<8>Skip the queue at Gwendolyn.", "<7>Gain permanent <d>Crystal Hollows<7> access.", dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess::grantFullAccess);
        action(layout, 47, Material.BUCKET, "<2>Fill Drill Tank", "<8>Never run dry.", "<7>Refill the <a>Drill <7>in your hand to its maximum <2>Fuel<7>.", p -> dev.daweapon.type.skyblockgeneric.drill.DrillFuel.fillHeld(p));
        info(layout, 50, Material.OAK_SIGN, "<a>Got Suggestions?", "<7>Share Alpha Network utility ideas in our discord!");
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private void addGarden(SkyBlockPlayer player, java.util.function.Consumer<dev.daweapon.type.skyblockgeneric.garden.GardenProfile> change) {
        var profile = dev.daweapon.type.skyblockgeneric.garden.Gardens.profile(player);
        change.accept(profile);
        dev.daweapon.type.skyblockgeneric.garden.Gardens.save(player, profile);
    }

    private void duplicate(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) { player.sendMessage("<c>Hold an item first!"); return; }
        player.addAndUpdateItem(held);
    }

    private void maxItem(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) { player.sendMessage("<c>Hold an item first!"); return; }
        player.updateItemInSlot(player.getHeldSlot(), item -> {
            int applied = CentauriItemMaximizer.maximize(player, item);
            if (applied == 0) player.sendMessage("<e>That item has no supported upgrades left to apply.");
            else player.sendMessage("<a>Applied <e>{} <a>maximum item upgrades!", applied);
        });
    }

    private void maxCollections(SkyBlockPlayer player) {
        CollectionCategories.getCategories().forEach(category -> {
            for (var collection : category.getCollections()) player.getCollection().set(collection.type(), Integer.MAX_VALUE);
        });
    }

    private void maxSkills(SkyBlockPlayer player) {
        var skills = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKILLS, DatapointSkills.class).getValue();
        for (SkillCategories category : SkillCategories.values()) skills.setRaw(player, category, 1_000_000_000D);
    }

    private void maxSlayers(SkyBlockPlayer player) {
        var slayers = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).getValue();
        for (SlayerType type : SlayerType.values()) slayers.progress(type).setXp(Integer.MAX_VALUE);
    }

    private DatapointHOTM.PlayerHOTMData hotm(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();
    }

    private void giveBottles(SkyBlockPlayer player) {
        for (var bottle : dev.daweapon.type.skyblockgeneric.experiments.ExperienceBottle.values()) {
            SkyBlockItem item = new SkyBlockItem(bottle.itemType());
            item.setAmount(16);
            player.addAndUpdateItem(item);
        }
    }
    private void give(SkyBlockPlayer player, ItemType type) { player.addAndUpdateItem(type); }
    private void success(SkyBlockPlayer player) { player.sendMessage("<a>Centauri granted your request!"); }

    private void action(ViewLayout<State> layout, int slot, Material material, String name, String flavor, String description, java.util.function.Consumer<SkyBlockPlayer> action) {
        layout.slot(slot, (s, c) -> GUICentauri.item(name, material, flavor, " ", description, " ", "<e>Click to claim!"),
                (click, c) -> { action.accept((SkyBlockPlayer) c.player()); success((SkyBlockPlayer) c.player()); });
    }

    private void unavailable(ViewLayout<State> layout, int slot, Material material, String name, String description) {
        layout.slot(slot, (s, c) -> GUICentauri.item(name, material, "<8>Coming from Centauri's toy box.", " ", description, " ", "<e>Click to claim!"),
                (click, c) -> c.player().sendMessage("<c>This progression system is not available on this server type yet."));
    }

    private void info(ViewLayout<State> layout, int slot, Material material, String name, String... lore) {
        layout.slot(slot, (s, c) -> GUICentauri.item(name, material, lore));
    }

    public record State() {}
}
