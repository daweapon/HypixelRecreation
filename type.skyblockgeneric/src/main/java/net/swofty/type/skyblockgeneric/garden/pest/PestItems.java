package net.swofty.type.skyblockgeneric.garden.pest;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.consumable.TimedConsumables;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PestItems {
    public static final String VACUUM = "PEST_VACUUM";
    public static final String SPRAYONATOR = "SPRAYONATOR";
    public static final String REPELLENT = "PEST_REPELLENT";
    public static final String REPELLENT_MAX = "PEST_REPELLENT_MAX";
    public static final long SPRAY_COOLDOWN_MILLIS = 3000;
    public static final int LARVA_BONUS = 2;
    public static final int MAX_LARVA_BONUS = 10;
    public static final List<String> MATERIALS = List.of("COMPOST", "HONEY_JAR", "DUNG", "PLANT_MATTER", "TASTY_CHEESE", "JELLY");

    private static final Map<UUID, Long> SPRAYED = new ConcurrentHashMap<>();

    private PestItems() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(VACUUM, InteractableItemConfig.builder()
                .rightClickHandler(PestVacuum::hold)
                .leftClickHandler(PestVacuum::track)
                .build());
        InteractableRegistry.register(SPRAYONATOR, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> spray(player))
                .leftClickHandler((player, item) -> cycle(player))
                .build());
        for (PestTraps.Kind kind : PestTraps.KINDS.values()) {
            InteractableRegistry.register(kind.item().name(), InteractableItemConfig.builder()
                    .rightClickHandler((player, item) -> PestTraps.place(player, item, kind.id())).build());
        }
        InteractableRegistry.register("WRIGGLING_LARVA", InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            GardenProfile profile = Gardens.profile(player);
            if (profile.pests().getPermanentBonusChance() >= MAX_LARVA_BONUS) {
                player.sendMessage(Text.of("<c>The sight of them makes you too sick to consume more!"));
                return;
            }
            TimedConsumables.consumeHeld(player, item);
            profile.pests().setPermanentBonusChance(profile.pests().getPermanentBonusChance() + LARVA_BONUS);
            Gardens.save(player, profile);
            player.sendMessage(Text.of("<a>You permanently gained <2>+{} Bonus Pest Chance<a>!", LARVA_BONUS));
        }).build());
        InteractableRegistry.register(REPELLENT, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> repel(player, item, false)).build());
        InteractableRegistry.register(REPELLENT_MAX, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> repel(player, item, true)).build());
    }

    public static void registerLore() {
        LoreRegistry.register(VACUUM, new LoreConfig((item, player) -> List.of("",
                "<7>Vacuum Bag: <2>" + Gardens.profile(player).pests().getStored() + " Pests", "<8>Vacuum Pests in your Garden to", "<8>increase!"),
                null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
        LoreRegistry.register(SPRAYONATOR, new LoreConfig((item, player) -> {
            String material = Gardens.profile(player).pests().getSelectedSpray();
            return List.of("", "<7>Selected Material: <a>" + materialName(material),
                    "<7>Attracts: <6>" + String.join("<7>, <6>", attracts(material)));
        }, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static String materialName(String material) {
        ItemType type = ItemType.get(material);
        return type == null ? material : new SkyBlockItem(type).getDisplayName();
    }

    public static List<String> attracts(String material) {
        return PestCatalogue.get().natural().stream().filter(pest -> material.equals(pest.spray())).map(PestCatalogue.Pest::name).toList();
    }

    private static void cycle(SkyBlockPlayer player) {
        if (!PestVacuum.inGarden(player)) return;
        PestData data = Gardens.profile(player).pests();
        int next = (MATERIALS.indexOf(data.getSelectedSpray()) + 1) % MATERIALS.size();
        data.setSelectedSpray(MATERIALS.get(next));
        player.sendMessage(Text.of("<7>Selected Material: <a>{} <7>(attracts <6>{}<7>)",
                materialName(MATERIALS.get(next)), String.join("<7>, <6>", attracts(MATERIALS.get(next)))));
    }

    private static void spray(SkyBlockPlayer player) {
        if (!PestVacuum.inGarden(player)) {
            player.sendMessage(Text.of("<c>You can only spray plots in the Garden!"));
            return;
        }
        GardenProfile profile = Gardens.profile(player);
        int plot = GardenLayout.plotAt(player.getPosition().x(), player.getPosition().z());
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN || !profile.isPlotUnlocked(plot)) {
            player.sendMessage(Text.of("<c>You can only spray one of your own plots!"));
            return;
        }
        long now = System.currentTimeMillis();
        Long last = SPRAYED.get(player.getUuid());
        if (last != null && now - last < SPRAY_COOLDOWN_MILLIS) return;
        PestData data = profile.pests();
        ItemType material = ItemType.get(data.getSelectedSpray());
        if (material == null) return;
        if (player.getAmountInInventory(material) > 0) {
            player.removeItemFromPlayer(material, 1);
        } else if (player.getSackItems().getAmount(material) > 0) {
            player.getSackItems().decrease(material, 1);
        } else {
            player.sendMessage(Text.of("<c>You don't have any {}<c>!", materialName(data.getSelectedSpray())));
            return;
        }
        SPRAYED.put(player.getUuid(), now);
        data.getSprays().put(plot, new PestData.Spray(data.getSelectedSpray(), now + PestRules.SPRAY_DURATION_MILLIS));
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>Sprayed <a>Plot <7>- <b>{} <a>with <a>{}<a>! <7>Attracts <6>{}<7>.", plot,
                materialName(data.getSelectedSpray()), String.join("<7>, <6>", attracts(data.getSelectedSpray()))));
    }

    private static void repel(SkyBlockPlayer player, SkyBlockItem item, boolean max) {
        PestData data = Gardens.profile(player).pests();
        long now = System.currentTimeMillis();
        if (data.getRepellentUntil() > now) {
            player.sendMessage(Text.of("<c>You are already covered in a Pest Repellent!"));
            return;
        }
        TimedConsumables.consumeHeld(player, item);
        data.setRepellentUntil(now + PestRules.REPELLENT_DURATION_MILLIS);
        data.setRepellentMax(max);
        player.sendMessage(Text.of("<a>You are covered in a pleasant aroma! <2>Pests <7>will take <a>{}x <7>as long to spawn for <a>60m<7>.", max ? 4 : 2));
    }
}
