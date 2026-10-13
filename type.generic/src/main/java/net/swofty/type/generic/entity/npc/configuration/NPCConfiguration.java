package net.swofty.type.generic.entity.npc.configuration;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface NPCConfiguration {

    default String[] holograms(HypixelPlayer player) {
        return new String[0];
    }

    default List<Text> hologramTexts(HypixelPlayer player) {
        return Arrays.stream(holograms(player))
                .map(Text::read)
                .toList();
    }

    Pos position(HypixelPlayer player);

    /** Where the NPC first appears for the player; a {@link #dynamicPosition()} NPC then makes its way to {@link #position}. */
    default Pos spawnPosition(HypixelPlayer player) {
        return position(player);
    }

    /**
     * Whether {@link #position(HypixelPlayer)} can return a different value across calls for an
     * already-spawned NPC (e.g. an NPC that relocates based on external state). Defaults to false
     * since most NPCs have a fixed home position; returning true here causes the NPC's already-spawned
     * entity to be re-teleported whenever its computed position changes and it isn't mid-walk.
     */
    default boolean dynamicPosition() {
        return false;
    }

    /** Whether a {@link #dynamicPosition()} NPC walks to its new position while the player is watching instead of teleporting. */
    default boolean walksToNewPosition() {
        return false;
    }

    default boolean looking(HypixelPlayer player) {
        return false;
    }

    default boolean visible(HypixelPlayer player) {
        return true;
    }

    @Nullable
    default String chatName(HypixelPlayer player) {
        return null;
    }

    @Nullable
    default Text chatNameText(HypixelPlayer player) {
        String name = chatName(player);
        return name == null ? null : Text.read(name);
    }

    default Instance instance() {
        return HypixelConst.getInstanceContainer();
    }

    /** The instance this player's copy of the NPC lives in; private worlds override this to return the player's own world. */
    default Instance instance(HypixelPlayer player) {
        return instance();
    }

    default EntityPose pose(HypixelPlayer player) {
        return EntityPose.STANDING;
    }

    default boolean shouldDisplayHolograms(HypixelPlayer player) {
        return true;
    }

    @Nullable
    default Map<EquipmentSlot, ItemStack> equipment(HypixelPlayer player) {
        return null;
    }
}
