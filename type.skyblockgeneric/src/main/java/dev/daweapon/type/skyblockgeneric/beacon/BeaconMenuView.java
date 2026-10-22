package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Locale;

public class BeaconMenuView extends StatelessView {
    private static final int INFO_SLOT = 4;
    private static final int MINION_SLOT = 20;
    private static final int EFFECT_SLOT = 21;
    private static final int POWER_SLOT = 22;
    private static final int STAT_SLOT = 23;
    private static final int SUMMONING_SLOT = 24;
    private static final int CLOSE_SLOT = 31;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Beacon", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(INFO_SLOT, (_, c) -> info(data(c)));
        layout.slot(MINION_SLOT, (_, c) -> minionBuff(data(c)), (_, c) -> showTiers((SkyBlockPlayer) c.player()));
        layout.slot(EFFECT_SLOT, (_, c) -> effects(data(c)), (_, c) -> c.push(new BeaconEffectsView()));
        layout.slot(POWER_SLOT, (_, c) -> power(data(c)), (_, c) -> insert((SkyBlockPlayer) c.player(), c));
        layout.slot(STAT_SLOT, (_, c) -> profileStat(data(c)), (_, c) -> c.push(new BeaconStatsView()));
        layout.slot(SUMMONING_SLOT, ItemStacks.item(Material.BLAZE_SPAWN_EGG, "<c>Summoning\n<7>Coming Soon..."));
        Components.close(layout, CLOSE_SLOT);
    }

    private static BeaconData data(ViewContext ctx) {
        return BeaconService.get(((SkyBlockPlayer) ctx.player()).getSkyBlockIsland());
    }

    static String roman(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "V";
        };
    }

    static String number(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    private static ItemStack.Builder missing() {
        return ItemStacks.item(Material.BARRIER, "<c>No Beacon\n<7>Place a Beacon on your island first.");
    }

    private static ItemStack.Builder info(BeaconData data) {
        if (data == null) return missing();
        long now = System.currentTimeMillis();
        String stat = data.stat() == null ? "<a>NONE" : data.stat().label();
        return ItemStacks.item(Material.BEACON, "<a>Beacon\n"
                + "<7>View and manage all island\n<7>Beacon related settings.\n\n"
                + "<7>Beacon Tier: <e>" + roman(data.tier()) + "\n\n"
                + "<7>Buffs:\n"
                + " <7>Minion Buff: <6>+" + number(BeaconData.MINION_PERCENT_PER_TIER * data.tier()) + "%\n"
                + " <7>Active Effects: <b>" + data.effects().stream().filter(effect -> effect.unlockedAt(data.tier())).count() + "\n"
                + " <7>Profile Stat: " + stat);
    }

    private static ItemStack.Builder minionBuff(BeaconData data) {
        if (data == null) return missing();
        String next = data.tier() >= BeaconStat.MAX_TIER ? "<a>Maxed out" : "<6>+" + number(BeaconData.MINION_PERCENT_PER_TIER * (data.tier() + 1)) + "%";
        return ItemStacks.item(Material.GOLDEN_PICKAXE, "<6>Minion Buff\n"
                + "<7>Additional percentage speed\n<7>buff to minions on your island.\n\n"
                + "<7>Current Buff: <6>+" + number(BeaconData.MINION_PERCENT_PER_TIER * data.tier()) + "%\n"
                + "<7>Next Tier: " + next + "\n\n"
                + "<e>Click to view upgrades!");
    }

    private static ItemStack.Builder effects(BeaconData data) {
        if (data == null) return missing();
        long now = System.currentTimeMillis();
        StringBuilder active = new StringBuilder();
        data.effects().stream().filter(effect -> effect.unlockedAt(data.tier())).forEach(effect -> active.append(" ").append(effect.label()).append("\n"));
        String list = active.length() == 0 ? "<c>No active effects.\n" : active.toString();
        String warning = data.isPowered(now) ? "" : "<c>The Beacon needs power for these to apply.\n";
        return ItemStacks.item(Material.POTION, "<b>Beacon Effect\n"
                + "<7>Beacon island effects applied\n<7>to everyone while ON the island.\n\n"
                + "<7>Current Effects:\n" + list + warning + "\n"
                + "<e>Click to change!");
    }

    private static ItemStack.Builder power(BeaconData data) {
        if (data == null) return missing();
        long now = System.currentTimeMillis();
        String status = data.isPowered(now)
                ? "<a>Beacon Activated <8>- <e>" + BeaconService.formatRemaining(data.remainingMillis(now)) + " <7>remaining"
                : "<7>Beacon Deactivated <8>- <c>No Power Remaining";
        String scorched = data.isScorched(now) ? "\n<6>Scorched <7>power is boosting your stats and minions!" : "";
        return ItemStacks.item(Material.FURNACE, "<6>Beacon Power\n"
                + "<7>Consumes <b>Power Crystals <7>to power\n<7>up the Beacon and its effects!\n\n"
                + "<7>Place a single <d>Power Crystal <7>here\n<7>and it can power the <b>Beacon <7>for up\n<7>to <3>48 hours<7>. <d>Power Crystals <7>can\n<7>be stacked for long periods of time.\n\n"
                + status + scorched + "\n\n<e>Click with a crystal on your cursor!");
    }

    private static ItemStack.Builder profileStat(BeaconData data) {
        if (data == null) return missing();
        String current = data.stat() == null
                ? "<c>No active profile stat bonus set!"
                : "<7>Current: " + data.stat().label() + " <a>+" + number(data.stat().valueAtTier(data.tier()));
        return ItemStacks.item(Material.EMERALD, "<a>Profile Stats\n"
                + "<7>Additional profile stat\n<7>bonuses which you gain\n<7>EVERYWHERE in SkyBlock!\n\n"
                + current + "\n\n<e>Click to change!");
    }

    private static void insert(SkyBlockPlayer player, ViewContext ctx) {
        boolean inserted = BeaconFuel.insertCursor(player) || BeaconFuel.insertFromInventory(player);
        if (!inserted) {
            player.sendMessage("<c>You need a Power Crystal or Scorched Power Crystal for that!");
            return;
        }
        ctx.session(DefaultState.class).refresh();
    }

    private static void showTiers(SkyBlockPlayer player) {
        player.sendMessage("<6>Beacon Minion Buff <7>by tier:");
        for (int tier = 1; tier <= BeaconStat.MAX_TIER; tier++) {
            player.sendMessage(" <7>Tier <e>" + roman(tier) + "<7>: <6>+" + number(BeaconData.MINION_PERCENT_PER_TIER * tier) + "%");
        }
        player.sendMessage("<7>Powering with a <6>Scorched Power Crystal <7>adds another <6>+1%<7>.");
    }
}
