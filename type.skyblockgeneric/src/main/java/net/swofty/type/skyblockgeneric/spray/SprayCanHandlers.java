package net.swofty.type.skyblockgeneric.spray;

import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.place.PlaceEventHandler;

/** Item callbacks for the Spray Can, registered under the handler id {@value #HANDLER_ID}. */
public final class SprayCanHandlers {
    public static final String HANDLER_ID = "SPRAY_CAN";

    private SprayCanHandlers() {}

    public static InteractableItemConfig interactable() {
        return InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> {
                    if (CanvasRoomPainter.isInCanvasRoom(player)) CanvasRoomPainter.paint(player);
                })
                .leftClickHandler((player, item) -> {
                    if (CanvasRoomPainter.isInCanvasRoom(player)) CanvasRoomPainter.pickColour(player);
                })
                .build();
    }

    public static PlaceEventHandler place() {
        return (event, player, item) -> {
            event.setCancelled(true);
            if (CanvasRoomPainter.isInCanvasRoom(player)) CanvasRoomPainter.paint(player);
        };
    }
}
