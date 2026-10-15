package dev.daweapon.type.hub.gui.maxwell;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import dev.daweapon.type.skyblockgeneric.accessory.power.PowerScaling;
import dev.daweapon.type.skyblockgeneric.accessory.power.StatText;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.List;

final class MaxwellItems {
    static final String ACCESSORY_BAG_TEXTURE = "961a918c0c49ba8d053e522cb91abc74689367b4d8aa06bfc1ba9154730985ff";

    private MaxwellItems() {
    }

    static ItemStack.Builder icon(AccessoryPowerType power) {
        ItemType stone = power.stone();
        if (stone == null) return ItemStack.builder(power.getIcon());
        return ItemStacks.copy(new SkyBlockItem(stone).getItemStack());
    }

    static ItemStack.Builder selectedIcon() {
        return ItemStack.builder(Material.LIME_STAINED_GLASS);
    }

    static ItemStack.Builder withText(ItemStack.Builder builder, String text) {
        return ItemStacks.clearAttributes(ItemStacks.text(builder, text.replace("{", "(").replace("}", ")")));
    }

    static String stats(AccessoryPowerType power, int accessoryPower) {
        StringBuilder text = new StringBuilder("<7>Stats:\n");
        for (String line : StatText.lines(PowerScaling.scaled(power, accessoryPower))) text.append(line).append('\n');
        if (!power.getUniqueBonus().isEmpty()) {
            text.append("\n<7>Unique Power Bonus:\n");
            for (String line : StatText.lines(power.getUniqueBonus())) text.append(line).append('\n');
        }
        return text.toString();
    }

    static String stoneName(AccessoryPowerType power) {
        ItemType stone = power.stone();
        if (stone == null) return "<7>None";
        Rarity rarity = new SkyBlockItem(stone).getAttributeHandler().getRarity();
        return "<color:" + rarity.getColor().asHexString() + ">" + stone.getDisplayName();
    }

    static String accessoryPower(int accessoryPower) {
        return "<7>You have: <6>" + StringUtility.commaify(accessoryPower) + " Accessory Power";
    }

    static ItemStack.Builder noPower(boolean selected) {
        ItemStack.Builder builder = ItemStacks.item(selected ? Material.LIME_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE, """
                <a>No Power
                <8>Starter Power

                <7>Decline to use any power, as you
                <7>dread the responsibilities and
                <7>expectations that come with better
                <7>stats.

                """ + (selected ? "<a>Power is selected!" : "<e>Click to select power!"));
        return selected ? ItemStacks.enchanted(builder) : builder;
    }

    static List<Rarity> breakdownRarities() {
        return List.of(Rarity.DIVINE, Rarity.MYTHIC, Rarity.LEGENDARY, Rarity.EPIC, Rarity.RARE, Rarity.UNCOMMON,
                Rarity.COMMON, Rarity.SPECIAL, Rarity.VERY_SPECIAL);
    }
}
