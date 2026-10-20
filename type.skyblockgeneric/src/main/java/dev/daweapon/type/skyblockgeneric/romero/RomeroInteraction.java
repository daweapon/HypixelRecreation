package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Set;

public final class RomeroInteraction {
    public static final int INTELLIGENCE_REQUIRED = 1291;
    public static final int POPPIES_REQUIRED = 15;
    public static final int EMERALDS_REQUIRED = 64;
    public static final int RABBIT_LEVEL_REQUIRED = 6;
    private static final Set<String> COFFEE_BASES = Set.of("CHEAP_COFFEE", "DECENT_COFFEE", "BLACK_COFFEE");
    private static final Set<Material> FLOWERS = Set.of(Material.DANDELION, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP,
            Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.SUNFLOWER, Material.LILAC,
            Material.ROSE_BUSH, Material.PEONY, Material.WITHER_ROSE);

    public record Response(String key, boolean completes) {
    }

    private RomeroInteraction() {
    }

    public static String heldId(SkyBlockPlayer player) {
        ItemStack stack = player.getItemInMainHand();
        if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return "";
        return new SkyBlockItem(stack).getAttributeHandler().getTypeAsString();
    }

    public static ItemType heldType(SkyBlockPlayer player) {
        String id = heldId(player);
        return id.isEmpty() ? null : ItemType.get(id);
    }

    private static int heldAmount(SkyBlockPlayer player) {
        return player.getItemInMainHand().amount();
    }

    public static Response respond(SkyBlockPlayer player, RomeroStep step) {
        String id = heldId(player);
        int amount = heldAmount(player);
        String start = "r" + step.getNumber() + "-start";
        String complete = "r" + step.getNumber() + "-complete";
        return switch (step) {
            case ROCK -> id.equals("YELLOW_ROCK") ? new Response(complete, true) : new Response(start, false);
            case BOUQUET -> bouquet(player, id, amount, start, complete);
            case EMERALD_ALTAR -> emeralds(id, amount, start, complete);
            case STEW -> stew(id, start, complete);
            case MOON -> moon(player, id, start, complete);
            case GOLD -> switch (id) {
                case "ENCHANTED_LAVA_BUCKET" -> new Response(complete, true);
                case "FLINT_AND_STEEL" -> new Response("r6-flint", false);
                case "LAVA_BUCKET" -> new Response("r6-lava", false);
                case "MAGICAL_LAVA_BUCKET" -> new Response("r6-magical-lava", false);
                default -> new Response(start, false);
            };
            case PRISM -> prism(player, start, complete);
            case POEM -> switch (id) {
                case "EMERALD_BLADE" -> new Response(complete, true);
                case "ASPECT_OF_THE_JERRY" -> new Response("r8-jerry", false);
                case "EMERALD" -> new Response("r8-emerald", false);
                case "PIGMAN_SWORD", "ASPECT_OF_THE_DRAGON", "ASPECT_OF_THE_DRAGONS" -> new Response("r8-weak-sword", false);
                default -> new Response(start, false);
            };
            case FLOWERS -> id.equals("FLOWER_MINION") ? new Response(complete, true) : new Response(start, false);
            case WEDDING -> wearsTuxedo(player) ? new Response("r10-after-suit", true) : new Response("r10-before-suit", false);
        };
    }

    private static Response bouquet(SkyBlockPlayer player, String id, int amount, String start, String complete) {
        if (id.equals("POPPY")) {
            if (amount >= POPPIES_REQUIRED) return new Response(complete, true);
            if (amount == 1) return new Response("r2-1-poppy", false);
            if (amount <= 7) return new Response("r2-2-7-poppies", false);
            if (amount <= 11) return new Response("r2-8-11-poppies", false);
            return new Response("r2-12-14-poppies", false);
        }
        ItemStack stack = player.getItemInMainHand();
        if (!stack.isAir() && FLOWERS.contains(stack.material())) return new Response("r2-wrong-flower", false);
        return new Response(start, false);
    }

    private static Response emeralds(String id, int amount, String start, String complete) {
        return switch (id) {
            case "EMERALD" -> amount >= EMERALDS_REQUIRED ? new Response(complete, true) : new Response("r3-insufficient", false);
            case "ENCHANTED_EMERALD" -> new Response("r3-enchanted-emerald", false);
            case "ENCHANTED_EMERALD_BLOCK" -> new Response("r3-enchanted-block", false);
            case "EMERALD_BLOCK" -> new Response("r3-block", false);
            default -> new Response(start, false);
        };
    }

    private static Response stew(String id, String start, String complete) {
        return switch (id) {
            case "ENCHANTED_RED_MUSHROOM_BLOCK" -> new Response(complete, true);
            case "RED_MUSHROOM" -> new Response("r4-red-mushroom", false);
            case "BROWN_MUSHROOM" -> new Response("r4-brown-mushroom", false);
            case "RED_MUSHROOM_BLOCK" -> new Response("r4-red-block", false);
            case "BROWN_MUSHROOM_BLOCK" -> new Response("r4-brown-block", false);
            case "ENCHANTED_BROWN_MUSHROOM_BLOCK" -> new Response("r4-enchanted-brown-block", false);
            default -> new Response(start, false);
        };
    }

    private static Response moon(SkyBlockPlayer player, String id, String start, String complete) {
        ItemStack stack = player.getItemInMainHand();
        if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return new Response(start, false);
        ItemAttributePotionData.PotionData potion = new SkyBlockItem(stack).getAttributeHandler().getPotionData();
        if (potion == null || potion.getEffectType() == null) return new Response(start, false);
        if (potion.isSplash() || !potion.getEffectType().equals("RABBIT")) return new Response("r5-wrong-potion", false);
        if (potion.getLevel() < RABBIT_LEVEL_REQUIRED) return new Response("r5-rabbit-not-6", false);
        if (potion.getBase() == null || !COFFEE_BASES.contains(potion.getBase())) return new Response("r5-no-coffee", false);
        return new Response(complete, true);
    }

    public static double intelligence(SkyBlockPlayer player) {
        return player.getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE);
    }

    private static Response prism(SkyBlockPlayer player, String start, String complete) {
        double intelligence = intelligence(player);
        if (intelligence >= INTELLIGENCE_REQUIRED) return new Response(complete, true);
        if (intelligence >= 1150) return new Response("r7-int-1150", false);
        if (intelligence >= 900) return new Response("r7-int-900", false);
        if (intelligence >= 750) return new Response("r7-int-750", false);
        if (intelligence >= 500) return new Response("r7-int-500", false);
        if (intelligence >= 250) return new Response("r7-int-250", false);
        return new Response(start, false);
    }

    public static boolean wearsTuxedo(SkyBlockPlayer player) {
        SkyBlockItem[] armor = player.getArmor();
        for (String tier : List.of("CHEAP", "FANCY", "ELEGANT")) {
            if (worn(armor[1], tier + "_TUXEDO_CHESTPLATE") && worn(armor[2], tier + "_TUXEDO_LEGGINGS") && worn(armor[3], tier + "_TUXEDO_BOOTS")) return true;
        }
        return false;
    }

    private static boolean worn(SkyBlockItem item, String id) {
        return item != null && !item.isAir() && !item.isNA() && id.equals(item.getAttributeHandler().getTypeAsString());
    }

    public static void consume(SkyBlockPlayer player, RomeroStep step) {
        switch (step) {
            case ROCK -> player.removeItemFromPlayer(ItemType.get("YELLOW_ROCK"), 1);
            case BOUQUET -> player.removeItemFromPlayer(ItemType.get("POPPY"), POPPIES_REQUIRED);
            case EMERALD_ALTAR -> player.removeItemFromPlayer(ItemType.get("EMERALD"), EMERALDS_REQUIRED);
            case STEW -> player.removeItemFromPlayer(ItemType.get("ENCHANTED_RED_MUSHROOM_BLOCK"), 1);
            case GOLD -> player.removeItemFromPlayer(ItemType.get("ENCHANTED_LAVA_BUCKET"), 1);
            case POEM -> player.removeItemFromPlayer(ItemType.get("EMERALD_BLADE"), 1);
            case FLOWERS -> player.removeItemFromPlayer(ItemType.get("FLOWER_MINION"), 1);
            case MOON -> player.setItemInMainHand(ItemStack.AIR);
            case PRISM, WEDDING -> {
            }
        }
    }
}
