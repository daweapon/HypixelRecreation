package dev.daweapon.type.generic.entity.npc.configuration;

import dev.daweapon.type.generic.user.HypixelPlayer;

public abstract class HumanConfiguration implements NPCConfiguration {

    public abstract String texture(HypixelPlayer player);

    public abstract String signature(HypixelPlayer player);

}
