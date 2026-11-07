package dev.daweapon.type.skyblockgeneric.event.actions.custom.skill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionSkillDisplayTest {
    @Test
    void skillXpShowsAtMostOneDecimal() {
        assertEquals("8.7", ActionSkillDisplay.oneDecimal(8.699999999999989));
        assertEquals("14.7", ActionSkillDisplay.oneDecimal(14.749999999999943));
        assertEquals("200", ActionSkillDisplay.oneDecimal(200));
        assertEquals("1,234.5", ActionSkillDisplay.oneDecimal(1234.56));
        assertEquals("8.7", ActionSkillDisplay.oneDecimal(8.7));
    }
}
