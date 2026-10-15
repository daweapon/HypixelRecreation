package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.museum.GUIYourMuseum;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCMadameEleanorQGoldsworthIII extends HypixelNPC {

    public NPCMadameEleanorQGoldsworthIII() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Madame Eleanor Q. Goldsworth III", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VnhMZ2a55oTztZzmxmmkoTQ8vmKUFyh1QUFBLHeVGB6AsaFeWhef+NLUtftoWAf64m78I+OkmIQcexSHtCLZm6xSGitEcx8i/TRx5X25ZsV+jHKd2jJQMMA/BFrnnfawvV6KZ7zqbc7m/5Uv4aOHZxcYt/EBJyNiDBpqhEGDX/Ulo34Ti87JTHp5lT4qG1pmND9FQw4T/1JNdJTI+Wlxw2Ux9k2tRloDAUtyb9YDwYjMn27Ua49eYtVv3tFawuibXFtIj0u/Ni88PF25zAU2kE+1i3dtmj6htQ/Nzgc8gaFB/cETHD997D4/DkpPPnCPL1sd8iO63ncma5aFxvaBwAh97bGIwNUrsJJy2AtYlluD3PHrIIukKCuN+v37+Tn8KM9AbYVVfpJ6Z1Xot+s3BClWuzo4+sAvfBER6QiOvCYuSBjlGxCagSEIaBkxc6YIFhVs5Wa1ijpPcebB+HROePr2lQNkRFjiA4QYDprZJLm6HeGpXAhHtKAEb/D965sYe1EY3zDSPB33ZdO/Yq0u5oq+jyzAIurmS/oHWbMJ8VPWF39jUzc7ykSosEUDwOt1N6bO67pOB7Axjkt/45zJiYgAiU9XOEAsfkeSQcHKeJve3yKKizs0j6nPXGsF9z+mYwVokYLFbisanum6GtXHmhCNNJEkHbX6cgOid5V0I4U=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYzNTE2OTIwNTU3NywKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I3ZTBmYzhkZjkwNjdkMzkzYzk3N2YxZjM0MDI1NjM0MjIzZGIxNDVlOTQwM2RjMzJjMzExODg5ZTMzNjU5NjUiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(33.500, 73.000, 11.500, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MADAME_ELEANOR)) {
            setDialogue(player, "welcome").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MADAME_ELEANOR, true);
                player.openView(new GUIYourMuseum());
            });
            return;
        }

        player.openView(new GUIYourMuseum());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        return Stream.of(
                DialogueSet.builder()
                        .key("welcome")
                        .line("Welcome, {}<f>!", player.getShortenedDisplayName())
                        .line("This world has an <o>incredible <f>amount of interesting gear and rare items, which I would like to showcase.")
                        .line("Would you like to help me fill the <9>Museum<f>? I can even reward you for your contributions!")
                        .line("I'll show you the list of items that I'm currently looking for.")
                        .line("You can donate any item from the list and you'll still be able to use those items whenever you want!")
                        .line("However, once you donate an item, it will become <c>Co-op Soulbound <f>to you, meaning you won't be able to trade off your profile, auction, or sell the item anymore.")
                        .line("Choose carefully!")
                        .build()
        ).toArray(DialogueSet[]::new);
    }
}
