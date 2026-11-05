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
import dev.daweapon.type.skyblockgeneric.item.ItemLore;
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
    private static final String WITHER_ESSENCE_HEAD = "c4db4adfa9bf48ff5d41707ae34ea78bd2371659fcd8cd8934749af4cce9b";
    private static final String UNDEAD_ESSENCE_HEAD = "71d7c816fc8c636d7f50a93a0ba7aaeff06c96a561645e9eb1bef391655c531";

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

    public Text name() {
        if (isEssence()) return Text.of("<d>{} Essence <8>x{}", essenceDisplayName(), StringUtility.commaify(essence));
        SkyBlockItem item = createItem();
        if (item == null) return Text.of("<7>{}", itemId);
        Text name = item.getDisplayNameText();
        List<Text> enchantments = item.getAttributeHandler().getEnchantments().map(ItemLore::enchantmentTitle).toList();
        if (!enchantments.isEmpty()) name = Text.of("{} <7>({}<7>)", name, Text.join(Text.of("<7>, "), enchantments));
        return amount > 1 ? Text.of("{} <8>x{}", name, amount) : name;
    }

    public ItemStack.Builder icon(SkyBlockPlayer player) {
        if (isEssence()) {
            String head = "WITHER".equals(essenceType) ? WITHER_ESSENCE_HEAD : UNDEAD_ESSENCE_HEAD;
            return ItemStacks.head(head, Text.of("<d>{} Essence <8>x{}", essenceDisplayName(), StringUtility.commaify(essence)),
                    List.of(Text.of("<7>An essence used to upgrade and"), Text.of("<7>star dungeon items.")));
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
