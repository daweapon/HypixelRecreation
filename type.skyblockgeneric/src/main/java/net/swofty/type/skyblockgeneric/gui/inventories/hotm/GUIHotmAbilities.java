package net.swofty.type.skyblockgeneric.gui.inventories.hotm;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.hotm.HotmAbilities;
import net.swofty.type.skyblockgeneric.hotm.HotmAbility;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIHotmAbilities extends StatelessView {
    private static final int[] ABILITY_SLOTS = {10, 12, 14, 16, 28, 30};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Pickaxe Abilities", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        HotmAbility[] abilities = HotmAbility.values();
        for (int i = 0; i < abilities.length; i++) {
            HotmAbility ability = abilities[i];
            layout.slot(ABILITY_SLOTS[i], (_, _) -> item(player, ability), (_, c) -> {
                if (HotmAbilities.select(player, ability)) {
                    player.sendMessage("<a>You selected <6>" + ability.getDisplayName() + "<a>.");
                } else {
                    player.sendMessage("<c>You need Heart of the Mountain Tier " + ability.getUnlockTier() + " to unlock this ability.");
                }
                c.session(Object.class).refresh();
            });
        }
    }

    private static ItemStack.Builder item(SkyBlockPlayer player, HotmAbility ability) {
        int level = HotmService.abilityLevel(player);
        boolean unlocked = HotmAbilities.isUnlocked(player, ability);
        boolean selected = HotmAbilities.selected(player) == ability;
        String description = ability.getDescription()
                .replace("{value}", String.format("%d", Math.round(ability.value(level))))
                .replace("{duration}", String.valueOf(ability.duration(level)));
        StringBuilder text = new StringBuilder((unlocked ? "<6>" : "<c>") + ability.getDisplayName() + " <7>Level " + level + "\n");
        text.append("<7>").append(description).append("\n\n<7>Cooldown: <a>")
                .append(Math.round(HotmAbilities.cooldownSeconds(player, ability))).append("s\n");
        if (!unlocked) {
            text.append("\n<c>Unlocked at Heart of the Mountain Tier ").append(ability.getUnlockTier());
        } else if (selected) {
            text.append("\n<a>SELECTED");
        } else {
            text.append("\n<e>Click to select!");
        }
        ItemStack.Builder builder = ItemStacks.item(ability.getIcon(), text.toString());
        return selected ? ItemStacks.enchanted(builder) : builder;
    }
}
