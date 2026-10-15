package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsAvailability;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsRequirements;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonsUnreleased;
import dev.daweapon.type.skyblockgeneric.dungeons.queue.CatacombsPartyCheck;
import dev.daweapon.type.skyblockgeneric.dungeons.queue.CatacombsQueue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Mort's Catacombs Gate. Entrance and Floor I queue the party; the other floors, Master Mode, classes and the
 * profile and rules menus stay locked until they exist. Shared by the Hub and Dungeon Hub Morts.
 */
public class GUICatacombsGate extends StatelessView {
    private final Consumer<SkyBlockPlayer> onLowCombat;

    public GUICatacombsGate(Consumer<SkyBlockPlayer> onLowCombat) {
        this.onLowCombat = onLowCombat;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Catacombs Gate", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        for (CatacombsFloorInfo info : CatacombsFloorInfo.values()) {
            layout.slot(info.slot(), (s, c) -> floorItem((SkyBlockPlayer) c.player(), info), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (CatacombsRequirements.combatLevel(player) < CatacombsRequirements.ENTRANCE_COMBAT_LEVEL) {
                    player.closeInventory();
                    onLowCombat.accept(player);
                    return;
                }
                if (CatacombsAvailability.blockIfUnavailable(player, info.floor())) return;
                if (!CatacombsRequirements.meets(player, info.floor())) {
                    player.sendMessage("<c>You don't meet the requirements for " + info.floor().displayName() + "!");
                    return;
                }
                player.closeInventory();
                CatacombsQueue.queue(player, info.floor());
            });
        }

        DungeonsUnreleased.slot(layout, 40, Material.TRIPWIRE_HOOK, """
                <c>Master Mode
                <8><italic>Like normal Dungeons... but
                <8><italic>more hardcore.""");
        DungeonsUnreleased.slot(layout, 45, Material.NETHER_STAR, """
                <a>Dungeon Classes
                <7>View and select a dungeon class.""");
        layout.slot(48, ItemStacks.item(Material.REDSTONE_BLOCK, """
                <a>Find a Party
                <7>Use the Party Finder to join a
                <7>party queued for the dungeon.

                <e>Click to open!"""), (click, c) -> GUICatacombsPartyFinder.open((SkyBlockPlayer) c.player()));
        DungeonsUnreleased.slot(layout, 50, Material.RED_STAINED_GLASS, """
                <a>Toggle Auto Ready Up
                <7>Auto ready up when joining <a>The
                <a>Catacombs <7>if you have the
                <7>requirements, so you don't have
                <7>to!

                <7>Currently: <c>Disabled!""");
        DungeonsUnreleased.slot(layout, 52, Material.OAK_SIGN, """
                <a>Catacombs Profile
                <7>View your statistics, best
                <7>performances, and more for <c>The
                <c>Catacombs<7>!""");
        DungeonsUnreleased.slot(layout, 53, Material.REDSTONE_TORCH, """
                <a>Dungeon Rules and Tips
                <7>SkyBlock Dungeons has special
                <7>rules! Some things inside
                <7>Dungeons work differently than
                <7>they do in regular SkyBlock.
                <7>This menu describes the list of
                <7>changes!""");
    }

    private ItemStack.Builder floorItem(SkyBlockPlayer player, CatacombsFloorInfo info) {
        CatacombsFloor floor = info.floor();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Dungeon Size: <b>{}", CatacombsFloorInfo.size(floor)));
        lore.add(Text.of("<7>Party size: <9>{}-{}", CatacombsPartyCheck.MIN_PARTY_SIZE, CatacombsPartyCheck.MAX_PARTY_SIZE));
        lore.add(Text.empty());
        lore.add(Text.of("<7>{}: <c>{}", floor == CatacombsFloor.ENTRANCE ? "Mini-Boss" : "Boss", floor.bossName()));
        lore.add(Text.of("<8>{}", info.bossTitle()));
        lore.addAll(Text.of(info.description()).lines());
        lore.add(Text.empty());
        lore.add(Text.of("<7>Requires: " + info.requirement()));
        lore.add(Text.empty());
        if (CatacombsRequirements.combatLevel(player) < CatacombsRequirements.ENTRANCE_COMBAT_LEVEL) {
            lore.add(Text.of("<c>Combat level too low!"));
        } else if (!CatacombsRequirements.meets(CatacombsRequirements.combatLevel(player), CatacombsProgress.level(player), floor, CatacombsMode.NORMAL)) {
            lore.add(Text.of("<c>Catacombs level too low!"));
        } else if (CatacombsAvailability.canQueue(player, floor)) {
            lore.add(Text.of("<e>Click to queue for dungeon!"));
        }
        CatacombsAvailability.floorLore(player, floor).stream().map(Text::of).forEach(lore::add);
        return ItemStacks.item(info.material(), Text.of("<a>The Catacombs <8>- <e>{}", floor.displayName()), lore);
    }
}
