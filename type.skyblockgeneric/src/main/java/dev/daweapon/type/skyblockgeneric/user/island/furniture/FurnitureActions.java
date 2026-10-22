package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIAnvil;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIEnchantmentTable;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.GUICrafting;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;

final class FurnitureActions {
    private FurnitureActions() {
    }

    static void interact(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed, boolean sneaking) {
        SkyBlockIsland island = SkyBlockIsland.forInstance(player.getInstance());
        if (island == null) return;
        boolean member = island == player.getSkyBlockIsland();
        FurnitureDefinition definition = FurnitureCatalog.get(placed.itemId());

        if (sneaking && member) {
            player.openView(new FurnitureMenuView(owner, placed));
            return;
        }

        switch (definition.interaction()) {
            case SIT -> sit(owner, player, placed, definition);
            case BED -> setSpawn(owner, player, placed, member);
            case TOGGLE -> toggle(owner, player, placed, member);
            case MINION_CHAIR -> minionChair(owner, player, placed, member);
            case CRAFTING -> player.openView(new GUICrafting());
            case ENCHANTING -> new GUIEnchantmentTable().open(player);
            case ANVIL -> new GUIAnvil().open(player);
            case CARPENTRY -> openCarpentryTable(owner, player, placed, member);
            case NONE -> {
                if (member) player.sendMessage("<7>Sneak and right-click to pick up or rotate this furniture.");
            }
        }
    }

    private static void sit(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed, FurnitureDefinition definition) {
        SpawnedFurniture spawned = owner.spawnedOf(placed);
        if (spawned == null) return;
        if (!FurnitureEntities.sit(player.getInstance(), player, placed, definition, spawned)) {
            player.sendMessage("<c>This seat is taken!");
            return;
        }
        player.sendMessage("<7>Sneak to stand up.");
    }

    private static void setSpawn(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed, boolean member) {
        if (!member) {
            player.sendMessage("<c>Only the island's members can use this bed!");
            return;
        }
        Pos spawn = new Pos(placed.x() + 0.5, placed.y() + 1, placed.z() + 0.5, placed.yaw(), 0);
        owner.setSpawnPoint(spawn);
        player.setRespawnPoint(spawn);
        player.sendMessage("<a>Your island spawn point has been set to this bed!");
    }

    private static void toggle(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed, boolean member) {
        if (!member) return;
        boolean on = FurnitureEntities.TOGGLED_ON.equals(placed.data());
        owner.replace(player.getInstance(), placed, placed.withData(on ? null : FurnitureEntities.TOGGLED_ON));
        player.sendMessage(on ? "<7>You put out the flames." : "<6>You lit the flames!");
    }

    private static void minionChair(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed, boolean member) {
        if (!member) return;
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        boolean holdingMinion = !held.isAir() && held.hasComponent(MinionComponent.class);
        if (!holdingMinion && placed.data() == null) {
            player.sendMessage("<7>Right-click with a minion to display it on this chair.");
            return;
        }
        if (placed.data() != null) {
            SkyBlockItem previous = FurnitureEntities.minionItem(placed.data());
            if (previous != null) player.addAndUpdateItem(previous);
        }
        String stored = null;
        if (holdingMinion) {
            stored = FurnitureEntities.minionData(held.getAttributeHandler().getMinionType(), held.getAttributeHandler().getMinionData().tier());
            ItemStack hand = player.getItemInMainHand();
            player.setItemInMainHand(hand.amount() > 1 ? hand.withAmount(hand.amount() - 1) : ItemStack.AIR);
            player.sendMessage("<a>You placed your minion on the chair! It will not work, and does not count towards your minion limit.");
        } else {
            player.sendMessage("<a>You took the minion off the chair.");
        }
        owner.replace(player.getInstance(), placed, placed.withData(stored));
    }

    private static void openCarpentryTable(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed, boolean member) {
        if (!member) {
            player.sendMessage("<c>Only the island's members can use this table!");
            return;
        }
        player.openView(new GUICarpentryTable(owner, placed));
    }

    static void pickUp(IslandFurniture owner, SkyBlockPlayer player, PlacedFurniture placed) {
        owner.remove(placed);
        player.addAndUpdateItem(dev.daweapon.commons.skyblock.item.ItemType.get(placed.itemId()));
        if (placed.data() != null && FurnitureCatalog.get(placed.itemId()).interaction() == FurnitureInteraction.MINION_CHAIR) {
            SkyBlockItem minion = FurnitureEntities.minionItem(placed.data());
            if (minion != null) player.addAndUpdateItem(minion);
        }
    }
}
