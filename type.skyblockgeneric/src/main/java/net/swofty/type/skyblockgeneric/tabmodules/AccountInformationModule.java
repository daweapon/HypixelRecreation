package net.swofty.type.skyblockgeneric.tabmodules;

import net.kyori.adventure.text.Component;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkillCategory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountInformationModule extends TablistModule {

    public List<TablistEntry> getEntries(SkyBlockPlayer player) {
        Locale l = player.getLocale();
        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.account_info"), l, TablistSkinRegistry.ORANGE)
        ));

        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        DatapointBankData.BankData bankData = dataHandler.get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue();

        entries.add(new TablistEntry(Text.key("tablist.account_info.profile_label", dataHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue()), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.pet_sitter"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.bank", StringUtility.shortenNumber(bankData.getAmount()), StringUtility.shortenNumber(bankData.getBalanceLimit())), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());

        SkillCategories skillCategory = net.swofty.type.generic.HypixelConst.isGardenServer() ? SkillCategories.FARMING
                : dataHandler.get(SkyBlockDataHandler.Data.LAST_EDITED_SKILL, DatapointSkillCategory.class).getValue();
        DatapointSkills.PlayerSkills skills = player.getSkills();
        ItemStatistics playerStatistics = player.getStatistics().allStatistics();
        entries.add(new TablistEntry(Text.key("tablist.account_info.skills_label", String.valueOf(skillCategory), String.valueOf(skills.getCurrentLevel(skillCategory)), String.valueOf(skills.getPercentage(skillCategory))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.speed", stat(ItemStatistic.SPEED, playerStatistics)), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.strength", stat(ItemStatistic.STRENGTH, playerStatistics)), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.crit_chance", stat(ItemStatistic.CRITICAL_CHANCE, playerStatistics)), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.crit_damage", stat(ItemStatistic.CRITICAL_DAMAGE, playerStatistics)), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.attack_speed", stat(ItemStatistic.BONUS_ATTACK_SPEED, playerStatistics)), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());
        fillRestWithGray(entries);
        return entries;
    }

    public static Component stat(ItemStatistic statistic, ItemStatistics statistics) {
        return statistic.getColouredSymbol()
                .append(Component.text(StringUtility.commaify(Math.round(statistics.getOverall(statistic))), statistic.getDisplayColor()));
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        return getEntries((SkyBlockPlayer) player);
    }
}
