package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.type.skyblockgeneric.tab.TabWidget;

import java.util.List;

public final class AreaWidgets {
    private AreaWidgets() {
    }

    public static List<TabWidget> all() {
        return List.of(
                new MinionsWidget(),
                new ComposterWidget(),
                new PestsWidget(),
                new JacobsContestWidget(),
                new VisitorsWidget(),
                new ReputationWidget(),
                new FactionQuestsWidget(),
                new TrophyFishWidget(),
                new VolcanoWidget(),
                new CrystalsWidget());
    }
}
