package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.type.skyblockgeneric.tab.TabWidget;

import java.util.List;

public final class CoreWidgets {
    private CoreWidgets() {
    }

    public static List<TabWidget> all() {
        return List.of(
                new GeneralInfoWidget(),
                new ProfileWidget(),
                new PetWidget(),
                new SkillsWidget(),
                new StatsWidget(),
                new EffectsWidget(),
                new CookieBuffWidget());
    }
}
