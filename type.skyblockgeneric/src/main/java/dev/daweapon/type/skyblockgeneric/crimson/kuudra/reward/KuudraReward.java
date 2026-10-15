package dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.item.ItemLore;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;

/** One line of a Kuudra chest: an item (maybe an enchanted book) or an amount of Crimson Essence. */
public record KuudraReward(@Nullable String itemId, int amount, @Nullable String enchantment, int enchantmentLevel,
                           long essence) {
    public static final String CRIMSON_ESSENCE_HEAD = "67c41930f8ff0f2b0430e169ae5f38e984df1244215705c6f173862844543e9d";

    public static KuudraReward item(String itemId, int amount) {
        return new KuudraReward(itemId, amount, null, 0, 0);
    }

    public static KuudraReward book(String enchantment, int level) {
        return new KuudraReward(ItemType.ENCHANTED_BOOK.name(), 1, enchantment, level, 0);
    }

    public static KuudraReward essence(long amount) {
        return new KuudraReward(null, 1, null, 0, amount);
    }

    public boolean isEssence() {
        return essence > 0;
    }

    public @Nullable SkyBlockItem createItem() {
        if (isEssence() || itemId == null) return null;
        ItemType type = ItemType.get(itemId);
        if (type == null) return null;
        SkyBlockItem item = new SkyBlockItem(type, amount);
        if (enchantment != null) {
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.valueOf(enchantment), enchantmentLevel));
        }
        return item;
    }

    public Text name() {
        if (isEssence()) return Text.of("<d>Crimson Essence <8>x{}", StringUtility.commaify(essence));
        SkyBlockItem item = createItem();
        if (item == null) return Text.of("<7>{}", itemId);
        Text name = item.getDisplayNameText();
        List<Text> enchantments = item.getAttributeHandler().getEnchantments().map(ItemLore::enchantmentTitle).toList();
        if (!enchantments.isEmpty()) name = Text.of("{} <7>({}<7>)", name, Text.join(Text.of("<7>, "), enchantments));
        return amount > 1 ? Text.of("{} <8>x{}", name, amount) : name;
    }

    public ItemStack.Builder icon(SkyBlockPlayer player) {
        if (isEssence()) {
            return ItemStacks.head(CRIMSON_ESSENCE_HEAD, Text.of("<d>Crimson Essence <8>x{}", StringUtility.commaify(essence)),
                    List.of(Text.of("<7>An essence used to upgrade Crimson"), Text.of("<7>Isle gear and buy Vulcan's perks.")));
        }
        SkyBlockItem item = createItem();
        if (item == null) return ItemStacks.named(net.minestom.server.item.Material.BARRIER, Text.of("<c>{}", itemId));
        return PlayerItemUpdater.playerUpdate(player, item.getItemStack()).amount(Math.max(1, Math.min(64, amount)));
    }

    public void deliver(SkyBlockPlayer player) {
        if (isEssence()) {
            Essences.add(player, EssenceType.CRIMSON, CombatPetHooks.crimsonEssence(player, essence));
            return;
        }
        SkyBlockItem item = createItem();
        if (item != null) player.addAndUpdateItem(item);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (itemId != null) json.put("item", itemId);
        if (amount > 1) json.put("amount", amount);
        if (enchantment != null) json.put("enchantment", enchantment).put("level", enchantmentLevel);
        if (essence > 0) json.put("essence", essence);
        return json;
    }

    public static KuudraReward fromJson(JSONObject json) {
        return new KuudraReward(json.optString("item", null), json.optInt("amount", 1),
                json.optString("enchantment", null), json.optInt("level", 0), json.optLong("essence", 0));
    }
}
