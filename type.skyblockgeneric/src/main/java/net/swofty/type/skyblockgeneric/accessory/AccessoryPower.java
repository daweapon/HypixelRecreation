package net.swofty.type.skyblockgeneric.accessory;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AbiphoneComponent;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Accessory Power (Hypixel renamed Magical Power in July 2026). Each counted accessory in the bag
 * adds its effective rarity's value; the Hegemony Artifact counts twice, an Abicase adds one per two
 * Abiphone contacts (the profile's unlocked contacts, or the best phone in the inventory), dungeon accessories count twice inside Dungeons, and imbuing a Rift Prism at
 * Erihann adds a permanent 11 (a Rift Prism still in the bag after that adds nothing more).
 */
public final class AccessoryPower {
    public static final int RIFT_PRISM_IMBUED = 11;

    private AccessoryPower() {
    }

    public static int of(SkyBlockPlayer player) {
        boolean imbued = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.RIFT_PRISM_IMBUED, DatapointBoolean.class).getValue();
        return of(AccessoryOwnership.active(player), abiphoneContacts(player), imbued,
                DungeonAccessoryHook.isInDungeon(player));
    }

    public static int of(List<OwnedAccessory> active, int abiphoneContacts, boolean riftPrismImbued, boolean inDungeon) {
        int total = riftPrismImbued ? RIFT_PRISM_IMBUED : 0;
        for (OwnedAccessory owned : active) {
            if (!owned.inBag() || (riftPrismImbued && owned.type() == ItemType.RIFT_PRISM)) continue;
            total += itemPower(owned.item(), abiphoneContacts, inDungeon);
        }
        return total;
    }

    /** What one accessory adds from the bag, as its tooltip shows it. */
    public static int itemPower(SkyBlockItem item, int abiphoneContacts, boolean inDungeon) {
        if (!item.hasComponent(AccessoryComponent.class)) return 0;
        AccessoryComponent definition = item.getComponent(AccessoryComponent.class);
        if (!definition.getRealm().countsForPower()) return 0;
        int power = item.getAttributeHandler().getEffectiveRarity().getAccessoryPower();
        power = switch (definition.getPowerRule()) {
            case NORMAL -> power;
            case DOUBLE -> power * 2;
            case ABIPHONE_CONTACTS -> power + abiphoneContacts / 2;
        };
        return definition.isDungeon() && inDungeon ? power * 2 : power;
    }

    public static int abiphoneContacts(SkyBlockPlayer player) {
        int best = AbiphoneContactXP.unlocked(player).size();
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (!item.hasComponent(AbiphoneComponent.class)) continue;
            best = Math.max(best, item.getAttributeHandler().getAbiphoneNPCs().size());
        }
        return best;
    }
}
