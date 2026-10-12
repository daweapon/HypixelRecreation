package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SafeConfig;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class ItemSkinComponent extends SkyBlockItemComponent {
    private final String skinId;
    private final List<String> targets;
    private final String texture;
    private final boolean animated;

    public ItemSkinComponent(String skinId, List<String> targets, String texture, boolean animated) {
        this.skinId = skinId;
        this.targets = List.copyOf(targets);
        this.texture = texture;
        this.animated = animated;

        String handlerId = "ITEM_SKIN:" + skinId;
        AnvilCombineRegistry.register(handlerId, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setItemSkin(ItemType.get(skinId)),
                (player, upgradeItem, sacrificeItem) -> canApplyTo(upgradeItem),
                (upgradeItem, sacrificeItem, player) -> 0));
        addInheritedComponent(new AnvilCombinableComponent(handlerId));
    }

    public static ItemSkinComponent parse(String skinId, SafeConfig config) {
        return new ItemSkinComponent(skinId, config.getList("targets", String.class), config.getString("texture"),
                config.getBoolean("animated", false));
    }

    public boolean canApplyTo(SkyBlockItem target) {
        ItemType type = target.getAttributeHandler().getPotentialType();
        if (animated || type == null || !targets.contains(type.name())) return false;
        ItemType current = target.getAttributeHandler().getItemSkin();
        return current == null || !current.name().equals(skinId);
    }

    public static @Nullable String textureOf(SkyBlockItem item) {
        ItemType skin = item.getAttributeHandler().getItemSkin();
        if (skin == null) return null;
        SkyBlockItem skinItem = new SkyBlockItem(skin);
        return skinItem.hasComponent(ItemSkinComponent.class) ? skinItem.getComponent(ItemSkinComponent.class).getTexture() : null;
    }
}
