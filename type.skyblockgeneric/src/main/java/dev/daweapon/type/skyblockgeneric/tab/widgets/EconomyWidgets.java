package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.type.skyblockgeneric.tab.TabWidget;

import java.util.List;

public final class EconomyWidgets {
    private EconomyWidgets() {
    }

    public static List<TabWidget> all() {
        return List.of(
                new FireSalesWidget(),
                new PrimalFearWidget(),
                new DailyQuestsWidget(),
                new ForgeWidget(),
                new PowderWidget(),
                new MineshaftPityWidget(),
                new CommissionsWidget(),
                new PickaxeAbilityWidget(),
                new EventsWidget(),
                new ElectionWidget(),
                new CollectionsWidget(),
                new SlayerWidget());
    }
}
