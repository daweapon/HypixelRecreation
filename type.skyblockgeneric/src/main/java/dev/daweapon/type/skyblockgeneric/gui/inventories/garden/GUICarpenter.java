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
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICarpenter extends StatelessView {
    private final int plot;

    public GUICarpenter(int plot) {
        this.plot = plot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Carpenter"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(20, (_, _) -> ItemStacks.item(Material.WHEAT_SEEDS, Text.of("<a>Crop Guide"), List.of(
                        Text.of("<7>In the <a>Greenhouse<7>, every crop has"),
                        Text.of("<9>unique effects <7>that affect adjacent"),
                        Text.of("<7>crops with <a>buffs <7>or <c>debuffs<7>."),
                        Text.of("<7>A crop can have multiple different"),
                        Text.of("<7>effects; however, the same effect"),
                        Text.of("<7>can't be active twice!"),
                        Text.empty(),
                        Text.of("<e>Unique Bonus:"),
                        Text.of("<7>Each unique <c>non-mutated <7>crop"),
                        Text.of("<7>currently growing in the Greenhouse"),
                        Text.of("<7>grants <a>+2.5% <b>Growth Speed <7>and <a>+2.5%"),
                        Text.of("<e>Yield <7>to all crops."),
                        Text.empty(),
                        Text.of("<e>Click to learn more about them!"))),
                (_, c) -> c.push(new GUICropGuide()));
        layout.slot(22, (_, _) -> ItemStacks.item(Material.PAINTING, Text.of("<a>All Mutations"), List.of(
                Text.empty(),
                Text.of("<c>Mutations <7>have a chance to spread"),
                Text.of("<7>from growing <a>crops <7>to nearby empty"),
                Text.of("<7>plots within the <a>Greenhouse<7>."),
                Text.of("<7>Each <c>mutation <7>requires a specific"),
                Text.of("<7>combination of <a>crops <7>to spread."),
                Text.empty(),
                Text.of("<7>For detailed requirements and"),
                Text.of("<7>spread conditions, check out your"),
                Text.of("<7>unlocked <c>mutations<7>!"),
                Text.empty(),
                Text.of("<e>Click to view!"))),
                (_, c) -> c.push(new GUIAllMutations()));
        layout.slot(24, (_, c) -> slots((SkyBlockPlayer) c.player()));
        layout.slot(48, (_, _) -> ItemStacks.item(Material.DEAD_BUSH, Text.of("<c>Decay Information"), List.of(
                Text.of("<c>Decay <7>is a mechanic in which a <a>crop"),
                Text.of("<7>or <c>mutation <7>can <c>die <7>after <e>mutating <7>a"),
                Text.of("<7>certain number of times."),
                Text.empty(),
                Text.of("<7>If you only have <a>1 <7>of the same crop"),
                Text.of("<7>or mutation, it can <e>mutate <a>X <7>times"),
                Text.of("<7>before being able to <c>decay<7>."),
                Text.empty(),
                Text.of("<7>If you have <a>2+ <7>of the same crops or"),
                Text.of("<7>mutations, their total number of"),
                Text.of("<e>mutates <7>are <b>combined<7>. Once the"),
                Text.of("<b>combined mutates <7>reaches zero, they"),
                Text.of("<7>can <c>decay<7>. <8>Must be fully grown and"),
                Text.of("<8>contribute to spreading a mutation to"),
                Text.of("<8>count towards the combined total."),
                Text.empty(),
                Text.of("<7>Each crop and mutation has a <c>decay"),
                Text.of("<c>attempt <7>timer that is always counting"),
                Text.of("<7>down. For a <c>decay attempt <7>to be"),
                Text.of("<a>successful<7>, the <b>combined <7>or"),
                Text.of("<7>individual <e>mutates <7>remaining must be"),
                Text.of("<7>zero. <8>The decay attempt timer resets"),
                Text.of("<8>to 24h when it runs out."))));
    }

    private net.minestom.server.item.ItemStack.Builder slots(SkyBlockPlayer player) {
        GreenhouseData.Bed bed = GreenhouseService.data(player).bed(plot);
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Sometimes when you break a crop or"),
                Text.of("<c>mutation<7>, it has a chance to drop an"),
                Text.of("<5>Ethereal Vine<7>, which can be used to"),
                Text.of("<7>unlock additional <6>plant slots <7>in your"),
                Text.of("<a>Greenhouse<7>."),
                Text.empty(),
                Text.of("<7>Your Slots:")));
        if (bed != null) {
            for (int row = 0; row < GreenhouseData.SIZE; row++) {
                StringBuilder line = new StringBuilder();
                for (int column = 0; column < GreenhouseData.SIZE; column++) {
                    int slot = GreenhouseData.slot(column, row);
                    line.append(bed.getUnlocked()[slot] ? "<a>■" : GreenhouseService.adjacentToUnlocked(bed, slot) ? "<e>■" : "<c>■");
                }
                lore.add(Text.of(line.toString()));
            }
        }
        return ItemStacks.item(Material.DIRT, Text.of("<a>Unlocking Plant Slots"), lore);
    }
}
