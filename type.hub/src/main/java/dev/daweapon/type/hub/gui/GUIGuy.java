package dev.daweapon.type.hub.gui;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.bank.PersonalBankService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBankData;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIGuy extends StatelessView {
    private static final String PERSONAL_BANK_TEXTURE = "e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852";
    private static final String[] COOLDOWN_NAMES = {"1 hour", "20 minutes", "5 minutes", "No Cooldown"};
    private static final int[] SKYBLOCK_XP_REWARD = {0, 25, 35, 50};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Guy", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int level = PersonalBankService.data(player).getPersonalBankLevel();
        int next = level + 1;

        if (next >= PersonalBankService.COST.length) {
            layout.slot(13, ItemStacks.head(PERSONAL_BANK_TEXTURE, """
                    <a>Personal Bank Upgrade
                    <7>You unlocked all the personal
                    <7>bank upgrades!

                    <7><o>Hackerman!"""));
            return;
        }

        layout.slot(13, ItemStacks.head(PERSONAL_BANK_TEXTURE, next + 1, """
                        <a>Personal Bank Upgrade
                        <7>Guy can reduce the cooldown
                        <7>to access your Personal
                        <7>Bank.

                        <7>Current: <a>{}
                        <7>Upgrade: <e>{}

                        <7>Cost: <6>{:,} Coins
                        <7>Collection: <a>{} Emeralds

                        <e>Click to upgrade!""",
                        COOLDOWN_NAMES[level], COOLDOWN_NAMES[next], (long) PersonalBankService.COST[next],
                        StringUtility.shortenNumber(PersonalBankService.COLLECTION[next])),
                (click, context) -> upgrade(player, next, context));
    }

    private static void upgrade(SkyBlockPlayer player, int next, ViewContext ctx) {
        if (player.getCollection().get(ItemType.EMERALD) < PersonalBankService.COLLECTION[next]) {
            player.sendMessage("<c>Your Emerald Collection is too low for this upgrade!");
            return;
        }
        if (player.getCoins() < PersonalBankService.COST[next]) {
            player.sendMessage("<c>You don't have enough coins!");
            return;
        }
        if (!PersonalBankService.tryUpgrade(player)) return;

        DatapointBankData bankData = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);
        bankData.setValue(bankData.getValue());
        player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.PERSONAL_BANK_UPGRADES, SKYBLOCK_XP_REWARD[next]);
        player.playSuccessSound();
        player.sendMessage("<a>Your Personal Bank cooldown is now <e>{}<a>!", COOLDOWN_NAMES[next]);
        ctx.session(DefaultState.class).refresh();
    }
}
