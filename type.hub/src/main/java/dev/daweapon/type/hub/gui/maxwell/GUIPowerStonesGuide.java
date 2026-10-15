package dev.daweapon.type.hub.gui.maxwell;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowers;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers.PowerData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;

/** Every stone power, its stone, where the stone comes from and whether it's learned. */
public class GUIPowerStonesGuide extends StatelessView {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Power Stones Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        PowerData data = AccessoryPowers.data((SkyBlockPlayer) ctx.player());
        List<AccessoryPowerType> powers = AccessoryPowerType.stonePowers().stream()
                .sorted(Comparator.comparing(AccessoryPowerType::getDisplayName)).toList();
        for (int i = 0; i < powers.size() && i < SLOTS.length; i++) {
            AccessoryPowerType power = powers.get(i);
            boolean learned = data.getLearned().contains(power);
            layout.slot(SLOTS[i], (s, c) -> MaxwellItems.withText(MaxwellItems.icon(power),
                    "<a>" + power.getDisplayName() + "\n<8>" + power.getTier().getDisplayName() + "\n\n"
                            + "<7>Power stone:\n" + MaxwellItems.stoneName(power) + "\n\n"
                            + "<7>Source:\n<f>" + power.getSource()
                            + (power.isSourceActive() ? "" : "\n<8>Not active yet") + "\n\n"
                            + "<7>Combat level required: <a>" + StringUtility.getAsRomanNumeral(power.getCombatLevel()) + "\n\n"
                            + "<7>Learned: " + (learned ? "<a>Yes ✔" : "<c>Not Yet ✖")));
        }
    }
}
