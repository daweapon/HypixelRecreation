package dev.daweapon.type.crystalhollows.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.skyblockgeneric.commission.CommissionZone;
import dev.daweapon.type.skyblockgeneric.commission.GUICommisions;

public class NPCEmissarySisko extends AbstractHollowsNPC {
    public NPCEmissarySisko() {
        super("Emissary Sisko", "<9>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhlYmY5Y2FjNWFiNDc5MDNlMWQyZDRjMjRmNjg1YTY2N2YyMDVmOWE2MTc3ZjZhNzYyMWY4NDcxOWU0ODc0NjUifX19", new Pos(495.5, 106, 556.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        new GUICommisions(CommissionZone.CRYSTAL_HOLLOWS).open(event.player());
    }
}
