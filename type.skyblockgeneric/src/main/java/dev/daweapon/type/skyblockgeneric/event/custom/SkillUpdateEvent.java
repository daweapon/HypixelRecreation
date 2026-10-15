package dev.daweapon.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
// Fired when a player's skill XP changes, carrying both raw and cumulative old/new values
public class SkillUpdateEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final SkillCategories skillCategory;
    private final double oldValueRaw;
    private final double oldValueCumulative;
    private final double newValueRaw;
    private final double newValueCumulative;

    public SkillUpdateEvent(SkyBlockPlayer player, SkillCategories skillCategory, double oldValueRaw, double oldValueCumulative, double newValueRaw, double newValueCumulative) {
        this.player = player;
        this.skillCategory = skillCategory;
        this.oldValueRaw = oldValueRaw;
        this.oldValueCumulative = oldValueCumulative;
        this.newValueRaw = newValueRaw;
        this.newValueCumulative = newValueCumulative;
    }
}
