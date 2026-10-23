package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramEditing {
    private static final Map<UUID, Selection> SELECTIONS = new ConcurrentHashMap<>();

    private HologramEditing() {
    }

    static void select(SkyBlockPlayer player, PlacedFurniture hologram, int line) {
        SELECTIONS.put(player.getUuid(), new Selection(hologram.x(), hologram.y(), hologram.z(), line));
    }

    static void clear(SkyBlockPlayer player, PlacedFurniture hologram) {
        Selection selection = SELECTIONS.get(player.getUuid());
        if (selection != null && selection.matches(hologram)) SELECTIONS.remove(player.getUuid());
    }

    public static void forget(SkyBlockPlayer player) {
        SELECTIONS.remove(player.getUuid());
    }

    public static boolean apply(SkyBlockPlayer player, String text) {
        Selection selection = SELECTIONS.get(player.getUuid());
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        if (selection == null || island == null || island != player.getSkyBlockIsland() || island.getFurniture() == null) {
            player.sendMessage("<c>Right-click one of your Holograms and pick a line to edit first!");
            return false;
        }
        IslandFurniture furniture = island.getFurniture();
        PlacedFurniture hologram = furniture.at(selection.x, selection.y, selection.z);
        if (hologram == null || FurnitureCatalog.get(hologram.itemId()).interaction() != FurnitureInteraction.HOLOGRAM) {
            SELECTIONS.remove(player.getUuid());
            player.sendMessage("<c>That Hologram no longer exists!");
            return false;
        }
        String line = text.equalsIgnoreCase("clear") ? null : text;
        if (line != null && line.length() > HologramSettings.MAX_LINE_LENGTH) {
            player.sendMessage("<c>A line can have at most {} characters, including colour codes!", HologramSettings.MAX_LINE_LENGTH);
            return false;
        }
        HologramSettings updated = HologramSettings.decode(hologram.data()).withLine(selection.line, line);
        furniture.replace(player.getInstance(), hologram, hologram.withData(updated.encode()));
        player.sendMessage(line == null ? "<a>Cleared line {}." : "<a>Set line {}.", selection.line + 1);
        return true;
    }

    private record Selection(int x, int y, int z, int line) {
        boolean matches(PlacedFurniture hologram) {
            return x == hologram.x() && y == hologram.y() && z == hologram.z();
        }
    }
}
