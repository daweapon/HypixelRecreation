package dev.daweapon.type.skyblockgeneric.gui.inventories.election;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.mythological.GriffinPet;
import dev.daweapon.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The shop half of Diana's in-office menu: the Ancestral Spade (sold while she is Mayor or
 * Minister), the Griffin pet (sold while her Mythological Ritual runs) and Reset Burrows.
 */
public final class DianaOffice {
    public static final int MAYOR_RESULTS_SLOT = 12;
    public static final int MINISTER_SPADE_SLOT = 13;
    private static final int MAYOR_SPADE_SLOT = 14;
    private static final int GRIFFIN_SLOT = 15;
    private static final int RESET_BURROWS_SLOT = 32;

    private static final ShopPrice SPADE_PRICE = new CombinedShopPrice(List.of(
            new CoinShopPrice(5_000),
            new ItemShopPrice(ItemType.GOLD_INGOT, 32)));
    private static final ShopPrice GRIFFIN_PRICE = new CoinShopPrice(25_000);

    private DianaOffice() {
    }

    public static boolean isDiana(SkyBlockMayor mayor) {
        return mayor == SkyBlockMayor.DIANA;
    }

    public static void layoutMayor(ViewLayout<DefaultState> layout) {
        layoutSpade(layout, MAYOR_SPADE_SLOT);
        layoutGriffin(layout);
        layoutResetBurrows(layout);
    }

    public static void layoutSpade(ViewLayout<DefaultState> layout, int slot) {
        layoutTrade(layout, slot, () -> new SkyBlockItem(ItemType.ANCESTRAL_SPADE), () -> new SkyBlockItem(ItemType.ANCESTRAL_SPADE),
                SPADE_PRICE, List.of());
    }

    private static void layoutGriffin(ViewLayout<DefaultState> layout) {
        if (!ElectionPerks.isActive(SkyBlockMayor.Perk.MYTHOLOGICAL_RITUAL)) return;
        layoutTrade(layout, GRIFFIN_SLOT, DianaOffice::griffinPreview, () -> griffin(Rarity.COMMON), GRIFFIN_PRICE, List.of(
                Text.empty(),
                Text.of("<c>This is a preview of Lvl 100."),
                Text.of("<c>New pets are lowest level!")));
    }

    private static void layoutTrade(ViewLayout<DefaultState> layout, int slot, Supplier<SkyBlockItem> display,
                                    Supplier<SkyBlockItem> purchase, ShopPrice price, List<Text> note) {
        layout.slot(slot, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            ItemStack.Builder stack = PlayerItemUpdater.playerUpdate(player, display.get().getItemStackBuilder().build());
            List<Text> lore = new ArrayList<>(note);
            lore.add(Text.empty());
            lore.add(Text.of("<7>Cost"));
            lore.addAll(price.getGUIDisplay());
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to trade!"));
            return ItemStacks.appendLore(stack, lore);
        }, (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (!price.canAfford(player)) {
                player.sendMessage("<c>You don't have enough " + price.getNamePlural() + "!");
                return;
            }
            price.processPurchase(player);
            player.addAndUpdateItem(purchase.get());
            player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));
        });
    }

    private static void layoutResetBurrows(ViewLayout<DefaultState> layout) {
        layout.slot(RESET_BURROWS_SLOT, (s, c) -> {
            boolean equipped = GriffinPet.equippedRarity((SkyBlockPlayer) c.player()).isPresent();
            return ItemStacks.item(Material.DIRT, 1, Text.of("<a>Reset Burrows"), List.of(
                    Text.of("<7>Are your burrows too high or too"),
                    Text.of("<7>low level? Use this function to reset"),
                    Text.of("<7>all of your burrows."),
                    Text.empty(),
                    equipped ? Text.of("<e>Click to reset!") : Text.of("<c>Griffin not equipped!")));
        }, (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (GriffinPet.equippedRarity(player).isEmpty()) {
                player.sendMessage("<c>You need to have a Griffin pet equipped to reset your burrows!");
                return;
            }
            GriffinBurrowService.INSTANCE.reset(player);
        });
    }

    private static SkyBlockItem griffinPreview() {
        SkyBlockItem preview = griffin(Rarity.COMMON);
        PetLevels.setLevel(preview, PetLevels.maxLevel(preview));
        return preview;
    }

    private static SkyBlockItem griffin(Rarity rarity) {
        SkyBlockItem griffin = new SkyBlockItem(ItemType.GRIFFIN_PET);
        griffin.getAttributeHandler().setRarity(rarity);
        return griffin;
    }
}
