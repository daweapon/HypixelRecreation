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

import java.util.List;

public class GUIPesthunter extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Pesthunter Phillip"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(11, (_, c) -> ItemStacks.item(Material.CHEST, Text.of("<a>Pesthunter's Wares"), List.of(
                        Text.of("<7>Trade the <2>Pests <7>in your vacuum for"),
                        Text.of("<7>traps, armor and accessories."),
                        Text.empty(),
                        Text.of("<7>Pests in your vacuum: <2>{}", stored((SkyBlockPlayer) c.player())),
                        Text.empty(),
                        Text.of("<e>Click to browse!"))),
                (_, c) -> c.push(new CatalogueShopView("pesthunter_wares")));
        layout.slot(15, (_, c) -> bonus((SkyBlockPlayer) c.player()), (_, c) -> {
            trade((SkyBlockPlayer) c.player());
            c.session(DefaultState.class).refresh();
        });
    }

    private static int stored(SkyBlockPlayer player) {
        return Gardens.profile(player).pests().getStored();
    }

    private static net.minestom.server.item.ItemStack.Builder bonus(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        int pests = Math.min(data.getStored(), PestRules.MAX_TRADED_PESTS);
        long now = System.currentTimeMillis();
        return ItemStacks.item(Material.GOLDEN_CARROT, Text.of("<a>Bonus Farming Fortune"), List.of(
                Text.of("<7>Turn in the <2>Pests <7>in your vacuum for"),
                Text.of("<7>temporary <6>Farming Fortune<7>."),
                Text.empty(),
                Text.of("<7>Rate: <6>+{} Farming Fortune <7>per Pest", PestRules.FORTUNE_PER_TRADED_PEST),
                Text.of("<7>Maximum: <6>+{} Farming Fortune <7>({} Pests)",
                        PestRules.tradeFortune(PestRules.MAX_TRADED_PESTS), PestRules.MAX_TRADED_PESTS),
                Text.of("<7>Duration: <a>{}m", PestRules.tradeBoostMillis(ElectionPerks.isActive(SkyBlockMayor.Perk.PEST_ERADICATOR)) / 60_000),
                Text.empty(),
                Text.of("<7>Pests in your vacuum: <2>{}", data.getStored()),
                data.getBoostUntil() > now
                        ? Text.of("<7>Current bonus: <6>+{} Farming Fortune <7>({}m left)", data.getBoostFortune(), (data.getBoostUntil() - now) / 60_000 + 1)
                        : Text.of("<7>Current bonus: <c>None"),
                Text.empty(),
                pests > 0 ? Text.of("<e>Click to turn in {} Pests!", pests) : Text.of("<c>You have no Pests to turn in!")));
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
