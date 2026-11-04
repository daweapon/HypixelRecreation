package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.skin.GardenSkinType;
import dev.daweapon.type.skyblockgeneric.garden.skin.GardenSkins;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIGardenSkinSelect extends StatelessView {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    private final GardenSkinType type;

    public GUIGardenSkinSelect(GardenSkinType type) {
        this.type = type;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of(type.getDisplayName() + " Skins"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        for (int index = 0; index < SLOTS.length; index++) {
            int position = index;
            layout.slot(SLOTS[index], (_, c) -> {
                List<GardenSkins.Skin> unlocked = GardenSkins.unlocked(Gardens.profile((SkyBlockPlayer) c.player()), type);
                if (position >= unlocked.size()) return ItemStack.builder(Material.AIR);
                return skinItem((SkyBlockPlayer) c.player(), unlocked.get(position));
            }, (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<GardenSkins.Skin> unlocked = GardenSkins.unlocked(Gardens.profile(player), type);
                if (position >= unlocked.size()) return;
                GardenSkins.Skin skin = unlocked.get(position);
                if (GardenSkins.selected(Gardens.profile(player), type).id().equals(skin.id())) {
                    player.sendMessage("<c>This skin is already selected!");
                    return;
                }
                if (GardenSkins.select(player, skin)) {
                    player.sendMessage("<a>Selected the " + skin.coloured() + " " + type.getDisplayName() + " Skin<a>!");
                }
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private ItemStack.Builder skinItem(SkyBlockPlayer player, GardenSkins.Skin skin) {
        GardenProfile profile = Gardens.profile(player);
        boolean selected = GardenSkins.selected(profile, type).id().equals(skin.id());
        String rarity = skin.rarity().name().replace('_', ' ');
        List<Text> lore = List.of(
                Text.of("<7>Select this skin for your " + type.getSelectNoun() + "!"),
                GardenMenus.blank(),
                Text.of("<color:" + skin.rarity().getColor().asHexString() + "><l>" + rarity + " COSMETIC"),
                GardenMenus.blank(),
                Text.of(selected ? "<a>SELECTED" : "<e>Click to Select!"));
        ItemStack.Builder base = skin.item() == null
                ? ItemStack.builder(skin.icon())
                : new NonPlayerItemUpdater(new SkyBlockItem(skin.item())).getUpdatedItem();
        return ItemStacks.text(base, Text.of(skin.coloured()), lore);
    }
}
