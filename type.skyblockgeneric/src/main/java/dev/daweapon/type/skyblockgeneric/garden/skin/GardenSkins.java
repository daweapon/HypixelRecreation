package dev.daweapon.type.skyblockgeneric.garden.skin;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GardenSkins {
    private static volatile List<Skin> catalogue;

    private GardenSkins() {
    }

    public record Skin(String id, GardenSkinType type, String name, Rarity rarity, @Nullable ItemType item,
                       int gardenLevel, Material icon) {
        public String coloured() {
            return "<color:" + rarity.getColor().asHexString() + ">" + name;
        }
    }

    public static List<Skin> all() {
        List<Skin> skins = catalogue;
        if (skins == null) {
            skins = build();
            catalogue = skins;
        }
        return skins;
    }

    public static List<Skin> of(GardenSkinType type) {
        return all().stream().filter(skin -> skin.type() == type).toList();
    }

    public static @Nullable Skin byId(String id) {
        return all().stream().filter(skin -> skin.id().equals(id)).findFirst().orElse(null);
    }

    public static @Nullable Skin byItem(@Nullable ItemType item) {
        if (item == null) return null;
        return byId(item.name());
    }

    public static boolean isUnlocked(GardenProfile profile, Skin skin) {
        if (skin.item() != null) return profile.getSkins().owns(skin.id());
        return GardenProgress.level(profile) >= skin.gardenLevel();
    }

    public static List<Skin> unlocked(GardenProfile profile, GardenSkinType type) {
        return of(type).stream().filter(skin -> isUnlocked(profile, skin)).toList();
    }

    public static Skin selected(GardenProfile profile, GardenSkinType type) {
        Skin skin = byId(profile.getSkins().selected(type));
        if (skin == null || skin.type() != type || !isUnlocked(profile, skin)) return byId(type.getDefaultId());
        return skin;
    }

    public static boolean select(SkyBlockPlayer player, Skin skin) {
        GardenProfile profile = Gardens.profile(player);
        if (!isUnlocked(profile, skin)) return false;
        profile.getSkins().select(skin.type(), skin.id());
        Gardens.save(player, profile);
        return true;
    }

    public static boolean unlock(SkyBlockPlayer player, Skin skin) {
        GardenProfile profile = Gardens.profile(player);
        if (!profile.getSkins().unlock(skin.id())) return false;
        Gardens.save(player, profile);
        return true;
    }

    public static void registerInteractables() {
        InteractableRegistry.register("GARDEN_SKIN", InteractableItemConfig.builder()
                .rightClickHandler(GardenSkins::consume).build());
    }

    private static void consume(SkyBlockPlayer player, SkyBlockItem item) {
        Skin skin = byItem(item.getAttributeHandler().getPotentialType());
        if (skin == null) return;
        if (!unlock(player, skin)) {
            player.sendMessage("<c>You have already unlocked the " + skin.coloured() + " " + skin.type().getDisplayName() + " Skin<c>!");
            return;
        }
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
        player.sendMessage("<a>You unlocked the " + skin.coloured() + " " + skin.type().getDisplayName()
                + " Skin<a>! Select it in <e>Garden Skins <a>at the <e>Desk<a>.");
    }

    private static List<Skin> build() {
        List<Skin> skins = new ArrayList<>();
        skins.add(new Skin(GardenSkinType.BARN.getDefaultId(), GardenSkinType.BARN, "Default", Rarity.COMMON, null, 0, Material.OAK_PLANKS));
        skins.add(new Skin("MEDIEVAL_BARN_SKIN", GardenSkinType.BARN, "Medieval", Rarity.UNCOMMON, null, 3, Material.STONE_BRICKS));
        skins.add(new Skin("SUNNY_BARN_SKIN", GardenSkinType.BARN, "Sunny", Rarity.UNCOMMON, null, 6, Material.YELLOW_TERRACOTTA));
        skins.add(new Skin("RED_BARN_SKIN", GardenSkinType.BARN, "Red", Rarity.UNCOMMON, null, 9, Material.RED_TERRACOTTA));
        skins.add(new Skin("CABIN_BARN_SKIN", GardenSkinType.BARN, "Cabin", Rarity.RARE, null, 11, Material.SPRUCE_LOG));
        skins.add(new Skin("MANSION_HEIGHTS_BARN_SKIN", GardenSkinType.BARN, "Mansion Heights", Rarity.EPIC, null, 15, Material.QUARTZ_BLOCK));
        skins.add(new Skin(GardenSkinType.GREENHOUSE.getDefaultId(), GardenSkinType.GREENHOUSE, "Default", Rarity.COMMON, null, 0,
                Material.WHITE_STAINED_GLASS));
        for (GardenSkinType type : GardenSkinType.values()) {
            for (ItemType item : ItemType.values()) {
                if (!item.name().endsWith(type.getItemSuffix())) continue;
                SkyBlockItem skyBlockItem = new SkyBlockItem(item);
                String name = skyBlockItem.getDisplayName();
                String suffix = " " + type.getDisplayName() + " Skin";
                if (name.endsWith(suffix)) name = name.substring(0, name.length() - suffix.length());
                skins.add(new Skin(item.name(), type, name, skyBlockItem.getAttributeHandler().getRarity(), item, 0,
                        skyBlockItem.getMaterial()));
            }
        }
        return List.copyOf(skins);
    }
}
