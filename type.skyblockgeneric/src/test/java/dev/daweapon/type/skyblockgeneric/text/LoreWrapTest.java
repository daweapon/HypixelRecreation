package dev.daweapon.type.skyblockgeneric.text;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.RequirementLines;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoreWrapTest {
    private static final String LONG = "§8Not active yet: Fishing nets need the Hunting creature system (swimming Water creatures; only Huntraps exist)";

    @Test
    void overLongConfigLoreIsWrappedAndKeepsEveryWord() {
        List<Text> lines = LoreText.grayWrapped(LONG);
        assertTrue(lines.size() >= 3, "wrapped into " + lines.size());
        lines.forEach(line -> assertTrue(line.plain().length() <= 44, line.plain()));
        String joined = String.join(" ", lines.stream().map(Text::plain).toList());
        assertEquals("Not active yet: Fishing nets need the Hunting creature system (swimming Water creatures; only Huntraps exist)", joined);
    }

    @Test
    void handWrappedLoreIsLeftAlone() {
        assertEquals(1, LoreText.grayWrapped("§7Throw your fishing net at swimming").size());
        assertEquals(1, LoreText.grayWrapped("§7Grants §9+1 Crit Damage§7 while holding").size());
    }

    @Test
    void requirementLinesAreRecognised() {
        RequirementLines.Requirement skill = RequirementLines.parse("§4❣ §cRequires §aMining Skill 30§c.");
        assertEquals(RequirementLines.Kind.SKILL, skill.kind());
        assertEquals("Mining", skill.name());
        assertEquals(30, skill.level());
        assertEquals(7, RequirementLines.parse("§4❣ §cRequires §5Heart of the Mountain Tier 7§c.").level());
        assertEquals(RequirementLines.Kind.SLAYER, RequirementLines.parse("§4☠ §cRequires §5Wolf Slayer 3§c.").kind());
        assertNull(RequirementLines.parse("§7Nothing to see here"));
        assertNull(RequirementLines.parse("§4❣ §cRequires §5The Catacombs Floor VII Completion§c."));
        assertNotNull(RequirementLines.parse("<dark_red>❣ <c>Requires <a>Social Skill 5<c>."));
    }

    @Test
    void withoutAPlayerEveryRequirementStays() {
        List<String> lines = List.of("§7Line", "", "§4❣ §cRequires §aMining Skill 30§c.");
        assertEquals(lines, RequirementLines.withoutMetRequirements(lines, null));
    }
}
