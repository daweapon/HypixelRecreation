package dev.daweapon.type.skyblockgeneric.item.handlers.place;

public final class InfiniTorchPlacement {
    public static final String HANDLER_ID = "INFINI_TORCH_PLACE";
    public static final double COST_PER_TORCH = 2;

    private InfiniTorchPlacement() {
    }

    public static boolean canAfford(double coins) {
        return coins >= COST_PER_TORCH;
    }

    public static PlaceEventHandler handler() {
        return (event, player, item) -> {
            if (!canAfford(player.getCoins())) {
                player.sendMessage("<c>You need <6>2 coins <c>to place a torch!");
                event.setCancelled(true);
                return;
            }
            player.removeCoins(COST_PER_TORCH);
            event.consumeBlock(false);
        };
    }
}
