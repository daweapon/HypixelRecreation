package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** A faction town NPC; most count towards the arrival quest's "talk to 4 villagers" step for their faction. */
public abstract class FactionVillagerNPC extends CrimsonNPC {
    private final CrimsonFaction faction;

    protected FactionVillagerNPC(String name, String texture, Pos home, CrimsonFaction faction) {
        this(name, texture, home, faction, true);
    }

    /** {@code villager} false: a faction NPC that does not take part in the "talk to 4 villagers" step. */
    protected FactionVillagerNPC(String name, String texture, Pos home, CrimsonFaction faction, boolean villager) {
        super(name, texture, home);
        this.faction = faction;
        if (villager) FactionVillagers.register(this, faction);
    }

    protected FactionVillagerNPC(String name, EntityType entityType, Pos home, CrimsonFaction faction) {
        super(name, entityType, home);
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
