package net.swofty.type.skyblockgeneric.carnival;

/** One player's running Carnival game; the game's tool forwards its clicks here. */
public interface CarnivalGameSession {
    CarnivalGame game();

    default void onLeftClick() {}

    default void onRightClick() {}

    /** Ends the game early without rewards, e.g. when the player leaves. */
    void abandon();
}
