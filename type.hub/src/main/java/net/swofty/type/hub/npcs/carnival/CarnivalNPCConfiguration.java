package net.swofty.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.carnival.CarnivalSkin;
import net.swofty.type.skyblockgeneric.carnival.Carnival;

/** A Carnival NPC: it only stands in the Hub while Foxy's Carnival is open. */
public class CarnivalNPCConfiguration extends HumanConfiguration {
    private final String name;
    private final CarnivalSkin skin;
    private final Pos position;

    public CarnivalNPCConfiguration(String name, CarnivalSkin skin, Pos position) {
        this.name = name;
        this.skin = skin;
        this.position = position;
    }

    @Override
    public String[] holograms(HypixelPlayer player) {
        return new String[]{name, "<e><l>CLICK"};
    }

    @Override
    public String texture(HypixelPlayer player) {
        return skin.texture();
    }

    @Override
    public String signature(HypixelPlayer player) {
        return skin.signature();
    }

    @Override
    public Pos position(HypixelPlayer player) {
        return position;
    }

    @Override
    public boolean looking(HypixelPlayer player) {
        return true;
    }

    @Override
    public boolean visible(HypixelPlayer player) {
        return Carnival.isOpen();
    }
}
