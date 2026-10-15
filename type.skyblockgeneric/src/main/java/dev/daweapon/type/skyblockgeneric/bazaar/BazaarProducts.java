package dev.daweapon.type.skyblockgeneric.bazaar;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Resolves Bazaar product ids to products and lists the enchantment and essence products.
// The enchantment levels sold are the ones the real Bazaar has live orders for (bazaar/enchantment_products.txt).
public final class BazaarProducts {
    private static final String ENCHANTMENT_RESOURCE = "/bazaar/enchantment_products.txt";
    private static final Pattern ENCHANTMENT_ID = Pattern.compile("ENCHANTMENT_(.+)_(\\d+)");
    private static final Map<ItemType, ItemProduct> ITEMS = new ConcurrentHashMap<>();
    private static final List<EssenceProduct> ESSENCES = List.of(
            essence(EssenceType.WITHER), essence(EssenceType.SPIDER), essence(EssenceType.UNDEAD), essence(EssenceType.DRAGON),
            essence(EssenceType.GOLD), essence(EssenceType.DIAMOND), essence(EssenceType.ICE), essence(EssenceType.CRIMSON),
            essence(EssenceType.FOREST), essence(EssenceType.FOSSIL), essence(EssenceType.SAFARI));

    private BazaarProducts() {
    }

    private static EssenceProduct essence(EssenceType type) {
        return new EssenceProduct(type);
    }

    public static ItemProduct of(ItemType type) {
        return ITEMS.computeIfAbsent(type, ItemProduct::new);
    }

    public static List<EssenceProduct> essences() {
        return ESSENCES;
    }

    public static @Nullable BazaarProduct byId(String id) {
        if (id == null) {
            return null;
        }
        ItemType type = ItemType.get(id);
        if (type != null && type.name().equals(id)) {
            return of(type);
        }
        if (id.startsWith(EssenceProduct.ID_PREFIX)) {
            return ESSENCES.stream().filter(product -> product.id().equals(id)).findFirst().orElse(null);
        }
        return Enchantments.BY_ID.get(id);
    }

    public static @Nullable BazaarProduct fromStack(ItemStack stack) {
        if (stack.isAir()) {
            return null;
        }
        SkyBlockItem item = new SkyBlockItem(stack);
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type == null) {
            return null;
        }
        if (type != ItemType.ENCHANTED_BOOK) {
            return of(type);
        }
        List<SkyBlockEnchantment> enchantments = item.getAttributeHandler().getEnchantments().toList();
        if (enchantments.size() != 1) {
            return null;
        }
        return Enchantments.BY_KEY_LEVEL.get(enchantments.getFirst().type().name() + "#" + enchantments.getFirst().level());
    }

    public static List<EnchantmentProduct> enchantments() {
        return Enchantments.ALL;
    }

    public static List<BazaarProductGroup> enchantmentGroups(boolean ultimate) {
        return ultimate ? Enchantments.ULTIMATE_GROUPS : Enchantments.NORMAL_GROUPS;
    }

    public static @Nullable BazaarProductGroup groupOf(EnchantmentProduct product) {
        return Enchantments.GROUP_BY_PRODUCT.get(product.id());
    }

    public static @Nullable EnchantmentProduct enchantment(String id) {
        return Enchantments.BY_ID.get(id);
    }

    public static @Nullable EnchantmentProduct parseEnchantment(String id) {
        Matcher matcher = ENCHANTMENT_ID.matcher(id);
        if (!matcher.matches()) {
            return null;
        }
        CatalogueEntry entry = EnchantmentCatalogue.findByNeuId(matcher.group(1));
        return entry == null ? null : new EnchantmentProduct(matcher.group(1), entry, Integer.parseInt(matcher.group(2)));
    }

    private static final class Enchantments {
        private static final List<EnchantmentProduct> ALL = load();
        private static final Map<String, EnchantmentProduct> BY_ID = index();
        private static final Map<String, EnchantmentProduct> BY_KEY_LEVEL = keyIndex();
        private static final List<BazaarProductGroup> NORMAL_GROUPS = groups(false);
        private static final List<BazaarProductGroup> ULTIMATE_GROUPS = groups(true);
        private static final Map<String, BazaarProductGroup> GROUP_BY_PRODUCT = groupIndex();

        private static List<EnchantmentProduct> load() {
            List<EnchantmentProduct> products = new ArrayList<>();
            try (InputStream stream = BazaarProducts.class.getResourceAsStream(ENCHANTMENT_RESOURCE)) {
                if (stream == null) {
                    throw new IllegalStateException("Missing " + ENCHANTMENT_RESOURCE);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    EnchantmentProduct product = line.isBlank() ? null : parseEnchantment(line.trim());
                    if (product != null) {
                        products.add(product);
                    }
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Could not read " + ENCHANTMENT_RESOURCE, exception);
            }
            return List.copyOf(products);
        }

        private static Map<String, EnchantmentProduct> index() {
            Map<String, EnchantmentProduct> index = new LinkedHashMap<>();
            ALL.forEach(product -> index.put(product.id(), product));
            return Map.copyOf(index);
        }

        private static Map<String, EnchantmentProduct> keyIndex() {
            Map<String, EnchantmentProduct> index = new LinkedHashMap<>();
            ALL.forEach(product -> index.put(product.enchantment().key() + "#" + product.level(), product));
            return Map.copyOf(index);
        }

        private static List<BazaarProductGroup> groups(boolean ultimate) {
            Map<String, TreeMap<Integer, EnchantmentProduct>> byEnchantment = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (EnchantmentProduct product : ALL) {
                if (product.ultimate() == ultimate) {
                    byEnchantment.computeIfAbsent(product.enchantment().name(), name -> new TreeMap<>())
                            .put(product.level(), product);
                }
            }
            List<BazaarProductGroup> groups = new ArrayList<>();
            byEnchantment.forEach((name, levels) -> {
                List<BazaarProduct> run = new ArrayList<>();
                int first = 0;
                int last = 0;
                for (EnchantmentProduct product : levels.values()) {
                    if (!run.isEmpty() && product.level() != last + 1) {
                        groups.add(new BazaarProductGroup(name, first, last, List.copyOf(run)));
                        run.clear();
                    }
                    if (run.isEmpty()) {
                        first = product.level();
                    }
                    run.add(product);
                    last = product.level();
                }
                groups.add(new BazaarProductGroup(name, first, last, List.copyOf(run)));
            });
            return List.copyOf(groups);
        }

        private static Map<String, BazaarProductGroup> groupIndex() {
            Map<String, BazaarProductGroup> index = new LinkedHashMap<>();
            for (BazaarProductGroup group : NORMAL_GROUPS) {
                group.products().forEach(product -> index.put(product.id(), group));
            }
            for (BazaarProductGroup group : ULTIMATE_GROUPS) {
                group.products().forEach(product -> index.put(product.id(), group));
            }
            return Map.copyOf(index);
        }
    }
}
