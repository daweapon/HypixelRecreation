package net.swofty.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.essence.EssenceCrafting;
import net.swofty.type.skyblockgeneric.essence.EssenceStars;
import net.swofty.type.skyblockgeneric.essence.EssenceUpgrades;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

// Essence Crafting > Upgrade Item: spend essence, coins and materials to add a star to an item.
public class GUIEssenceUpgrade extends EssenceItemSlotGUI {
    public GUIEssenceUpgrade() {
        super("Upgrade Item");
    }

    @Override
    protected boolean accepts(SkyBlockItem item) {
        return EssenceUpgrades.upgradeFor(item.getAttributeHandler().getPotentialType()).isPresent() && item.getAmount() == 1;
    }

    @Override
    protected ItemStack.Builder actionButton(SkyBlockPlayer player, @Nullable SkyBlockItem item) {
        if (item == null) {
            return ItemStacks.item(Material.ANVIL, """
                    <a>Upgrade Item
                    <7>Place an item above to upgrade it
                    <7>using <d>Essence<7>. Each star grants
                    <a>+2% <7>of the item's stats.""");
        }
        var upgrade = EssenceUpgrades.upgradeFor(item.getAttributeHandler().getPotentialType());
        if (upgrade.isEmpty() || item.getAmount() != 1) {
            return ItemStacks.item(Material.BARRIER, "<c>Cannot Upgrade\n<7>This item can't be upgraded with\n<7>essence.");
        }
        int stars = item.getAttributeHandler().getUpgradeLevel();
        if (stars >= upgrade.get().maxStars()) {
            return ItemStacks.item(Material.BARRIER, "<a>Maximum Stars\n<7>This item already has every star.");
        }
        EssenceUpgrades.Star next = upgrade.get().star(stars + 1);
        StringBuilder lore = new StringBuilder("<a>Upgrade Item\n<7>Upgrade to " + EssenceStars.display(stars + 1) + "\n\n<7>Cost\n");
        lore.append("<d>").append(StringUtility.commaify(next.essence())).append(" ")
                .append(upgrade.get().essence().getFullName()).append(" <8>(")
                .append(StringUtility.commaify(Essences.get(player, upgrade.get().essence()))).append(")\n");
        if (next.coins() > 0) lore.append("<6>").append(StringUtility.commaify(next.coins())).append(" Coins\n");
        for (Map.Entry<String, Integer> entry : next.items().entrySet()) {
            ItemType type = ItemType.get(entry.getKey());
            String name = type == null ? StringUtility.toNormalCase(entry.getKey()) : type.getDisplayName();
            lore.append(type == null ? "<8>" : "<a>").append(name).append(" <8>x").append(entry.getValue()).append("\n");
        }
        lore.append("\n").append(switch (EssenceCrafting.check(player, item)) {
            case UPGRADED -> "<e>Click to upgrade!";
            case TOO_POOR -> "<c>You don't have the required items!";
            case MISSING_ITEMS_IN_SERVER -> "<c>A required material isn't in this server yet.";
            default -> "<c>This item can't be upgraded.";
        });
        return ItemStacks.item(Material.ANVIL, lore.toString());
    }

    @Override
    protected SkyBlockItem act(SkyBlockPlayer player, SkyBlockItem item) {
        switch (EssenceCrafting.upgrade(player, item)) {
            case UPGRADED -> player.sendMessage("<a>You upgraded your " + item.getDisplayName() + " <a>to "
                    + EssenceStars.display(item.getAttributeHandler().getUpgradeLevel()) + "<a>!");
            case TOO_POOR -> player.sendMessage("<c>You don't have the required items to upgrade this!");
            case MISSING_ITEMS_IN_SERVER -> player.sendMessage("<c>A required material isn't in this server yet.");
            case MAXED -> player.sendMessage("<c>This item already has every star!");
            case NOT_UPGRADABLE -> player.sendMessage("<c>This item can't be upgraded with essence!");
        }
        return item;
    }
}
