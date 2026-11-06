package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

/** One line of a dungeon reward chest: an item (book, pet) or an amount of Wither or Undead Essence. */
public record DungeonReward(@Nullable String itemId, int amount, @Nullable String enchantment, int enchantmentLevel,
                            @Nullable String rarity, @Nullable String essenceType, long essence) {
    private static final String WITHER_ESSENCE_HEAD = "e49ec7d82b1415acae2059f78cd1d1754b9de9b18ca59f609024c4af843d4d24";
    private static final String UNDEAD_ESSENCE_HEAD = "65d9d98ad6d154ef1f4c63dba92a51337cd1f4eb0cb65b9b2a90fe8b03096db7";

    public static DungeonReward item(String itemId, int amount) {
        return new DungeonReward(itemId, amount, null, 0, null, null, 0);
    }

    public static DungeonReward essence(String type, long amount) {
        return new DungeonReward(null, 1, null, 0, null, type, amount);
    }

    public boolean isEssence() {
        return essenceType != null && essence > 0;
    }

    public @Nullable SkyBlockItem createItem() {
        if (isEssence() || itemId == null) return null;
        ItemType type = ItemType.get(itemId);
        if (type == null) return null;
        SkyBlockItem item = new SkyBlockItem(type, amount);
        if (enchantment != null) {
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.valueOf(enchantment), enchantmentLevel));
        }
        if (rarity != null) item.getAttributeHandler().setRarity(Rarity.valueOf(rarity));
        return item;
    }

    /**
     * The line a chest's Contents lore shows, as captured: {@code §fEnchanted Book (Infinite Quiver VI§f)} for a book
     * (an ultimate enchantment in {@code §d§l}), the item's own coloured name otherwise, essence as
     * {@code §dWither Essence §8x9}.
     */
    public Text name() {
        if (isEssence()) return Text.of("<d>{} Essence <8>x{}", essenceDisplayName(), StringUtility.commaify(essence));
        SkyBlockItem item = createItem();
        if (item == null) return Text.of("<7>{}", itemId);
        List<SkyBlockEnchantment> enchantments = item.getAttributeHandler().getEnchantments().toList();
        Text name;
        if (enchantments.isEmpty()) {
            name = item.getDisplayNameText();
        } else {
            List<Text> titles = enchantments.stream().map(DungeonReward::contentsTitle).toList();
            Text base = "ENCHANTED_BOOK".equals(itemId) ? Text.of("<f>Enchanted Book") : item.getDisplayNameText();
            name = Text.of("{} <f>({}<f>)", base, Text.join(Text.of("<f>, "), titles));
        }
        return amount > 1 ? Text.of("{} <8>x{}", name, amount) : name;
    }

    private static Text contentsTitle(SkyBlockEnchantment enchantment) {
        String format = enchantment.type().getEnch().isUltimate() ? "<d><l>{} {:roman}" : "<f>{} {:roman}";
        return Text.of(format, Text.parseLenient(enchantment.type().getName()), enchantment.level());
    }

    public ItemStack.Builder icon(SkyBlockPlayer player) {
        if (isEssence()) {
            String head = "WITHER".equals(essenceType) ? WITHER_ESSENCE_HEAD : UNDEAD_ESSENCE_HEAD;
            String type = essenceDisplayName();
            return ItemStacks.head(head, Text.of("<d>{} Essence <8>x{}", type, StringUtility.commaify(essence)),
                    List.of(Text.of("<7>{} Essence can be used to", type), Text.of("<7>convert some items into Dungeon"),
                            Text.of("<7>items and upgrade them!")));
        }
        SkyBlockItem item = createItem();
        if (item == null) return ItemStacks.named(Material.BARRIER, Text.of("<c>{}", itemId));
        return PlayerItemUpdater.playerUpdate(player, item.getItemStack()).amount(Math.max(1, Math.min(64, amount)));
    }

    public void deliver(SkyBlockPlayer player, boolean masterMode) {
        if (isEssence()) {
            Essences.add(player, EssenceType.valueOf(essenceType), essence);
            return;
        }
        SkyBlockItem item = createItem();
        if (item == null) return;
        DungeonChestUpgrades.apply(player.getUuid(), item, masterMode);
        player.addAndUpdateItem(item);
    }

    private String essenceDisplayName() {
        return EssenceType.valueOf(essenceType).getDisplayName();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (itemId != null) json.put("item", itemId);
        if (amount > 1) json.put("amount", amount);
        if (enchantment != null) json.put("enchantment", enchantment).put("level", enchantmentLevel);
        if (rarity != null) json.put("rarity", rarity);
        if (essenceType != null) json.put("essenceType", essenceType).put("essence", essence);
        return json;
    }

    public static DungeonReward fromJson(JSONObject json) {
        return new DungeonReward(json.optString("item", null), json.optInt("amount", 1),
                json.optString("enchantment", null), json.optInt("level", 0), json.optString("rarity", null),
                json.optString("essenceType", null), json.optLong("essence", 0));
    }
}
