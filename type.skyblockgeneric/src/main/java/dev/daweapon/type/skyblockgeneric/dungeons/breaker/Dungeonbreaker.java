package dev.daweapon.type.skyblockgeneric.dungeons.breaker;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeDungeonbreakerCharges;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShops;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

/**
 * The Dungeonbreaker pickaxe: its charges live on the item, show in its lore, and the Echoes of the Lost perk gives
 * some back for every secret found. Breaking blocks with it is up to the Catacombs.
 */
public final class Dungeonbreaker {
    public static final String LORE_HANDLER = "DUNGEONBREAKER";
    public static final String ECHOES_OF_THE_LOST = "dungeonbreaker_secrets";
    public static final CatacombsFloor REQUIRED_FLOOR = CatacombsFloor.FLOOR_THREE;
    private static final String ATTRIBUTE = "dungeonbreaker_charges";

    private Dungeonbreaker() {
    }

    public static void registerLore() {
        LoreRegistry.register(LORE_HANDLER, new LoreConfig((item, player) -> lore(charges(item, System.currentTimeMillis()).charges()),
                null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static List<String> lore(int charges) {
        return List.of("<7>Charges: <e>" + charges + "<7>/<e>" + BreakerCharges.MAX + "<c>⸕");
    }

    public static boolean is(ItemStack stack) {
        if (stack == null || stack.material() != Material.DIAMOND_PICKAXE || !SkyBlockItem.isSkyBlockItem(stack)) return false;
        return new SkyBlockItem(stack).getAttributeHandler().getPotentialType() == ItemType.DUNGEONBREAKER;
    }

    public static boolean canUse(SkyBlockPlayer player) {
        return CatacombsProgress.hasCompleted(player, REQUIRED_FLOOR);
    }

    public static BreakerCharges charges(SkyBlockItem item, long now) {
        ItemAttributeDungeonbreakerCharges attribute = (ItemAttributeDungeonbreakerCharges) item.getAttribute(ATTRIBUTE);
        return BreakerCharges.decode(attribute == null ? null : attribute.getValue(), now).at(now);
    }

    private static void store(SkyBlockItem item, BreakerCharges charges) {
        ItemAttributeDungeonbreakerCharges attribute = (ItemAttributeDungeonbreakerCharges) item.getAttribute(ATTRIBUTE);
        if (attribute != null) attribute.setValue(charges.encode());
    }

    /** Spends one charge of the held Dungeonbreaker; false when it is not held or has none left. */
    public static boolean spendHeld(SkyBlockPlayer player, long now) {
        if (!is(player.getItemInMainHand())) return false;
        int slot = player.getHeldSlot();
        BreakerCharges spent = charges(new SkyBlockItem(player.getItemInMainHand()), now).spend(now);
        if (spent == null) return false;
        player.updateItemInSlot(slot, item -> store(item, spent));
        return true;
    }

    public static int heldCharges(SkyBlockPlayer player, long now) {
        if (!is(player.getItemInMainHand())) return 0;
        return charges(new SkyBlockItem(player.getItemInMainHand()), now).charges();
    }

    /** Echoes of the Lost: the finder's Dungeonbreaker gets its perk value in charges back. */
    public static void secretFound(SkyBlockPlayer player, long now) {
        int amount = (int) Math.round(echoesOfTheLost(player));
        if (amount <= 0) return;
        int slot = slotOf(player);
        if (slot < 0) return;
        player.updateItemInSlot(slot, item -> store(item, charges(item, now).restore(amount, now)));
    }

    /** Brings the lore of every Dungeonbreaker the player carries up to its regenerated charge count. */
    public static void refresh(SkyBlockPlayer player, long now) {
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (!is(stack)) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            ItemAttributeDungeonbreakerCharges attribute = (ItemAttributeDungeonbreakerCharges) item.getAttribute(ATTRIBUTE);
            BreakerCharges stored = BreakerCharges.decode(attribute == null ? null : attribute.getValue(), now);
            BreakerCharges current = stored.at(now);
            if (current.charges() == stored.charges()) continue;
            player.updateItemInSlot(slot, update -> store(update, current));
        }
    }

    public static double echoesOfTheLost(SkyBlockPlayer player) {
        return EssenceShops.shop(EssenceType.WITHER)
                .flatMap(shop -> shop.perks().stream().filter(perk -> ECHOES_OF_THE_LOST.equals(perk.id())).findFirst())
                .map(perk -> EssencePerks.value(player, perk))
                .orElse(0D);
    }

    private static int slotOf(SkyBlockPlayer player) {
        if (is(player.getItemInMainHand())) return player.getHeldSlot();
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            if (is(player.getInventory().getItemStack(slot))) return slot;
        }
        return -1;
    }
}
