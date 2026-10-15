package dev.daweapon.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.bedwarsgame.shop.impl.AxeShopItem;
import dev.daweapon.type.bedwarsgame.shop.impl.PickaxeShopItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;

// Lucky Block reward that grants the opener a permanent axe or pickaxe upgrade.
public class PermanentToolLuckyReward extends LuckyReward {
    private final boolean axe;

    public PermanentToolLuckyReward(String name, boolean axe) {
        super(name);
        this.axe = axe;
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        if (axe) {
            new AxeShopItem().onPurchase(player);
        } else {
            new PickaxeShopItem().onPurchase(player);
        }
    }
}
