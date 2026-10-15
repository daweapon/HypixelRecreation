package dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Staff-only GUI for directly editing a player's Chocolate Factory progress
public class GUIChocolateFactoryAdmin extends StatelessView {
    private static final int RABBIT_BARN_MAX_LEVEL = 247;
    private static final int HAND_BAKED_MAX_LEVEL = 10;
    private static final int TIME_TOWER_MAX_LEVEL = 15;
    private static final int RABBIT_SHRINE_MAX_LEVEL = 20;
    private static final int COACH_JACKRABBIT_MAX_LEVEL = 20;
    private static final int EMPLOYEE_MAX_LEVEL = 220;

    private static final String CHOCOLATE_TEXTURE = "9a815398e7da89b1bc08f646cafc8e7b813da0be0eec0cce6d3eff5207801026";

    private static final int[] EMPLOYEE_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final DatapointChocolateFactory.EmployeeType[] EMPLOYEE_TYPES = DatapointChocolateFactory.EmployeeType.values();

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Chocolate Factory - Admin Menu", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        layout.slot(10, (s, c) -> ItemStacks.head(CHOCOLATE_TEXTURE, """
                <6>Chocolate
                <7>Current: <6>{}

                <e>Click to set!""", ChocolateFactoryHelper.formatChocolate(data.getChocolate())),
                (click, c) -> promptLong(player, "Enter Chocolate amount", value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setChocolate(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(11, (s, c) -> ItemStacks.head(CHOCOLATE_TEXTURE, """
                <6>All-Time Chocolate
                <7>Drives <a>Milestones <7>and <a>Prestige<7>.
                <7>Current: <6>{}

                <e>Click to set!""", ChocolateFactoryHelper.formatChocolate(data.getChocolateAllTime())),
                (click, c) -> promptLong(player, "Enter All-Time Chocolate", value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setChocolateAllTime(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(12, (s, c) -> ItemStacks.item(Material.OAK_FENCE, """
                <a>Rabbit Barn Level
                <7>Storage capacity. Max: <a>{}
                <7>Current: <a>{}

                <e>Click to set!""", RABBIT_BARN_MAX_LEVEL, data.getRabbitBarnLevel()),
                (click, c) -> promptInt(player, "Enter Rabbit Barn Level", 0, RABBIT_BARN_MAX_LEVEL, value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setRabbitBarnLevel(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(13, (s, c) -> ItemStacks.item(Material.COOKIE, """
                <d>Hand-Baked Chocolate Level
                <7>Max: <d>{}
                <7>Current: <d>{}

                <e>Click to set!""", HAND_BAKED_MAX_LEVEL, data.getHandBakedChocolateLevel()),
                (click, c) -> promptInt(player, "Enter Hand-Baked Level", 0, HAND_BAKED_MAX_LEVEL, value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setHandBakedChocolateLevel(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(14, (s, c) -> ItemStacks.item(Material.CLOCK, """
                <d>Time Tower Level
                <7>Max: <d>{}
                <7>Current: <d>{}

                <e>Click to set!""", TIME_TOWER_MAX_LEVEL, data.getTimeTowerLevel()),
                (click, c) -> promptInt(player, "Enter Time Tower Level", 0, TIME_TOWER_MAX_LEVEL, value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setTimeTowerLevel(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(15, (s, c) -> ItemStacks.item(Material.CLOCK, """
                <d>Time Tower Charges
                <7>Current: <d>{}

                <e>Click to set!""", data.getTimeTowerCharges()),
                (click, c) -> promptInt(player, "Enter Time Tower Charges", 0, 99, value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setTimeTowerCharges(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(16, (s, c) -> ItemStacks.item(Material.RABBIT_FOOT, """
                <d>Rabbit Shrine Level
                <7>Max: <d>{}
                <7>Current: <d>{}

                <e>Click to set!""", RABBIT_SHRINE_MAX_LEVEL, data.getRabbitShrineLevel()),
                (click, c) -> promptInt(player, "Enter Rabbit Shrine Level", 0, RABBIT_SHRINE_MAX_LEVEL, value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setRabbitShrineLevel(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        layout.slot(19, (s, c) -> ItemStacks.item(Material.SADDLE, """
                <d>Coach Jackrabbit Level
                <7>Max: <d>{}
                <7>Current: <d>{}

                <e>Click to set!""", COACH_JACKRABBIT_MAX_LEVEL, data.getCoachJackrabbitLevel()),
                (click, c) -> promptInt(player, "Enter Coach Jackrabbit Level", 0, COACH_JACKRABBIT_MAX_LEVEL, value -> {
                    DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                    d.setCoachJackrabbitLevel(value);
                    ChocolateFactoryHelper.saveData(player, d);
                }, c));

        for (int i = 0; i < EMPLOYEE_SLOTS.length; i++) {
            int slot = EMPLOYEE_SLOTS[i];
            DatapointChocolateFactory.EmployeeType employeeType = EMPLOYEE_TYPES[i];

            layout.slot(slot, (s, c) -> {
                DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                DatapointChocolateFactory.EmployeeData employee = d.getEmployees().get(employeeType);
                int level = employee != null ? employee.getLevel() : 0;

                return ItemStacks.item(Material.VILLAGER_SPAWN_EGG, """
                        <a>{}
                        <7>Max: <a>{}
                        <7>Current: <a>{}

                        <e>Click to set!""", employeeType.getName(), EMPLOYEE_MAX_LEVEL, level);
            }, (click, c) -> promptInt(player, "Enter " + employeeType.getName() + " Level", 0, EMPLOYEE_MAX_LEVEL, value -> {
                DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
                d.setEmployee(employeeType, value);
                ChocolateFactoryHelper.saveData(player, d);
            }, c));
        }

        layout.slot(40, (s, c) -> {
            DatapointChocolateFactory.ChocolateFactoryData d = ChocolateFactoryHelper.getData(player);
            int found = d.getFoundRabbits().size();
            int total = ChocolateRabbit.values().length;

            return ItemStacks.item(Material.RABBIT_HIDE, """
                    <a>Rabbit Collection
                    <7>Found: <a>{}<7>/<a>{}

                    <e>Click to edit!""", found, total);
        }, (click, c) -> player.openView(new GUIHoppityCollection(), GUIHoppityCollection.adminInitialState()));

        Components.back(layout, 45, ctx);
        Components.close(layout, 49);
    }

    private void promptLong(SkyBlockPlayer player, String prompt, java.util.function.LongConsumer onSuccess, ViewContext c) {
        new HypixelSignGUI(player).open(new String[]{prompt, ""}).thenAccept(input -> {
            if (input == null) return;
            try {
                long value = Long.parseLong(input.replace(",", "").trim());
                if (value < 0) throw new NumberFormatException();
                onSuccess.accept(value);
                c.session(DefaultState.class).refresh();
            } catch (NumberFormatException ignored) {
                player.sendMessage("<c>Please enter a valid positive whole number.");
            }
        });
    }

    private void promptInt(SkyBlockPlayer player, String prompt, int min, int max, java.util.function.IntConsumer onSuccess, ViewContext c) {
        new HypixelSignGUI(player).open(new String[]{prompt, ""}).thenAccept(input -> {
            if (input == null) return;
            try {
                int value = Integer.parseInt(input.replace(",", "").trim());
                if (value < min || value > max) throw new NumberFormatException();
                onSuccess.accept(value);
                c.session(DefaultState.class).refresh();
            } catch (NumberFormatException ignored) {
                player.sendMessage("<c>Please enter a valid number between {} and {}.", min, max);
            }
        });
    }
}
