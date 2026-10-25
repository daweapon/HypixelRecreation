package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.gui.GUIHunterJean;
import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCHunterJean extends TalkNPC {
    private static final String MET = "npc.hunter_jean";

    public NPCHunterJean() {
        super(new Spec("Hunter Jean", "b",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4MzYzNjQzODI2NywKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IxMjU1MGVkMzQ1MDU1YjExOTU5MmFmY2M4OTA0YzczYTk5ZWFhZGNhYzc5ODFhNzQ4MWRjMjViZThiMTc5NDAiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "RV+Goslwd6IofaEZGXjIXqk+o7aoSC4g93Hc4y2crjELR22inWeLVN9cwu7T1YR92fAhvTyBdHfCizbfyWC3MjZPhTpu4xyH4qeQQbtWFYBiK1oDen/Tx9ppQ0TVBupDO9oAWzbA+/AU7EddaOhATDeIQH+5RD4F9h5uGnsU9KG01uQtliFyAPqDcNzCAiYDZNmXMzWO32hG8bc/Dh0Unx2xDdHdC80kpzX6Qxkoh8s5/CtyfVE+IMJRF03F+/HI/cdGl4R/HEAoYeICVIr+nFVyh/ySDijg1SSWbJ2EVfQLddccbDmV6/bn9D9IquKE4oS2pzeSl/0YEuuklVILWxUbLsdP7jMZGO2cOin1KIsDJ9hmJXclRkFTUKBV8aCU3dLvUT4cpg0Ys+jmg+M9uxV81DqHaiS6OcFQT8PcgjUxFIXxSXaSOXBT3bSFYxJ5APQx+C/QHCm0h77RWRwnMMSk+5Lxxm+5n4tGRYUS/ML6jxQfkH54WRG4PMK66kJiXV9wnHL7ftPRmlPnvqhf+ulkoGigvzZjCv6s6o9hEkQXA1RsqKTkqCnCIW7/8B7YgUr5crvXzWLzfiUu80vV2G8qRw6aNeCiD84ttUMwhLsQDgGUPcOqgMis8Cz4yDgshetH/lE01g+7OUZduCZ5c7yWfb5gDhEoxvFZ+b2SMCU=",
                new Pos(-665.5, 145, 170.5, -180f, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (ChapterTriggers.done(player, MET)) {
            player.openView(new GUIHunterJean());
            return;
        }
        setDialogue(player, "first-interaction").thenRun(() -> {
            ChapterTriggers.fire(player, MET);
            player.openView(new GUIHunterJean());
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first-interaction").lines(
                        "I don't really hunt <a> Critters<f>, it's not enough of a challenge.",
                        "<6>Torrhus Sea Creatures<f>, though? That's more like it.",
                        "Here's all of the ones that I've found throughout my time in <b>Torrhus Springs<f>."
                ).build()
        };
    }
}
