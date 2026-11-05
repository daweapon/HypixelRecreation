package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import dev.daweapon.dungeons.catacombs.classes.DungeonClassScaling;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonClassProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * The Dungeon Classes menu from Mort's Catacombs Gate and the run's waiting room, laid out like the wiki's Mort UI:
 * four rows, the five classes in row two, Go Back and Close in row four. Picking a class saves it on the profile.
 */
public class GUIDungeonClasses extends StatelessView {
    private static final int FIRST_CLASS_SLOT = 11;
    private static final int BACK_SLOT = 30;
    private static final int CLOSE_SLOT = 31;
    private static final String DOUBLED = """
            <a>All positive bonus stats are doubled if
            <a>only 1 player uses this class in a dungeon.""";

    private final BiConsumer<SkyBlockPlayer, DungeonClassType> onSelect;

    public GUIDungeonClasses() {
        this((_, _) -> {
        });
    }

    public GUIDungeonClasses(BiConsumer<SkyBlockPlayer, DungeonClassType> onSelect) {
        this.onSelect = onSelect;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dungeon Classes", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, BACK_SLOT, ctx);
        Components.close(layout, CLOSE_SLOT);
        DungeonClassType[] types = DungeonClassType.values();
        for (int index = 0; index < types.length; index++) {
            DungeonClassType type = types[index];
            layout.slot(FIRST_CLASS_SLOT + index, (s, c) -> classItem((SkyBlockPlayer) c.player(), type), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (DungeonClassProgress.selected(player) == type) {
                    player.sendMessage("<c>You already have the " + type.displayName() + " class selected!");
                    return;
                }
                DungeonClassProgress.select(player, type);
                player.sendMessage("<a>You have selected the <e>" + type.displayName() + " <a>Dungeon Class!");
                onSelect.accept(player, type);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    public static ItemStack.Builder classItem(SkyBlockPlayer player, DungeonClassType type) {
        int level = DungeonClassProgress.level(player, type);
        boolean selected = DungeonClassProgress.selected(player) == type;
        String body = "<7>[Lvl " + level + "] <a>" + type.displayName() + "\n"
                + bonuses(type, level)
                + abilityList(type) + "\n\n"
                + DOUBLED + "\n\n"
                + (selected ? "<a>SELECTED" : "<e>Click to select!");
        ItemStack.Builder builder = ItemStacks.item(icon(type), body);
        if (type == DungeonClassType.HEALER) builder.set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.HEALING));
        return selected ? ItemStacks.enchanted(builder) : builder;
    }

    public static Material icon(DungeonClassType type) {
        return switch (type) {
            case HEALER -> Material.SPLASH_POTION;
            case MAGE -> Material.BLAZE_ROD;
            case BERSERK -> Material.IRON_SWORD;
            case ARCHER -> Material.BOW;
            case TANK -> Material.LEATHER_CHESTPLATE;
        };
    }

    private static String bonuses(DungeonClassType type, int level) {
        return switch (type) {
            case HEALER -> "";
            case MAGE -> "<7>Intelligence: <a>+" + whole(DungeonClassScaling.mageIntelligence(level, 1)) + "\n\n";
            case BERSERK -> "<7>Melee Damage: <c>+" + whole(DungeonClassScaling.berserkMeleePercent(level, 1)) + "%\n\n"
                    + "<7>Walk Speed: <a>+" + whole(DungeonClassScaling.berserkSpeed(level, 1)) + "\n\n";
            case ARCHER -> "<7>Arrow Damage: <c>+" + whole(DungeonClassScaling.archerArrowPercent(level, 1)) + "%\n"
                    + "<7>Melee Damage: <c>-" + DungeonClassScaling.ARCHER_MELEE_PENALTY_PERCENT + "%\n\n";
            case TANK -> "<7>Health: <a>+" + whole(DungeonClassScaling.tankHealth(1)) + "\n"
                    + "<7>Defense: <a>+" + whole(DungeonClassScaling.tankDefense(level, 1)) + "\n\n";
        };
    }

    private static String abilityList(DungeonClassType type) {
        List<String> passives = switch (type) {
            case HEALER -> List.of("Renew", "Healing Aura", "Revive", "Orbies", "Soul Tether", "Overheal");
            case MAGE -> List.of("Mage Staff", "Efficient Spells");
            case BERSERK -> List.of("Bloodlust", "Lust for Blood", "Indomitable", "Weapon Master");
            case ARCHER -> List.of("Doubleshot", "Bone Plating", "Bouncy Arrows");
            case TANK -> List.of("Protective Barrier", "Taunt", "Diversion", "Defensive Stance");
        };
        List<String> orb = switch (type) {
            case HEALER -> List.of("Healing Circle", "Wish");
            case MAGE -> List.of("Guided Sheep", "Thunderstorm");
            case BERSERK -> List.of("Throwing Axe", "Ragnarok");
            case ARCHER -> List.of("Explosive Shot", "Machine Gun Bow");
            case TANK -> List.of("Seismic Wave", "Castle of Stone");
        };
        List<String> ghost = switch (type) {
            case HEALER -> List.of("Healing Potion", "Revive Self");
            case MAGE -> List.of("Pop-up Wall", "Fireball");
            case BERSERK -> List.of("Strength Potion", "Ghost Axe");
            case ARCHER -> List.of("Stun Bow", "Healing Bow");
            case TANK -> List.of("Stun Potion", "Absorption Potion");
        };
        return "<f><l>Class Passives\n" + bullets(passives, "<a>")
                + "\n\n<f><l>Dungeon Orb Abilities\n" + bullets(orb, "<6>")
                + "\n\n<f><l>Ghost Abilities\n" + bullets(ghost, "<f>");
    }

    private static String bullets(List<String> names, String colour) {
        return String.join("\n", names.stream().map(name -> "<8>∙ " + colour + name).toList());
    }

    private static String whole(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }
}
