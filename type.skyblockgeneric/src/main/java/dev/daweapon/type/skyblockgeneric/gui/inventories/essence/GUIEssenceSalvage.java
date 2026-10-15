package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.essence.EssenceCrafting;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

// Essence Crafting > Salvage Items: break eligible gear down into essence (and half its star cost).
public class GUIEssenceSalvage extends EssenceItemSlotGUI {
    public GUIEssenceSalvage() {
        super("Salvage Items");
    }

    @Override
    protected boolean accepts(SkyBlockItem item) {
        return EssenceCrafting.canSalvage(item);
    }

    @Override
    protected ItemStack.Builder actionButton(SkyBlockPlayer player, @Nullable SkyBlockItem item) {
        if (item == null) {
            return ItemStacks.item(Material.ANVIL, """
                    <a>Salvage Items
                    <7>Place an item above to salvage it
                    <7>into <d>Essence<7>. Upgraded items
                    <7>refund half of their star costs.""");
        }
        return EssenceCrafting.preview(player, item).map(result -> {
            StringBuilder lore = new StringBuilder("<a>Salvage Item\n<7>You will receive:\n");
            lore.append("<d>").append(StringUtility.commaify(result.amount())).append(" ")
                    .append(result.essence().getFullName()).append("\n");
            result.items().forEach((type, amount) -> lore.append("<a>").append(type.getDisplayName())
                    .append(" <8>x").append(amount).append("\n"));
            lore.append("\n<c>The item will be destroyed!\n\n<e>Click to salvage!");
            return ItemStacks.item(Material.ANVIL, lore.toString());
        }).orElseGet(() -> ItemStacks.item(Material.BARRIER, "<c>Cannot Salvage\n<7>This item can't be salvaged."));
    }

    @Override
    protected @Nullable SkyBlockItem act(SkyBlockPlayer player, SkyBlockItem item) {
        return EssenceCrafting.salvage(player, item).map(result -> {
            player.sendMessage("<a>You salvaged your " + item.getDisplayName() + " <a>for <d>"
                    + StringUtility.commaify(result.amount()) + " " + result.essence().getFullName() + "<a>!");
            return (SkyBlockItem) null;
        }).orElse(item);
    }
}
