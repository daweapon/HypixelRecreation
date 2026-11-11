package dev.daweapon.type.skyblockgeneric.carnival;

import java.util.List;

/** One player's running Carnival game; the game's tool forwards its clicks here. */
public interface CarnivalGameSession {
    CarnivalGame game();

    default void onLeftClick() {}

    default void onRightClick() {}

    /** The game's block on the sidebar, which takes the purse's place while it runs. */
    List<String> sidebarLines();

    /** Ends the game early without rewards, e.g. when the player leaves. */
    void abandon();

    /** The player clicked Leave Game: the game ends as a fail. */
    default void leave() {
        abandon();
    }
}
