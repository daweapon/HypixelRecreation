package dev.daweapon.type.catacombs.engine.look;

import com.google.gson.JsonObject;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

/**
 * One piece a dungeon mob wears or holds, as the client saw it on Hypixel: the vanilla material, leather colour, head
 * texture, glint and Hypixel item model. The SkyBlock item id is kept for reference only; mobs show the piece, they do
 * not use it.
 */
public record ItemSpec(@Nullable String item, String material, @Nullable Integer color, @Nullable Skin head,
                       boolean glint, @Nullable String model) {

    static ItemSpec parse(JsonObject json) {
        Skin head = json.has("texture") ? Skin.parse(json) : null;
        return new ItemSpec(
                json.has("item") ? json.get("item").getAsString() : null,
                json.get("material").getAsString(),
                json.has("color") ? json.get("color").getAsInt() : null,
                head,
                json.has("glint") && json.get("glint").getAsBoolean(),
                json.has("model") ? json.get("model").getAsString() : null);
    }

    public Material resolvedMaterial() {
        Material resolved = Material.fromKey(material.toLowerCase());
        if (resolved == null) throw new IllegalArgumentException("Unknown material " + material);
        return resolved;
    }

    public ItemStack toStack() {
        ItemStack.Builder builder = ItemStack.builder(resolvedMaterial());
        if (color != null) builder.set(DataComponents.DYED_COLOR, new Color(color));
        if (head != null) builder.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(head.texture(), head.signature())));
        if (glint) builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        if (model != null) builder.set(DataComponents.ITEM_MODEL, model);
        return builder.build();
    }
}
