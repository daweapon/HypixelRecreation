package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseEffect;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseRules;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationBehaviour;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationBehaviours;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.PlantType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICropDiagnostics extends StatelessView {
    private final GreenhouseLayout.Spot spot;

    public GUICropDiagnostics(GreenhouseLayout.Spot spot) {
        this.spot = spot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Crop Diagnostics"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(4, (_, c) -> identity((SkyBlockPlayer) c.player()));
        layout.slot(10, (_, c) -> growth((SkyBlockPlayer) c.player()));
        layout.slot(12, (_, c) -> water((SkyBlockPlayer) c.player()));
        layout.slot(14, (_, c) -> applies((SkyBlockPlayer) c.player()));
        layout.slot(16, (_, c) -> receives((SkyBlockPlayer) c.player()));
        layout.slot(22, (_, c) -> decay((SkyBlockPlayer) c.player()));
    }

    private GreenhouseData.Plant plant(SkyBlockPlayer player) {
        GreenhouseData.Bed bed = GreenhouseService.data(player).bed(spot.plot());
        return bed == null ? null : bed.plantAt(spot.slot());
    }

    private PlantType type(GreenhouseData.Plant plant) {
        return plant == null ? null : GreenhouseConfig.get().plant(plant.getType());
    }

    private ItemStack.Builder missing() {
        return ItemStacks.item(Material.BARRIER, Text.of("<c>No crop"), List.of(Text.of("<7>This plant is gone.")));
    }

    private ItemStack.Builder identity(SkyBlockPlayer player) {
        GreenhouseData.Plant plant = plant(player);
        PlantType type = type(plant);
        if (type == null) return missing();
        return ItemStacks.item(type.baseCrop() ? GardenMenus.icon(type.crop()) : Material.FERN, Text.of("<a>" + type.name()), List.of(
                Text.of(type.mutation() ? "<5>Mutation Crop" : "<8>Base Crop"),
                Text.of("<7>Size: <a>{}x{}", type.footprint(), type.footprint()),
                Text.of("<7>Growth Surface: <a>{}", type.surface()),
                Text.of("<7>Growth Stages: <a>{}", type.stages())));
    }

    private ItemStack.Builder growth(SkyBlockPlayer player) {
        GreenhouseData.Plant plant = plant(player);
        PlantType type = type(plant);
        if (type == null) return missing();
        GreenhouseData data = GreenhouseService.data(player);
        long now = System.currentTimeMillis();
        boolean grown = plant.grown(type);
        MutationBehaviour.Ctx ctx = new MutationBehaviour.Ctx(player, null, spot.plot(), GreenhouseService.data(player).bed(spot.plot()), spot.slot(), plant, type, now);
        String status = MutationBehaviours.of(type.id()).status(ctx);
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Age: <a>{}", span(now - plant.getPlantedAt())),
                Text.of("<7>Stage: <a>{}", grown ? "FULLY GROWN" : plant.getStage() + "/" + type.stages()),
                Text.of("<7>Next Stage: <a>{}", grown ? "FULLY GROWN" : span(GreenhouseService.nextStageIn(data))),
                Text.empty(),
                Text.of("<7>Shows how much the crop has grown.")));
        if (status != null) lore.add(3, Text.of("<7>Status: <e>{}", status));
        return ItemStacks.item(Material.OAK_SAPLING, Text.of("<a>Growth Stage"), lore);
    }

    private ItemStack.Builder water(SkyBlockPlayer player) {
        GreenhouseData.Plant plant = plant(player);
        PlantType type = type(plant);
        if (type == null) return missing();
        boolean needs = !plant.grown(type) && MutationBehaviours.of(type.id()).needsWater();
        return ItemStacks.item(Material.WATER_BUCKET, Text.of("<b>Water Level"), List.of(
                Text.of("<7>Water: <b>{}<7>/<b>100", Math.round(plant.getWater())),
                Text.of(needs ? "<7>This crop needs water to grow." : "<7>This crop doesn't need water!"),
                Text.empty(),
                Text.of("<7>Shows how much water the current crop has,"),
                Text.of("<7>ranging between <c>-100 <7>(dead) and <a>100 <7>(fully watered).")));
    }

    private ItemStack.Builder applies(SkyBlockPlayer player) {
        PlantType type = type(plant(player));
        if (type == null) return missing();
        List<Text> lore = new ArrayList<>(List.of(Text.of("<7>Applies Effects:")));
        if (type.effects().isEmpty()) lore.add(Text.of("<8>None"));
        for (GreenhouseEffect effect : type.effects()) {
            lore.add(Text.of("<8>> {}{}<7>: {}", effect.isNegative() ? "<c>" : "<a>", effect.getDisplayName(), effect.getDescription()));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Shows what effects this crop applies to"));
        lore.add(Text.of("<7>adjacent crops."));
        return ItemStacks.item(Material.BEACON, Text.of("<a>Applies Effects"), lore);
    }

    private ItemStack.Builder receives(SkyBlockPlayer player) {
        GreenhouseData.Bed bed = GreenhouseService.data(player).bed(spot.plot());
        if (bed == null) return missing();
        List<GreenhouseEffect> effects = GreenhouseRules.appliedEffects(bed, GreenhouseConfig.get()).getOrDefault(spot.slot(), List.of());
        List<Text> lore = new ArrayList<>(List.of(Text.of("<7>Received Effects:")));
        if (effects.isEmpty()) lore.add(Text.of("<8>None"));
        GreenhouseRules.counts(effects).forEach((effect, count) ->
                lore.add(Text.of("<8>> {}{}{}", effect.isNegative() ? "<c>" : "<a>", effect.getDisplayName(), count > 1 ? " <8>x" + count : "")));
        return ItemStacks.item(Material.NETHER_STAR, Text.of("<a>Received Effects"), lore);
    }

    private ItemStack.Builder decay(SkyBlockPlayer player) {
        GreenhouseData.Plant plant = plant(player);
        if (plant == null) return missing();
        GreenhouseConfig config = GreenhouseConfig.get();
        MutationCatalogue.Mutation mutation = MutationCatalogue.get().mutation(plant.getType());
        if (mutation != null && mutation.neverDecays()) {
            return ItemStacks.item(Material.CLOCK, Text.of("<c>Decay Information"), List.of(Text.of("<7>Decay: <a>Never"), Text.empty(), Text.of("<7>This mutation never decays.")));
        }
        boolean waterable = MutationBehaviours.waterable(plant.getType());
        long left = waterable
                ? plant.getLastWateredAt() + config.decayDaysSinceWatering() * 86_400_000L - System.currentTimeMillis()
                : plant.getPlantedAt() + config.decayDaysUnwaterable() * 86_400_000L - System.currentTimeMillis();
        return ItemStacks.item(Material.CLOCK, Text.of("<c>Decay Information"), List.of(
                Text.of("<7>Decay: <c>{}", left <= 0 ? "Decayed" : span(left)),
                Text.empty(),
                waterable ? Text.of("<7>A crop decays {} days after it was", config.decayDaysSinceWatering()) : Text.of("<7>A crop that cannot be watered decays {}", config.decayDaysUnwaterable()),
                Text.of(waterable ? "<7>last watered." : "<7>days after it was planted.")));
    }

    private static String span(long millis) {
        long seconds = Math.max(0, millis / 1000);
        long days = seconds / 86400;
        long hours = seconds % 86400 / 3600;
        long minutes = seconds % 3600 / 60;
        long rest = seconds % 60;
        if (days > 0) return days + "d " + hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h " + minutes + "m " + rest + "s";
        if (minutes > 0) return minutes + "m " + rest + "s";
        return rest + "s";
    }
}
