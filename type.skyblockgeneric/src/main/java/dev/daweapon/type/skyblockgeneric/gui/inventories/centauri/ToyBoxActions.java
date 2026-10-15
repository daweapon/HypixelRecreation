package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkills;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.experiments.ExperienceBottle;
import dev.daweapon.type.skyblockgeneric.garden.GardenLayout;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

final class ToyBoxActions {
    private ToyBoxActions() {}

    static void give(SkyBlockPlayer player, ItemType type) {
        player.addAndUpdateItem(type);
    }

    static void duplicate(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) { player.sendMessage("<c>Hold an item first!"); return; }
        player.addAndUpdateItem(held);
    }

    static void maxItem(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) { player.sendMessage("<c>Hold an item first!"); return; }
        player.updateItemInSlot(player.getHeldSlot(), item -> {
            int applied = CentauriItemMaximizer.maximize(player, item);
            if (applied == 0) player.sendMessage("<e>That item has no supported upgrades left to apply.");
            else player.sendMessage("<a>Applied <e>{} <a>maximum item upgrades!", applied);
        });
    }

    static void maxCollections(SkyBlockPlayer player) {
        CollectionCategories.getCategories().forEach(category -> {
            for (var collection : category.getCollections()) player.getCollection().set(collection.type(), Integer.MAX_VALUE);
        });
    }

    static void maxSkills(SkyBlockPlayer player) {
        var skills = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKILLS, DatapointSkills.class).getValue();
        for (SkillCategories category : SkillCategories.values()) skills.setRaw(player, category, 1_000_000_000D);
    }

    static void maxSlayers(SkyBlockPlayer player) {
        var slayers = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).getValue();
        for (SlayerType type : SlayerType.values()) slayers.progress(type).setXp(Integer.MAX_VALUE);
    }

    static void giveBottles(SkyBlockPlayer player) {
        for (var bottle : ExperienceBottle.values()) {
            SkyBlockItem item = new SkyBlockItem(bottle.itemType());
            item.setAmount(16);
            player.addAndUpdateItem(item);
        }
    }

    static void garden(SkyBlockPlayer player, Consumer<GardenProfile> change) {
        var profile = Gardens.profile(player);
        change.accept(profile);
        Gardens.save(player, profile);
    }

    static void infestation(SkyBlockPlayer player) {
        int plot = GardenLayout.plotAt(player.getPosition().x(), player.getPosition().z());
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN) { player.sendMessage("<c>Stand in a Garden plot first!"); return; }
        PestService.spawn(player, plot, 4, null);
    }

    static void analyseEveryMutation(SkyBlockPlayer player) {
        garden(player, profile -> {
            for (var mutation : MutationCatalogue.get().all()) profile.greenhouse().getAnalyzed().add(mutation.id());
        });
    }

    static void giveEveryChip(SkyBlockPlayer player) {
        for (var chip : GardenChip.values()) {
            var type = ItemType.get(chip.getItemId());
            if (type != null) player.addAndUpdateItem(new SkyBlockItem(type, 21));
        }
    }

    static void skipGreenhouseQuest(SkyBlockPlayer player) {
        garden(player, profile -> {
            profile.greenhouse().setBlueprintGiven(true);
            profile.greenhouse().setBlueprintOffered(true);
            profile.greenhouse().setBlueprintDelivered(true);
            profile.greenhouse().setResourcesDelivered(true);
        });
    }
}
