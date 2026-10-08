package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** A town villager that counts towards the arrival quest's "talk to 4 villagers" step for its faction. */
public abstract class FactionVillagerNPC extends CrimsonNPC {
    private final CrimsonFaction faction;

    protected FactionVillagerNPC(String name, String texture, Pos home, CrimsonFaction faction) {
        super(name, texture, home);
        this.faction = faction;
        FactionVillagers.register(this, faction);
    }

    protected abstract void talk(SkyBlockPlayer player);

    @Override
    protected final void interact(SkyBlockPlayer player) {
        if (FactionVillagers.intercept(this, player)) return;
        talk(player);
    }

    public CrimsonFaction faction() {
        return faction;
    }

    protected boolean isMember(SkyBlockPlayer player) {
        return CrimsonReputationService.isMember(player, faction);
    }
}
