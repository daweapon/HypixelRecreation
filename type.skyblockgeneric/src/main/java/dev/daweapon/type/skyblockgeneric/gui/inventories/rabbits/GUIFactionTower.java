package dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.rabbits.FactionRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitFaction;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitFactions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUIFactionTower implements StatefulView<GUIFactionTower.State> {
    private static final int[] FACTION_SLOTS = {19, 21, 23, 25};

    public record State() {
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Faction Tower", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(4, (s, c) -> {
            ChocolateFactoryData data = ChocolateFactoryHelper.getData((SkyBlockPlayer) c.player());
            RabbitFaction faction = data.getFaction();
            String current = faction == null ? "<c>None" : "<a>" + faction.getDisplayName() + " <7>(Lvl " + data.getFactionLevel() + ")";
            return ItemStacks.item(Material.BELL, "<6>Faction Tower\n"
                    + "<7>Choose one of the four <a>Rabbit\n"
                    + "<a>Factions<7>. Each Faction has its own\n"
                    + "<a>Faction Rabbits <7>that can appear in\n"
                    + "<7>your <a>Chocolate Eggs<7>.\n\n"
                    + "<7>Faction Rabbits drop as items. Right\n"
                    + "<7>click them to add them to your Factory!\n\n"
                    + "<7>Current Faction: " + current + "\n"
                    + "<7>Faction Rabbits added: <a>" + data.getFactionRabbits().size() + "<7>/<a>" + FactionRabbit.values().length);
        });

        for (int index = 0; index < RabbitFaction.values().length; index++) {
            RabbitFaction faction = RabbitFaction.values()[index];
            layout.slot(FACTION_SLOTS[index], (s, c) -> factionItem((SkyBlockPlayer) c.player(), faction),
                    (click, c) -> {
                        click((SkyBlockPlayer) c.player(), faction);
                        c.session(State.class).refresh();
                    });
        }

        Components.back(layout, 39, ctx);
        Components.close(layout, 40);
    }

    private net.minestom.server.item.ItemStack.Builder factionItem(SkyBlockPlayer player, RabbitFaction faction) {
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        int found = 0;
        for (FactionRabbit rabbit : FactionRabbit.of(faction)) {
            if (data.hasFactionRabbit(rabbit)) found++;
        }
        String collected = "<7>Rabbits added: <a>" + found + "<7>/<a>" + FactionRabbit.of(faction).size();
        if (data.getFaction() == faction) {
            int level = data.getFactionLevel();
            RabbitRarity max = RabbitFactions.maxRarity(level);
            String next = level >= RabbitFactions.MAX_LEVEL
                    ? "<a><l>MAXED"
                    : "<7>Next Level: " + rarityName(RabbitRarity.values()[level]) + "\n<7>Cost: <6>"
                    + ChocolateFactoryHelper.formatChocolate(RabbitFactions.upgradeCost(level)) + " Chocolate\n\n<e>Click to upgrade!";
            return ItemStacks.head(faction.getTexture(), "<a>" + faction.getDisplayName() + " Lvl " + level + "\n"
                    + "<7>You are part of the " + faction.getDisplayName() + "!\n\n"
                    + rarityName(max) + " <7>" + faction.getDisplayName() + " can appear in\n<7>your eggs.\n\n"
                    + collected + "\n\n" + next);
        }
        long cost = RabbitFactions.joinCost(data);
        String costLine = cost == 0 ? "<7>Cost: <a>FREE" : "<7>Cost: <6>" + ChocolateFactoryHelper.formatChocolate(cost) + " Chocolate";
        String warning = data.getFaction() == null ? "" : "\n<c>Switching resets your Faction level!";
        return ItemStacks.head(faction.getTexture(), "<e>" + faction.getDisplayName() + "\n"
                + "<7>Join the " + faction.getDisplayName() + " to find\n<7>their <a>Faction Rabbits <7>in your eggs.\n\n"
                + collected + "\n\n" + costLine + warning + "\n\n<e>Click to join!");
    }

    private void click(SkyBlockPlayer player, RabbitFaction faction) {
        ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
        if (data.getFaction() == faction) {
            switch (RabbitFactions.upgrade(player)) {
                case SUCCESS -> player.sendMessage("<a>Your dedication to the " + faction.getDisplayName() + " grew to <6>Lvl "
                        + ChocolateFactoryHelper.getData(player).getFactionLevel() + "<a>!");
                case MAXED -> player.sendMessage("<c>You have already reached the highest Faction level!");
                case NOT_ENOUGH_CHOCOLATE -> player.sendMessage("<c>You don't have enough Chocolate!");
                default -> player.sendMessage("<c>The Faction Tower requires Chocolate Factory V!");
            }
            return;
        }
        switch (RabbitFactions.join(player, faction)) {
            case SUCCESS -> player.sendMessage("<a>You joined the " + faction.getDisplayName() + "!");
            case NOT_ENOUGH_CHOCOLATE -> player.sendMessage("<c>You don't have enough Chocolate!");
            default -> player.sendMessage("<c>The Faction Tower requires Chocolate Factory V!");
        }
    }

    private static String rarityName(RabbitRarity rarity) {
        String name = rarity.getDisplayName();
        return "<color:" + rarity.getColorCode() + "><l>" + name.charAt(0) + name.substring(1).toLowerCase() + "</l>";
    }
}
