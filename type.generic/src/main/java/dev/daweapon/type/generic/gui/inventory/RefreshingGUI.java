package dev.daweapon.type.generic.gui.inventory;

import dev.daweapon.type.generic.user.HypixelPlayer;

@Deprecated
public interface RefreshingGUI {
    /**
     * If the GUI implements this method, this is the method that should be using in setting all the items
     * This is called async
     */
    void refreshItems(HypixelPlayer player);

    /**
     * How long between each refresh (ticks)
     *
     * @return time in ticks
     */
    int refreshRate();
}