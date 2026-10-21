package dev.daweapon.type.skyblockgeneric.item.handlers.interactable;

import dev.daweapon.type.skyblockgeneric.crimson.CrimsonItemHandlers;
import dev.daweapon.type.skyblockgeneric.minion.inferno.ReaperPepper;
import dev.daweapon.type.skyblockgeneric.gifts.BottleOfJyrre;
import dev.daweapon.type.skyblockgeneric.gifts.SnowCannon;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import dev.daweapon.type.skyblockgeneric.elections.jerrypocalypse.HiddenJerryType;
import dev.daweapon.type.skyblockgeneric.elections.jerrypocalypse.JerryBoxLoot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.hologram.ServerHolograms;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockMenu;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIBoosterCookie;
import dev.daweapon.type.skyblockgeneric.potion.GodPotionService;
import dev.daweapon.type.skyblockgeneric.slayer.MaddoxBatphone;
import dev.daweapon.type.skyblockgeneric.spray.SprayCanHandlers;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.pets.PetItems;
import dev.daweapon.type.skyblockgeneric.utility.JerryInformation;

import java.util.HashMap;
import java.util.Map;

public class InteractableRegistry {
    private static final Map<String, InteractableItemConfig> REGISTERED_HANDLERS = new HashMap<>();

    static {
        MythologicalItemHandlers.registerInteractables();
        dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingTools.registerInteractables();
        dev.daweapon.type.skyblockgeneric.hoppity.Egglocator.registerInteractables();
        BottleOfJyrre.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.Salt.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.ItemBundle.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.DwarvenOs.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.GreatSpookPotion.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.JerryCandy.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.FinnsFocaccia.registerInteractables();
        dev.daweapon.type.skyblockgeneric.spookyfestival.BlastOLantern.registerInteractables();
        dev.daweapon.type.skyblockgeneric.experiments.ExperienceBottle.registerInteractables();
        dev.daweapon.type.skyblockgeneric.experiments.MetaphysicalSerum.registerInteractables();
        SnowCannon.registerInteractables();
        CrimsonItemHandlers.registerInteractables();
        dev.daweapon.type.skyblockgeneric.crystalhollows.GoblinEggs.registerInteractables();
        dev.daweapon.type.skyblockgeneric.crystalhollows.MetalDetector.registerInteractables();
        dev.daweapon.type.skyblockgeneric.hunting.creature.trigger.HoneycombPots.registerInteractables();
        ReaperPepper.registerInteractables();
        dev.daweapon.type.skyblockgeneric.consumable.VialOfVenom.registerInteractables();
        dev.daweapon.type.skyblockgeneric.item.utility.BiomeSticks.registerInteractables();
        PetItems.registerInteractables();
        dev.daweapon.type.skyblockgeneric.garden.pest.PestItems.registerInteractables();
        dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseItems.registerInteractables();
        dev.daweapon.type.skyblockgeneric.garden.chip.ChipItems.registerInteractables();
        dev.daweapon.type.skyblockgeneric.garden.tool.FarmingToolItems.registerInteractables();
        dev.daweapon.type.skyblockgeneric.garden.tool.FarmingToolkit.registerInteractables();
        dev.daweapon.type.skyblockgeneric.bank.PersonalBankItem.registerInteractables();
        dev.daweapon.type.skyblockgeneric.accessory.enrichment.Enrichments.registerInteractables();
        register("SKYBLOCK_MENU_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                ((skyBlockPlayer, skyBlockItem) -> skyBlockPlayer.openView(new GUISkyBlockMenu()))
        ).leftClickHandler(
                ((skyBlockPlayer, skyBlockItem) -> skyBlockPlayer.openView(new GUISkyBlockMenu()))
        ).inventoryInteractHandler(
                ((skyBlockPlayer, skyBlockItem) -> {
                    skyBlockPlayer.openView(new GUISkyBlockMenu());
                    return false;
                })
        ).build());
        register("MOVE_JERRY_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                ((player, skyBlockItem) -> {
                    if (!HypixelConst.isIslandServer()) {
                        player.sendMessage("<c>You can't move Jerry here! He doesn't belong here!");
                        return;
                    }

                    Point position = player.getTargetBlockPosition(5);
                    if (position == null) {
                        return;
                    }

                    // Move up one block and center jerry
                    position = position.add(0.5, 1, 0.5);

                    SkyBlockIsland island = player.getSkyBlockIsland();
                    JerryInformation jerryInformation = island.getJerryInformation();

                    ServerHolograms.ExternalHologram hologram = jerryInformation.getHologram();
                    ServerHolograms.removeExternalHologram(hologram);

                    jerryInformation.setJerryPosition(new Pos(position).withLookAt(player.getPosition()));
                    jerryInformation.getJerry().teleport(jerryInformation.getJerryPosition());
                    jerryInformation.getJerry().lookAt(player.getPosition().add(0, 1.4, 0));

                    hologram = ServerHolograms.ExternalHologram.builder()
                            .text(hologram.getText())
                            .instance(hologram.getInstance())
                            .pos(jerryInformation.getJerryPosition().add(0, 1, 0))
                            .build();

                    ServerHolograms.addExternalHologram(hologram);
                    jerryInformation.setHologram(hologram);
                    skyBlockItem.setAmount(0);
                })
        ).build());
        register(SprayCanHandlers.HANDLER_ID, SprayCanHandlers.interactable());
        register("MADDOX_BATPHONE_CALL", InteractableItemConfig.builder().rightClickHandler(
                (player, item) -> MaddoxBatphone.call(player)
        ).build());
        register("BOOSTER_COOKIE_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                (player, item) -> new GUIBoosterCookie().open(player)
        ).build());
        register("GOD_POTION_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                (player, item) -> GodPotionService.consume(player, item)
        ).build());
        register("JERRY_BOX_OPEN", InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            HiddenJerryType type = HiddenJerryType.ofBox(item.getAttributeHandler().getPotentialType());
            if (type == null) return;
            player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
            JerryBoxLoot.open(player, type);
        }).build());
    }

    public static void register(String id, InteractableItemConfig handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static InteractableItemConfig getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}
