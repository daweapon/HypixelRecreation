package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetToggle;
import dev.daweapon.type.skyblockgeneric.user.ProfileMode;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class ProfileWidget implements TabWidget {
    private static final String SB_LEVEL = "sb_level";
    private static final String BANK = "bank";
    private static final String INTEREST = "interest";
    private static final String SOULFLOW = "soulflow";

    @Override
    public String id() {
        return "profile";
    }

    @Override
    public String displayName() {
        return "Profile";
    }

    @Override
    public Material icon() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    public int defaultPriority() {
        return 90;
    }

    @Override
    public List<TabWidgetToggle> toggles() {
        return List.of(
                new TabWidgetToggle(SB_LEVEL, "SkyBlock Level", true),
                new TabWidgetToggle(BANK, "Bank", true),
                new TabWidgetToggle(INTEREST, "Interest", false),
                new TabWidgetToggle(SOULFLOW, "Soulflow", false));
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        SkyBlockDataHandler data = player.getSkyblockDataHandler();
        List<Text> lines = new ArrayList<>();
        String mode = player.getProfileMode() == ProfileMode.IRONMAN ? " <7>♲" : "";
        lines.add(Text.of("<e><l>Profile: <a>{}" + mode,
                data.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue()));

        if (context.shows(this, SB_LEVEL)) {
            double totalXp = player.getSkyBlockExperience().getTotalXP();
            int level = (int) (totalXp / SkyBlockXPSource.XP_PER_LEVEL);
            int xp = (int) (totalXp % SkyBlockXPSource.XP_PER_LEVEL);
            lines.add(Text.of(" <7>SB Level: <6>[{}] <b>{}<3>/<b>{} XP", level, xp, SkyBlockXPSource.XP_PER_LEVEL));
        }

        DatapointBankData.BankData bank = data.get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue();
        if (context.shows(this, BANK)) lines.add(bankLine(player, bank));
        if (context.shows(this, INTEREST)) {
            long hours = SkyBlockCalendar.INTEREST_INTERVAL - SkyBlockCalendar.getDifferenceInHours(bank.getLastClaimedInterest());
            lines.add(Text.of(" <7>Interest: <e>{}", hours <= 0 ? "Soon" : hours + (hours == 1 ? " Hour" : " Hours")));
        }
        if (context.shows(this, SOULFLOW)) {
            lines.add(Text.of(" <7>Soulflow: <3>{:,}",
                    data.get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue()));
        }
        return lines;
    }

    private Text bankLine(SkyBlockPlayer player, DatapointBankData.BankData bank) {
        if (player.isCoop()) {
            CoopDatabase.Coop coop = player.getCoop();
            if (coop != null) return Text.of(" <7>Bank: <6>{}", StringUtility.shortenNumber(coop.bankBalance()));
        }
        return Text.of(" <7>Bank: <6>{} <7>/ <6>{}",
                StringUtility.shortenNumber(bank.getAmount()), StringUtility.shortenNumber(bank.getBalanceLimit()));
    }
}
