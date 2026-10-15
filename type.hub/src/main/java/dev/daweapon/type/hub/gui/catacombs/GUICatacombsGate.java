package dev.daweapon.type.hub.gui.catacombs;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.hub.util.UnreleasedContent;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GUICatacombsGate extends StatelessView {
    public static final int REQUIRED_COMBAT_LEVEL = 15;

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

        for (Floor floor : Floor.values()) {
            layout.slot(floor.slot, (s, c) -> floorItem((SkyBlockPlayer) c.player(), floor), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (combatLevel(player) < REQUIRED_COMBAT_LEVEL) {
                    player.closeInventory();
                    onLowCombat.accept(player);
                    return;
                }
                player.sendMessage(UnreleasedContent.DUNGEONS.message());
            });
        }

        UnreleasedContent.DUNGEONS.slot(layout, 40, Material.TRIPWIRE_HOOK, """
                <c>Master Mode
                <8><italic>Like normal Dungeons... but
                <8><italic>more hardcore.""");
        UnreleasedContent.DUNGEONS.slot(layout, 45, Material.NETHER_STAR, """
                <a>Dungeon Classes
                <7>View and select a dungeon class.""");
        UnreleasedContent.DUNGEONS.slot(layout, 48, Material.REDSTONE_BLOCK, """
                <a>Find a Party
                <7>Use the Party Finder to join a
                <7>party queued for the dungeon.""");
        UnreleasedContent.DUNGEONS.slot(layout, 50, Material.RED_STAINED_GLASS, """
                <a>Toggle Auto Ready Up
                <7>Auto ready up when joining <a>The
                <a>Catacombs <7>if you have the
                <7>requirements, so you don't have
                <7>to!

                <7>Currently: <c>Disabled!""");
        UnreleasedContent.DUNGEONS.slot(layout, 52, Material.OAK_SIGN, """
                <a>Catacombs Profile
                <7>View your statistics, best
                <7>performances, and more for <c>The
                <c>Catacombs<7>!""");
        UnreleasedContent.DUNGEONS.slot(layout, 53, Material.REDSTONE_TORCH, """
                <a>Dungeon Rules and Tips
                <7>SkyBlock Dungeons has special
                <7>rules! Some things inside
                <7>Dungeons work differently than
                <7>they do in regular SkyBlock.
                <7>This menu describes the list of
                <7>changes!""");
    }

    private static int combatLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.COMBAT);
    }

    private ItemStack.Builder floorItem(SkyBlockPlayer player, Floor floor) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Dungeon Size: <b>{}", floor.size));
        lore.add(Text.of("<7>Party size: <9>2-5"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>{}: <c>{}", floor == Floor.ENTRANCE ? "Mini-Boss" : "Boss", floor.boss));
        lore.add(Text.of("<8>{}", floor.bossTitle));
        lore.addAll(Text.of(floor.description).lines());
        lore.add(Text.empty());
        lore.add(Text.of("<7>Requires: " + floor.requirement));
        lore.add(Text.empty());
        if (combatLevel(player) < REQUIRED_COMBAT_LEVEL) {
            lore.add(Text.of("<c>Combat level too low!"));
        } else if (floor == Floor.ENTRANCE) {
            lore.add(Text.of("<e>Click to queue for dungeon!"));
        } else {
            lore.add(Text.of("<c>Catacombs level too low!"));
        }
        lore.addAll(UnreleasedContent.DUNGEONS.loreLines());
        return ItemStacks.item(floor.material, Text.of("<a>The Catacombs <8>- <e>{}", floor.displayName), lore);
    }

    private enum Floor {
        ENTRANCE(11, "Entrance", Material.ZOMBIE_HEAD, "Tiny", "The Watcher", "Stalker",
                "<7>This strange creature is roaming the\n<7>Catacombs to add powerful adventurers to\n<7>its collection.",
                "<b>Combat Level XV"),
        FLOOR_I(12, "Floor I", Material.SKELETON_SKULL, "Tiny", "Bonzo", "New Necromancer",
                "<7>Involved in the dark arts due to his\n<7>parents' insistence. Originally worked as\n<7>a Circus clown.",
                "<d>Catacombs Level I"),
        FLOOR_II(13, "Floor II", Material.SKELETON_SKULL, "Small", "Scarf", "Apprentice Necromancer",
                "<7>First of his class. His teacher said he\n<7>will do \"great things\".",
                "<d>Catacombs Level III"),
        FLOOR_III(14, "Floor III", Material.SKELETON_SKULL, "Small", "The Professor", "Professor",
                "<7>Despite his great technique, he failed\n<7>the Masters exam three times. Works from 8\n<7>to 5. Cares about his students.",
                "<d>Catacombs Level V"),
        FLOOR_IV(15, "Floor IV", Material.SKELETON_SKULL, "Small", "Thorn", "Shaman Necromancer",
                "<7>Powerful Necromancer that specializes in\n<7>animals. Calls himself a vegetarian, go\n<7>figure.",
                "<d>Catacombs Level IX"),
        FLOOR_V(21, "Floor V", Material.SKELETON_SKULL, "Medium", "Livid", "Master Necromancer",
                "<7>Strongly believes he will become the\n<7>Lord one day. Subject of mockeries, even\n<7>from his disciples.",
                "<d>Catacombs Level XIV"),
        FLOOR_VI(22, "Floor VI", Material.SKELETON_SKULL, "Medium", "Sadan", "Necromancer Lord",
                "<7>Necromancy was always strong in his\n<7>family. Says he once beat a Wither in a\n<7>duel. Likes to brag.",
                "<d>Catacombs Level XIX"),
        FLOOR_VII(23, "Floor VII", Material.WITHER_SKELETON_SKULL, "Large", "Maxor, Storm, Goldor and Necron", "The Wither Lords",
                "<7>Disciples of the Wither King. Inherited\n<7>the Catacombs eons ago. Never defeated,\n<7>feared by anything living AND dead.",
                "<d>Catacombs Level XXIV");

        private final int slot;
        private final String displayName;
        private final Material material;
        private final String size;
        private final String boss;
        private final String bossTitle;
        private final String description;
        private final String requirement;

        Floor(int slot, String displayName, Material material, String size, String boss, String bossTitle,
              String description, String requirement) {
            this.slot = slot;
            this.displayName = displayName;
            this.material = material;
            this.size = size;
            this.boss = boss;
            this.bossTitle = bossTitle;
            this.description = description;
            this.requirement = requirement;
        }
    }
}
