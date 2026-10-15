package dev.daweapon.type.skyblockgeneric.drill;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeDrillData.DrillData;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HeatService;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteOre;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.DrillComponent;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

public final class DrillFuel {
    public static final int DEFAULT_BLOCK_COST = 1;
    public static final int GEMSTONE_BLOCK_COST = 2;
    public static final int GLACITE_BLOCK_COST = 3;
    private static final int ACTION_BAR_TICKS = 30;
    private static final int ACTION_BAR_PRIORITY = 3;
    private static final Map<UUID, Integer> REGULATION_PROGRESS = new ConcurrentHashMap<>();

    private DrillFuel() {
    }

    public static boolean isDrill(@Nullable SkyBlockItem item) {
        return item != null && !item.isAir() && item.hasComponent(DrillComponent.class);
    }

    public static void fillHeld(SkyBlockPlayer player) {
        if (!isDrill(held(player))) {
            player.sendMessage("<c>Hold a Drill to fill its tank!");
            return;
        }
        player.updateItemInSlot(player.getHeldSlot(), item -> {
            DrillData data = item.getAttributeHandler().getDrillData();
            item.getAttributeHandler().setDrillData(fill(data));
        });
        player.sendMessage("<a>Your Drill's tank is full!");
    }

    public static boolean hasDrill(SkyBlockPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (isDrill(new SkyBlockItem(player.getInventory().getItemStack(slot)))) return true;
        }
        return false;
    }

    public static boolean isEmpty(@Nullable SkyBlockItem item) {
        return isDrill(item) && item.getAttributeHandler().getDrillData().fuel() <= 0;
    }

    public static double heldCooldownReductionPercent(SkyBlockPlayer player) {
        SkyBlockItem held = held(player);
        return isDrill(held) ? DrillParts.cooldownReductionPercent(held.getAttributeHandler().getDrillData()) : 0;
    }

    public static int heldPerkLevelBonus(SkyBlockPlayer player) {
        SkyBlockItem held = held(player);
        return isDrill(held) ? DrillParts.perkLevels(held.getAttributeHandler().getDrillData()) : 0;
    }

    private static SkyBlockItem held(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
    }

    public static int capacity(DrillData data) {
        DrillPart tank = DrillParts.get(data.tank());
        return tank == null ? DrillData.BASE_FUEL : tank.maxFuel();
    }

    public static int baseBlockCost(@Nullable Material material, @Nullable RegionType region) {
        GlaciteOre ore = material == null ? null : GlaciteOre.of(material, region);
        if (ore == null) return DEFAULT_BLOCK_COST;
        return switch (ore) {
            case UMBER, TUNGSTEN, GLACITE -> GLACITE_BLOCK_COST;
            case AQUAMARINE, ONYX, CITRINE, PERIDOT -> GEMSTONE_BLOCK_COST;
        };
    }

    public static int blockCost(int baseCost, DrillData data) {
        return baseCost * DrillParts.fuelMultiplier(data);
    }

    public static boolean savesFuel(DrillData data, RandomGenerator random) {
        double chance = DrillParts.fuelSaveChance(data);
        return chance > 0 && random.nextDouble() * 100 < chance;
    }

    public static DrillData consume(DrillData data, int cost) {
        return data.withFuel(Math.max(0, data.fuel() - Math.max(0, cost)));
    }

    public static DrillData fill(DrillData data) {
        return data.withFuel(capacity(data));
    }

    public static int itemsToFill(DrillData data, int fuelPerItem, int available) {
        if (fuelPerItem <= 0 || available <= 0) return 0;
        int room = capacity(data) - data.fuel();
        if (room <= 0) return 0;
        return Math.min(available, (room + fuelPerItem - 1) / fuelPerItem);
    }

    public static DrillData refuel(DrillData data, int fuelPerItem, int items) {
        long total = (long) fuelPerItem * items;
        return data.withFuel((int) Math.min(capacity(data), data.fuel() + total));
    }

    public static int regulationSteps(int fuelUsed, int previousRemainder, int fuelPerStep) {
        if (fuelPerStep <= 0) return 0;
        return (previousRemainder + fuelUsed) / fuelPerStep;
    }

    public static void onBlockMined(SkyBlockPlayer player, SkyBlockItem held, Material material, @Nullable RegionType region,
                                    RandomGenerator random) {
        if (!isDrill(held)) return;
        DrillData data = held.getAttributeHandler().getDrillData();
        if (data.fuel() <= 0 || savesFuel(data, random)) {
            return;
        }
        int cost = Math.min(data.fuel(), blockCost(baseBlockCost(material, region), data));
        DrillData after = consume(data, cost);
        player.updateItemInSlot(player.getHeldSlot(), item -> item.getAttributeHandler().setDrillData(after));
        regulate(player, data, cost);
        showFuel(player, after);
        if (after.fuel() <= 0) warnEmpty(player);
    }

    private static void regulate(SkyBlockPlayer player, DrillData data, int cost) {
        int perStep = DrillParts.fuelPerRegulation(data);
        if (perStep <= 0) return;
        int previous = REGULATION_PROGRESS.getOrDefault(player.getUuid(), 0);
        int steps = regulationSteps(cost, previous, perStep);
        REGULATION_PROGRESS.put(player.getUuid(), (previous + cost) % perStep);
        if (steps <= 0) return;
        HeatService.reduce(player, steps);
        ColdService.addCold(player, steps);
    }

    public static void showFuel(SkyBlockPlayer player, DrillData data) {
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE,
                Text.of("<2>{}/{} Drill Fuel", format(data.fuel()), formatCapacity(capacity(data))),
                ACTION_BAR_TICKS, ACTION_BAR_PRIORITY);
    }

    public static void warnEmpty(SkyBlockPlayer player) {
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE,
                Text.of("<c>Your Drill is out of fuel!"), ACTION_BAR_TICKS, ACTION_BAR_PRIORITY + 1);
    }

    public static String format(int fuel) {
        return NumberFormat.getIntegerInstance(Locale.US).format(fuel);
    }

    public static String formatCapacity(int capacity) {
        return capacity % 1000 == 0 ? capacity / 1000 + "k" : format(capacity);
    }
}
