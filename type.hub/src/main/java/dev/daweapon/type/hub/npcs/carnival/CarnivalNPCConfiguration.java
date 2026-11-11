package dev.daweapon.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.carnival.CarnivalSkin;
import dev.daweapon.type.skyblockgeneric.carnival.Carnival;

/** A Carnival NPC: it only stands in the Hub while Foxy's Carnival is open. */
public class CarnivalNPCConfiguration extends HumanConfiguration {
    private final String[] holograms;
    private final String chatName;
    private final CarnivalSkin skin;
    private final Pos position;

    public CarnivalNPCConfiguration(String chatName, CarnivalSkin skin, Pos position, String... holograms) {
        this.chatName = chatName;
        this.skin = skin;
        this.position = position;
        this.holograms = holograms;
    }

    public static CarnivalNPCConfiguration host(String name, CarnivalSkin skin, Pos position) {
        return new CarnivalNPCConfiguration(null, skin, position, name, "<e><l>CLICK");
    }

    @Override
    public String[] holograms(HypixelPlayer player) {
        return holograms;
    }

    @Override
    public String chatName(HypixelPlayer player) {
        return chatName;
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
