package dev.daweapon.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import dev.daweapon.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;

import java.util.List;

public class Excalibur implements LuckyBlockWeapon {

    public static final String ID = "excalibur";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Excalibur";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.DIAMOND_SWORD,
                        Text.of("<b>Excalibur"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>+7 Attack Damage"),
                                Text.empty(),
                                Text.of("<8><o>\"The sword of kings.\""),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public double getAttackDamage() {
        return 7.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
