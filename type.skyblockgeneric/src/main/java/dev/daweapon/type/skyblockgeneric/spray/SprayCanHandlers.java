package dev.daweapon.type.skyblockgeneric.spray;

import dev.daweapon.type.skyblockgeneric.gui.inventories.spraycan.GUISprayCan;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.place.PlaceEventHandler;

import java.util.List;

/**
 * Item callbacks for the Spray Can, registered under the handler id {@value #HANDLER_ID}.
 * Left-click opens the Spray Selector (or picks a palette colour in the Canvas Room); right-click on
 * a block sprays the selected spray, or paints when aimed at Marco's canvas.
 */
public final class SprayCanHandlers {
    public static final String HANDLER_ID = "SPRAY_CAN";

    private SprayCanHandlers() {}

    public static InteractableItemConfig interactable() {
        return InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> {
                    if (CanvasRoomPainter.isInCanvasRoom(player)) CanvasRoomPainter.paint(player);
                })
                .leftClickHandler((player, item) -> {
                    if (CanvasRoomPainter.isInCanvasRoom(player) && CanvasRoomPainter.pickColour(player)) return;
                    player.openView(new GUISprayCan(), GUISprayCan.State.forHeldCan(player));
                })
                .build();
    }

    public static PlaceEventHandler place() {
        return (event, player, item) -> {
            event.setCancelled(true);
            if (CanvasRoomPainter.isInCanvasRoom(player) && CanvasRoomPainter.paint(player)) return;
            SprayPlacer.spray(player, item, event.getBlockPosition(), event.getBlockFace());
        };
    }

    public static LoreConfig lore() {
        return new LoreConfig((item, player) -> List.of(
                "<e>Selected Spray: " + SprayCanData.of(item).selected().map(spray -> "<a>" + spray.name()).orElse("<c>NONE"),
                ""), null);
    }
}
