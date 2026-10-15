package dev.daweapon.type.skyblockgeneric.garden.pest;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.garden.GardenLayout;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PestItems {
    public static final String VACUUM = "PEST_VACUUM";
    public static final String SPRAYONATOR = "SPRAYONATOR";
    public static final String REPELLENT = "PEST_REPELLENT";
    public static final String REPELLENT_MAX = "PEST_REPELLENT_MAX";
    public static final String PORTABLE_WASHER = "PORTABLE_WASHER";
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
                .rightClickHandler((player, item) -> spray(player, item))
                .leftClickHandler((player, item) -> configure(player, item))
                .build());
        PestDisco.registerInteractables();
        InteractableRegistry.register(PORTABLE_WASHER, InteractableItemConfig.builder()
                .rightClickHandler(PestItems::wash).build());
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
        LoreRegistry.register(VACUUM, new LoreConfig((item, player) -> vacuumLore(item, player),
                null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
        LoreRegistry.register(SPRAYONATOR, new LoreConfig((item, player) -> {
            PestData data = Gardens.profile(player).pests();
            PestNozzle nozzle = selectedNozzle(data, item);
            return List.of("", "<7>Selected Nozzle: " + nozzle.colored(),
                    "<7>Selected Material: <a>" + materialName(data.getSelectedSpray()),
                    "<7>Attracts: <6>" + String.join("<7>, <6>", attracts(data.getSelectedSpray())));
        }, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    private static List<String> vacuumLore(SkyBlockItem item, SkyBlockPlayer player) {
        PestData data = Gardens.profile(player).pests();
        List<String> lore = new ArrayList<>();
        lore.add("");
        int books = item.getAttributeHandler().getBookwormBooks();
        if (books > 0) lore.add("<7>Bookworm's Favorite Book: <c>+" + books * PestRules.BOOKWORM_DAMAGE + " Damage <8>(" + books + "/" + PestRules.MAX_VACUUM_BOOKS + ")");
        int dummies = item.getAttributeHandler().getFarmingForDummies();
        if (dummies > 0) lore.add("<7>Farming for Dummies: <6>+" + dummies * PestRules.FARMING_FOR_DUMMIES_FORTUNE + " Farming Fortune <8>(" + dummies + "/" + PestRules.MAX_VACUUM_BOOKS + ")");
        if (PestVinyls.isHooverius(item)) {
            lore.add("<7>Now Playing: " + PestVinyls.playingName(data.getPlayingVinyl()));
            lore.add("");
        }
        lore.add("<7>Vacuum Bag: <2>" + data.getStored() + " Pests");
        lore.add("<8>Vacuum Pests in your Garden to");
        lore.add("<8>increase!");
        return lore;
    }

    private static PestNozzle bestNozzle(SkyBlockItem item) {
        PestNozzle best = PestNozzle.ofSprayonator(item.getAttributeHandler().getTypeAsString());
        return best == null ? PestNozzle.BASIC : best;
    }

    private static PestNozzle selectedNozzle(PestData data, SkyBlockItem item) {
        return PestNozzle.parse(data.getSelectedNozzle()).clamp(bestNozzle(item));
    }

    public static String materialName(String material) {
        ItemType type = ItemType.get(material);
        return type == null ? material : new SkyBlockItem(type).getDisplayName();
    }

    public static List<String> attracts(String material) {
        return PestCatalogue.get().natural().stream().filter(pest -> material.equals(pest.spray())).map(PestCatalogue.Pest::name).toList();
    }

    private static void configure(SkyBlockPlayer player, SkyBlockItem item) {
        if (!PestVacuum.inGarden(player)) return;
        PestData data = Gardens.profile(player).pests();
        if (player.isSneaking()) {
            PestNozzle best = bestNozzle(item);
            PestNozzle next = selectedNozzle(data, item).cycle(best);
            data.setSelectedNozzle(next.name());
            player.sendMessage(Text.of("<7>Selected Nozzle: {} <7>(<2>+{} Bonus Pest Chance<7>, <a>{}x <7>spawn chance, uses <a>{} <7>per spray)",
                    next.colored(), next.bonusChance(), next.spawnMultiplier(), next.materialCost()));
            return;
        }
        int next = (MATERIALS.indexOf(data.getSelectedSpray()) + 1) % MATERIALS.size();
        data.setSelectedSpray(MATERIALS.get(next));
        player.sendMessage(Text.of("<7>Selected Material: <a>{} <7>(attracts <6>{}<7>)",
                materialName(MATERIALS.get(next)), Text.parse(String.join("<7>, <6>", attracts(MATERIALS.get(next))))));
    }

    private static void spray(SkyBlockPlayer player, SkyBlockItem item) {
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
        PestNozzle nozzle = selectedNozzle(data, item);
        if (!player.removeItemFromPlayer(material, nozzle.materialCost())) {
            player.sendMessage(Text.of("<c>You need {}x {} <c>to spray with the {} <c>Nozzle!", nozzle.materialCost(),
                    materialName(data.getSelectedSpray()), nozzle.colored()));
            return;
        }
        SPRAYED.put(player.getUuid(), now);
        data.getSprays().put(plot, new PestData.Spray(data.getSelectedSpray(), now + nozzle.durationMillis(), nozzle.name()));
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>Sprayed <a>Plot <7>- <b>{} <a>with <a>{} <7>({} Nozzle<7>)<a>! <7>Attracts <6>{}<7>.", plot,
                materialName(data.getSelectedSpray()), Text.parse(nozzle.colored()), Text.parse(String.join("<7>, <6>", attracts(data.getSelectedSpray())))));
    }

    private static void wash(SkyBlockPlayer player, SkyBlockItem item) {
        if (!PestVacuum.inGarden(player)) {
            player.sendMessage(Text.of("<c>You can only use the Portable Washer in the Garden!"));
            return;
        }
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        long now = System.currentTimeMillis();
        if (data.getSprays().values().stream().noneMatch(spray -> spray.until() > now)) {
            player.sendMessage(Text.of("<c>None of your plots are sprayed!"));
            return;
        }
        TimedConsumables.consumeHeld(player, item);
        data.getSprays().clear();
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>You washed every <a>Sprayonator <a>effect off your Garden!"));
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
