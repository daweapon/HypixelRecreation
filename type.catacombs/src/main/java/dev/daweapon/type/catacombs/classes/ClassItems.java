package dev.daweapon.type.catacombs.classes;

import dev.daweapon.dungeons.catacombs.classes.DungeonClassAbility;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassAbilityType;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassScaling;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * The run-only class items: the Dungeon Orb, the waiting room's class selector and a ghost's toolbar (Haunt and the two
 * class ghost abilities). They are plain tagged stacks, never SkyBlock items, so they are never saved with the profile.
 * Lore is transcribed from the wiki's tooltip module; the ghost potions use vanilla potions because their custom head
 * textures aren't public.
 */
public final class ClassItems {
    public static final String ORB = "ORB";
    public static final String SELECTOR = "SELECTOR";
    public static final String HAUNT = "HAUNT";
    public static final String GHOST_FIRST = "GHOST_0";
    public static final String GHOST_SECOND = "GHOST_1";
    public static final int SELECTOR_SLOT = 0;
    public static final int HAUNT_SLOT = 0;
    public static final int GHOST_FIRST_SLOT = 3;
    public static final int GHOST_SECOND_SLOT = 5;

    private static final Tag<String> TAG = Tag.String("catacombs_class_item");
    private static final String ORB_TEXTURE = "ba49f9de12c813329f38d0af2da0f32f32a6dcd52c3221d19090c256376c418c";
    private static final String HAUNT_TEXTURE = "f1d26142909bde6d9ab136fe8928dd67b662843d6c1813e108842c23f1c793bb";
    private static final DungeonClassRegistry REGISTRY = DungeonClassRegistry.defaults();

    private ClassItems() {
    }

    public static @Nullable String kind(ItemStack stack) {
        return stack == null || stack.isAir() ? null : stack.getTag(TAG);
    }

    public static boolean isClassItem(ItemStack stack) {
        return kind(stack) != null;
    }

    public static ItemStack orb(DungeonClassType type, int level) {
        DungeonClassAbility ultimate = REGISTRY.ability(type, DungeonClassAbilityType.ULTIMATE, 0);
        DungeonClassAbility normal = REGISTRY.ability(type, DungeonClassAbilityType.ORB, 0);
        String body = """
                <6>Dungeon Orb
                <7>When entering a Dungeon, this stone
                <7>adapts to its user and allows them to
                <7>use their class abilities.

                <6>Ability: %s  <e><l>LEFT CLICK
                %s
                <8>Cooldown: <a>%ss

                <6>Ability: %s  <e><l>RIGHT CLICK
                %s
                <8>Cooldown: <a>%ss

                <6><l>LEGENDARY DUNGEON ITEM""".formatted(
                ultimate.displayName(), ultimateLore(type, level), seconds(DungeonClassScaling.cooldownSeconds(ultimate, type, level)),
                normal.displayName(), normalLore(type), seconds(DungeonClassScaling.cooldownSeconds(normal, type, level)));
        return tagged(ItemStacks.head(ORB_TEXTURE, body), ORB);
    }

    public static ItemStack selector(DungeonClassType selected) {
        return tagged(ItemStacks.item(Material.NETHER_STAR, """
                <a>Dungeon Classes
                <7>View and select a dungeon class.

                <a>Currently Selected: <b>%s

                <e>Right-click to open!""".formatted(selected.displayName())), SELECTOR);
    }

    public static ItemStack haunt() {
        return tagged(ItemStacks.head(HAUNT_TEXTURE, """
                <a>Haunt
                <6>Ability: Haunt  <e><l>RIGHT CLICK
                <7>Teleport to an alive player!
                <8>Cooldown: <a>2s

                <a><l>UNCOMMON DUNGEON ITEM"""), HAUNT);
    }

    public static ItemStack ghost(DungeonClassType type, int index) {
        DungeonClassAbility ability = REGISTRY.ability(type, DungeonClassAbilityType.GHOST, index);
        String name = ghostItemName(ability);
        String body = "<a>" + name + "\n<6>Ability: " + ability.displayName() + "  <e><l>RIGHT CLICK\n"
                + ghostLore(ability) + "\n<8>Cooldown: <a>" + ability.cooldownSeconds() + "s\n\n<a><l>UNCOMMON DUNGEON ITEM";
        ItemStack.Builder builder = ItemStacks.item(ghostMaterial(ability), body);
        PotionType potion = ghostPotion(ability);
        if (potion != null) builder.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        if ("INSTANT_WALL".equals(ability.id())) builder = ItemStacks.enchanted(builder);
        return tagged(builder, index == 0 ? GHOST_FIRST : GHOST_SECOND);
    }

    private static ItemStack tagged(ItemStack.Builder builder, String kind) {
        return ItemStacks.notEditable(builder).set(TAG, kind).build();
    }

    private static String ghostItemName(DungeonClassAbility ability) {
        return switch (ability.id()) {
            case "BUFF_POTION" -> "Strength Potion";
            case "INSTANT_WALL" -> "Pop-up Wall";
            case "REVIVE_SELF" -> "Revive Self";
            default -> ability.displayName();
        };
    }

    private static Material ghostMaterial(DungeonClassAbility ability) {
        return switch (ability.id()) {
            case "GHOST_AXE" -> Material.IRON_AXE;
            case "STUN_BOW", "HEALING_BOW" -> Material.BOW;
            case "FIREBALL" -> Material.FIRE_CHARGE;
            case "INSTANT_WALL" -> Material.BRICKS;
            case "REVIVE_SELF" -> Material.TOTEM_OF_UNDYING;
            default -> Material.SPLASH_POTION;
        };
    }

    private static @Nullable PotionType ghostPotion(DungeonClassAbility ability) {
        return switch (ability.id()) {
            case "HEALING_POTION" -> PotionType.HEALING;
            case "BUFF_POTION" -> PotionType.STRENGTH;
            case "STUN_POTION" -> PotionType.SLOWNESS;
            case "ABSORPTION_POTION" -> PotionType.REGENERATION;
            default -> null;
        };
    }

    private static String ghostLore(DungeonClassAbility ability) {
        return switch (ability.id()) {
            case "HEALING_POTION" -> "<7>Throw a potion which heals all teammates in a\n<a>10<7> block radius for <a>10%<7> of their max health\n<7>and <c>+100 Health<7>.";
            case "REVIVE_SELF" -> "<7>Revive yourself after 50 seconds.";
            case "INSTANT_WALL" -> "<7>Create a 5x3 wall at the block you are looking\n<7>at which lasts for <a>10<7> seconds.";
            case "FIREBALL" -> "<7>Shoots a fireball, dealing damage based on\n<7>your Mage level!";
            case "BUFF_POTION" -> "<7>Throw a potion which temporarily gives all\n<7>teammates in a <a>10<7> block radius <c>+30 Strength<7>.";
            case "GHOST_AXE" -> "<7>Throw an Axe, dealing damage based on your\n<7>Berserk level!";
            case "STUN_BOW" -> "<7>Temporarily stuns any non-boss\n<7>dungeon monster for a short period\n<7>of time when shot.";
            case "HEALING_BOW" -> "<7>Shoot a teammate to heal them for\n<a>10%<7> of their max HP and grant them\n<c>+50 Strength<7> for 10 seconds.";
            case "STUN_POTION" -> "<7>Throw a potion which temporarily stuns all\n<7>monsters in a <a>10<7> block radius.";
            case "ABSORPTION_POTION" -> "<7>Throw a potion which gives all teammates in a\n<a>10 <7>block radius <a>200 <7>HP worth of absorption\n<7>for <a>3 <7>seconds.";
            default -> "<7>" + ability.description() + ".";
        };
    }

    private static String ultimateLore(DungeonClassType type, int level) {
        return switch (type) {
            case HEALER -> "<7>Heals everyone in your group to full\n<7>health and grants them a shield for\n<a>"
                    + seconds(DungeonClassScaling.wishShieldPercent(level, 1)) + "% <7>of their maximum health.";
            case MAGE -> "<7>Unleash a Thunderstorm, striking\n<7>enemies in a <a>10<7> block radius for <a>15\n<7>seconds, dealing damage based on\n<7>your Mage level!";
            case BERSERK -> "<7>Grants <e>100 Attack Speed<7>,\n<f>400 Speed <7>and <c>1.5x <7>melee damage\n<7>increase for <b>15 <7>seconds.";
            case ARCHER -> "<7>Shoots <a>5<7> Arrows per second for <a>" + DungeonClassScaling.rapidFireSeconds(level)
                    + "\n<7>seconds! Arrows deal <a>75.0%<7> of your\n<7>highest Bow hit in the last minute.";
            case TANK -> "<7>Fortifies your defence, reducing the\n<7>damage you take by <c>70% <7>for <a>20\n<7>seconds. Also aggros all enemies in\n<7>a <a>10<7> block radius.";
        };
    }

    private static String normalLore(DungeonClassType type) {
        return switch (type) {
            case HEALER -> "<7>Launches a healing orb that creates\n<7>a healing circle where it lands. The\n<7>healing circle heals friendly players\n<7>inside for <a>1% <7>of their health every\n<7>second.";
            case MAGE -> "<7>Shoots a Guided Sheep, dealing\n<7>damage based on your Mage level!";
            case BERSERK -> "<7>Throw an Axe, dealing the same\n<7>damage as your highest hit in the\n<7>last minute.";
            case ARCHER -> "<7>Shoots a volley of 3 Arrows that\n<7>explodes on impact, dealing the same\n<7>damage as your highest bow hit in\n<7>the last minute in a <a>4<7> block radius.";
            case TANK -> "<7>A Seismic Wave emerges from\n<7>underneath you and travels in a\n<7>straight line. Deals <c>20,000 +10%\n<7>every <a>+50 Defense <7>in damage to\n<7>any enemy in its path.";
        };
    }

    private static String seconds(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }
}
