package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestData;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestRules;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIPesthunter extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Pesthunter"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(11, (_, c) -> bag((SkyBlockPlayer) c.player()), (_, c) -> {
            trade((SkyBlockPlayer) c.player());
            c.session(DefaultState.class).refresh();
        });
        layout.slot(13, (_, c) -> bonus((SkyBlockPlayer) c.player()));
        layout.slot(15, (_, c) -> ItemStacks.item(Material.OAK_SIGN, Text.of("<6>Pesthunter's Wares"), List.of(
                        Text.of("<7>Using <2>Pests <7>as currency,"),
                        Text.of("<7>purchase a variety of bits 'n bobs"),
                        Text.of("<7>that <6>Phillip <7>has held onto over the"),
                        Text.of("<7>years!"),
                        Text.empty(),
                        Text.of("<7>Vacuum Bag: <2>{} Pests", stored((SkyBlockPlayer) c.player())),
                        Text.empty(),
                        Text.of("<e>Click to view!"))),
                (_, c) -> c.push(new CatalogueShopView("pesthunter_wares")));
        layout.slot(32, (_, _) -> ItemStacks.item(Material.RED_DYE, Text.of("<c>Garden Infestation"), List.of(
                Text.of("<7>Your <6>Farming Fortune <7>decreases"),
                Text.of("<7>as more <2>Pests <7>spawn in <a>The"),
                Text.of("<a>Garden<7>!"),
                Text.empty(),
                Text.of("<7>For every <2>+100 Bonus Pest Chance"),
                Text.of("<7>you have, you can spawn <a>1 <7>extra <2>Pest"),
                Text.of("<7>before you start losing fortune!"),
                Text.empty(),
                Text.of("<7>Get rid of <2>Pests <7>to restore it!"),
                Text.empty(),
                Text.of(" <c>0-3 Pests <a><bold>SAFE"),
                Text.of(" <7>4 Pests <8>-5%"),
                Text.of(" <7>5 Pests <8>-15%"),
                Text.of(" <7>6 Pests <8>-30%"),
                Text.of(" <7>7 Pests <8>-50%"),
                Text.of(" <7>8 Pests <8>-75%"))));
    }

    private static int stored(SkyBlockPlayer player) {
        return Gardens.profile(player).pests().getStored();
    }

    private static net.minestom.server.item.ItemStack.Builder bag(SkyBlockPlayer player) {
        PestData data = Gardens.profile(player).pests();
        int pests = Math.min(data.getStored(), PestRules.MAX_TRADED_PESTS);
        long minutes = PestRules.tradeBoostMillis(ElectionPerks.isActive(SkyBlockMayor.Perk.PEST_ERADICATOR)) / 60_000;
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Empty your <6>Vacuum Bag <7>to receive"),
                Text.of("<7>bonus <6>Farming Fortune <7>while on"),
                Text.of("<7>your <a>Garden <7>over the next <a>{}m<7>!", minutes),
                Text.empty(),
                Text.of("<7>Vacuum Bag: <2>{} Pests", data.getStored()),
                Text.empty(),
                Text.of("<7>You will receive <6>+{} Farming", PestRules.tradeFortune(pests)),
                Text.of("<6>Fortune <7>from this exchange!"),
                Text.empty(),
                pests > 0 ? Text.of("<e>Click to empty your Vacuum Bag!") : Text.of("<c>You have no Pests to turn in!")));
        return ItemStacks.item(Material.HOPPER_MINECART, Text.of("<a>Empty Vacuum Bag"), lore);
    }

    private static net.minestom.server.item.ItemStack.Builder bonus(SkyBlockPlayer player) {
        PestData data = Gardens.profile(player).pests();
        long now = System.currentTimeMillis();
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Exchange <2>Pests <7>from your"),
                Text.of("<6>Vacuum Bag <7>with the <6>Pesthunter <7>to"),
                Text.of("<7>gain temporary <6>Farming Fortune"),
                Text.of("<7>while on your <a>Garden<7>."),
                Text.empty()));
        if (data.getBoostUntil() > now) {
            lore.add(Text.of("<7>Current bonus: <6>+{} Farming Fortune <7>({}m left)", data.getBoostFortune(), (data.getBoostUntil() - now) / 60_000 + 1));
            lore.add(Text.of("<a><bold>ACTIVE"));
        } else {
            lore.add(Text.of("<c><bold>INACTIVE"));
        }
        return ItemStacks.item(Material.CLOCK, Text.of("<e>Pesthunter Bonus"), lore);
    }

    private static void trade(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        int pests = Math.min(data.getStored(), PestRules.MAX_TRADED_PESTS);
        if (pests <= 0) {
            player.sendMessage(Text.of("<c>You have no Pests in your vacuum to turn in!"));
            return;
        }
        data.setStored(data.getStored() - pests);
        int fortune = (int) Math.round(PestRules.tradeFortune(pests) * (1 + dev.daweapon.type.skyblockgeneric.garden.chip.ChipService.bonus(profile.chips(), dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.HYPERCHARGE) / 100.0));
        data.setBoostFortune(fortune);
        data.setBoostUntil(System.currentTimeMillis() + PestRules.tradeBoostMillis(ElectionPerks.isActive(SkyBlockMayor.Perk.PEST_ERADICATOR)));
        Gardens.save(player, profile);
        long minutes = PestRules.tradeBoostMillis(ElectionPerks.isActive(SkyBlockMayor.Perk.PEST_ERADICATOR)) / 60_000;
        player.sendMessage(Text.of("<e>[NPC] <6>Phillip<f>: Thanks for the <6>Pest<f>, {}!", player.getUsername()));
        player.sendMessage(Text.of("<e>[NPC] <6>Phillip<f>: In exchange for <6>{} Pest<f>, I've given you <6>+{}☘ Farming Fortune <f>for <a>{}m<f>!",
                pests, fortune, minutes));
        player.sendMessage(Text.of("<e>[NPC] <6>Phillip<f>: Vacuum more Pests and exchange them with me to gain even more <6>☘ Farming Fortune<f>!"));
    }
}
