package dev.daweapon.type.theend.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.theend.altar.DraconicAltarRules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GUIDraconicAltar extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Draconic Altar", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.slot(4, ItemStacks.item(Material.DRAGON_HEAD, """
                <5>Draconic Altar
                <7>Sacrifice dragon armor, Ender gear
                <7>and the Aspect of the Dragons for
                <7><d>Dragon Essence <7>and a chance at a bonus.

                <7>Items below are in your inventory."""));
        for (int i = 0; i < SLOTS.length; i++) {
            int index = i;
            layout.slot(SLOTS[i], (_, c) -> entry((SkyBlockPlayer) c.player(), index), (_, c) -> {
                List<ItemType> held = sacrificable((SkyBlockPlayer) c.player());
                if (index < held.size()) sacrifice((SkyBlockPlayer) c.player(), held.get(index));
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private ItemStack.Builder entry(SkyBlockPlayer player, int index) {
        List<ItemType> held = sacrificable(player);
        if (index >= held.size()) return ItemStack.builder(Material.AIR);
        ItemType type = held.get(index);
        var rules = DraconicAltarRules.of(type.name()).orElseThrow();
        ItemStack.Builder stack = PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(type).getItemStack());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        lore.add(Text.of("<7>Sacrifice for <d>{} Dragon Essence", rules.essence()));
        lore.add(Text.of("<7>and a {}% chance for a bonus.", (int) DraconicAltarRules.BONUS_CHANCE_PERCENT));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to sacrifice one!"));
        return ItemStacks.appendLore(stack, lore);
    }

    private List<ItemType> sacrificable(SkyBlockPlayer player) {
        Map<ItemType, Integer> held = new LinkedHashMap<>();
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if (stack.isAir()) continue;
            ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
            if (type != null && DraconicAltarRules.of(type.name()).isPresent()) held.merge(type, stack.amount(), Integer::sum);
        }
        return new ArrayList<>(held.keySet());
    }

    private void sacrifice(SkyBlockPlayer player, ItemType type) {
        var rules = DraconicAltarRules.of(type.name()).orElseThrow();
        if (!player.removeItemFromPlayer(type, 1)) return;
        Essences.add(player, EssenceType.DRAGON, rules.essence());
        player.sendMessage(Text.of("<5>You sacrificed {} <5>for <d>{} Dragon Essence<5>!", new SkyBlockItem(type).getDisplayNameText(), rules.essence()));
        DraconicAltarRules.Bonus bonus = LootRoll.pick(player.getUuid(), rules.withNothing(),
                entry -> entry.itemId() == null ? null : ItemType.get(entry.itemId()),
                DraconicAltarRules.Bonus::weight, ThreadLocalRandom.current());
        if (bonus.itemId() == null) return;
        ItemType reward = ItemType.get(bonus.itemId());
        if (reward == null) return;
        int amount = bonus.min() + ThreadLocalRandom.current().nextInt(bonus.max() - bonus.min() + 1);
        SkyBlockItem item = new SkyBlockItem(reward, amount);
        player.addAndUpdateItem(item);
        player.sendMessage(Text.of("<d>The altar rewards you with {}<d>!", item.getDisplayNameText()));
    }
}
